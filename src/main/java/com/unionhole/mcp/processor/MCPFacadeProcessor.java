/*
 * Copyright 2024 James Zou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unionhole.mcp.processor;

import com.unionhole.mcp.annotation.MCPMethod;
import com.unionhole.mcp.annotation.MCPService;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import com.unionhole.mcp.generator.DemoProjectGenerator;

/**
 * Annotation processor for generating MCP Facade classes
 *
 * @author James Zou
 * @version 1.0.0
 * @since 2024/03/19
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.unionhole.mcp.annotation.MCPService",
    "com.unionhole.mcp.annotation.MCPMethod"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MCPFacadeProcessor extends AbstractProcessor {
    private DocTrees docTrees;
    private Map<String, Set<String>> existingFacadeMethods = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.docTrees = DocTrees.instance(processingEnv);
        
        // 检查是否需要生成demo项目
        Map<String, String> options = processingEnv.getOptions();
        
        // 获取demo生成配置
        String demoEnabled = options.get("mcp.demo.output");
        String configFile = options.get("mcpConfigFile");

        // 如果直接指定了配置，使用该配置
        if (demoEnabled != null && Boolean.parseBoolean(demoEnabled)) {
            try {
                // 获取当前工程根目录
                String projectRoot = System.getProperty("user.dir");
                generateDemoProject(projectRoot + "/demo");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 否则尝试从配置文件读取
        else if (configFile != null && !configFile.isEmpty()) {
            try {
                Properties props = new Properties();
                File file = new File(configFile);
                if (file.exists()) {
                    if (configFile.endsWith(".properties")) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            props.load(fis);
                            String enabled = props.getProperty("mcp.demo.output");
                            if (Boolean.parseBoolean(enabled)) {
                                String projectRoot = System.getProperty("user.dir");
                                generateDemoProject(projectRoot + "/demo");
                            }
                        }
                    } else if (configFile.endsWith(".yml") || configFile.endsWith(".yaml")) {
                        // 简单的 YAML 解析
                        List<String> lines = Files.readAllLines(file.toPath());
                        for (String line : lines) {
                            if (line.trim().startsWith("mcp.demo.output:")) {
                                String enabled = line.split(":")[1].trim();
                                if (Boolean.parseBoolean(enabled)) {
                                    String projectRoot = System.getProperty("user.dir");
                                    generateDemoProject(projectRoot + "/demo");
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 处理带有 MCPService 注解的类
        for (Element element : roundEnv.getElementsAnnotatedWith(MCPService.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            processServiceClass(typeElement);
        }

        // 处理带有 MCPMethod 注解的方法
        for (Element element : roundEnv.getElementsAnnotatedWith(MCPMethod.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            ExecutableElement methodElement = (ExecutableElement) element;
            Element enclosingElement = methodElement.getEnclosingElement();
            
            // 检查方法所在的类是否有 MCPService 注解
            if (enclosingElement.getAnnotation(MCPService.class) != null) {
                TypeElement typeElement = (TypeElement) enclosingElement;
                String facadeClassName = typeElement.getSimpleName().toString() + "Facade";
                String methodName = methodElement.getSimpleName().toString();
                
                // 检查方法是否已存在
                if (existingFacadeMethods.containsKey(facadeClassName) && 
                    existingFacadeMethods.get(facadeClassName).contains(methodName)) {
                    // 方法已存在，跳过生成
                    continue;
                }
                
                processMethodInService(typeElement, methodElement);
            }
        }

        return true;
    }

    private void processServiceClass(TypeElement typeElement) {
        MCPService annotation = typeElement.getAnnotation(MCPService.class);
        String originalPackage = processingEnv.getElementUtils()
                .getPackageOf(typeElement).getQualifiedName().toString();
        String targetPackage = annotation.packageName().isEmpty()
                ? originalPackage
                : annotation.packageName();
        String className = typeElement.getSimpleName().toString();
        String facadeClassName = className + "Facade";

        try {
            String projectRoot = findProjectRoot();
            File facadeFile = getFacadeFile(projectRoot, targetPackage, facadeClassName);
            
            // 如果 Facade 文件不存在，则生成新文件
            if (!facadeFile.exists()) {
                generateNewFacadeClass(projectRoot, targetPackage, className, facadeClassName, typeElement);
            } else {
                // 如果文件存在，读取现有方法
                loadExistingMethods(facadeFile, facadeClassName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processMethodInService(TypeElement typeElement, ExecutableElement methodElement) {
        MCPService serviceAnnotation = typeElement.getAnnotation(MCPService.class);
        String originalPackage = processingEnv.getElementUtils()
                .getPackageOf(typeElement).getQualifiedName().toString();
        String targetPackage = serviceAnnotation.packageName().isEmpty()
                ? originalPackage
                : serviceAnnotation.packageName();
        String className = typeElement.getSimpleName().toString();
        String facadeClassName = className + "Facade";

        try {
            String projectRoot = findProjectRoot();
            File facadeFile = getFacadeFile(projectRoot, targetPackage, facadeClassName);
            
            // 如果文件不存在，先创建文件
            if (!facadeFile.exists()) {
                generateNewFacadeClass(projectRoot, targetPackage, className, facadeClassName, typeElement);
                return;
            }
            
            // 如果文件存在但方法集合未加载，先加载现有方法
            if (!existingFacadeMethods.containsKey(facadeClassName)) {
                loadExistingMethods(facadeFile, facadeClassName);
            }
            
            // 检查方法是否已存在
            String methodName = methodElement.getSimpleName().toString();
            if (!existingFacadeMethods.containsKey(facadeClassName) || 
                !existingFacadeMethods.get(facadeClassName).contains(methodName)) {
                // 方法不存在，添加到文件
                appendMethodToFacade(facadeFile, methodElement);
                // 更新方法集合
                existingFacadeMethods.computeIfAbsent(facadeClassName, k -> new HashSet<>()).add(methodName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFacadeFile(String projectRoot, String packageName, String facadeClassName) {
        String packagePath = packageName.replace('.', '/');
        File packageDir = new File(projectRoot, packagePath);
        return new File(packageDir, facadeClassName + ".java");
    }

    private void loadExistingMethods(File facadeFile, String facadeClassName) throws IOException {
        Set<String> methods = new HashSet<>();
        List<String> lines = Files.readAllLines(facadeFile.toPath());
        
        for (String line : lines) {
            // 更新方法签名检测逻辑
            if (line.trim().startsWith("public ") && line.contains("(")) {
                String methodSignature = line.trim();
                int startIndex = methodSignature.lastIndexOf(" ", methodSignature.indexOf("("));
                if (startIndex != -1) {
                    String methodName = methodSignature.substring(startIndex + 1, methodSignature.indexOf("("));
                    methods.add(methodName);
                }
            }
        }
        
        existingFacadeMethods.put(facadeClassName, methods);
    }

    private void appendMethodToFacade(File facadeFile, ExecutableElement method) throws IOException {
        List<String> lines = Files.readAllLines(facadeFile.toPath());
        int insertIndex = findInsertIndex(lines);
        
        List<String> methodLines = generateMethodLines(method);
        lines.addAll(insertIndex, methodLines);
        
        Files.write(facadeFile.toPath(), lines);
    }

    private int findInsertIndex(List<String> lines) {
        // 在最后一个 } 之前插入
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (lines.get(i).trim().equals("}")) {
                return i;
            }
        }
        return lines.size() - 1;
    }

    private List<String> generateMethodLines(ExecutableElement method) {
        List<String> lines = new ArrayList<>();
        String methodName = method.getSimpleName().toString();
        MCPMethod annotation = method.getAnnotation(MCPMethod.class);
        String description = annotation != null && !annotation.description().isEmpty() 
            ? annotation.description() 
            : getMethodDescription(method);

        lines.add("");
        lines.add("    @Tool(description = \"" + description + "\")");
        
        // 获取原始方法的返回类型
        String returnType = method.getReturnType().toString();
        
        // 生成方法签名
        StringBuilder signature = new StringBuilder("    public " + returnType + " " + methodName + "(");
        List<? extends VariableElement> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement param = parameters.get(i);
            if (i > 0) {
                signature.append(", ");
            }
            signature.append(param.asType().toString())
                    .append(" ")
                    .append(param.getSimpleName().toString());
        }
        signature.append(") {");
        lines.add(signature.toString());

        // 生成方法体
        lines.add("        try {");
        StringBuilder methodCall = new StringBuilder("            return service." + methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                methodCall.append(", ");
            }
            methodCall.append(parameters.get(i).getSimpleName().toString());
        }
        methodCall.append(");");
        lines.add(methodCall.toString());
        lines.add("        } catch (Exception e) {");
        lines.add("            throw new RuntimeException(e.getMessage(), e);");
        lines.add("        }");
        lines.add("    }");

        return lines;
    }

    private String findProjectRoot() {
        try {
            // 获取当前工作目录
            String userDir = System.getProperty("user.dir");
            return userDir + "/src/main/java";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateNewFacadeClass(String projectRoot, String packageName,
                                     String serviceClassName, String facadeClassName,
                                     TypeElement typeElement) throws IOException {
        if (projectRoot == null) {
            throw new IOException("Cannot determine project root directory");
        }

        // 创建包目录
        String packagePath = packageName.replace('.', '/');
        File packageDir = new File(projectRoot, packagePath);
        packageDir.mkdirs();

        // 创建 Facade 文件
        File facadeFile = new File(packageDir, facadeClassName + ".java");

        // 如果文件已存在，先删除
        if (facadeFile.exists()) {
            facadeFile.delete();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(facadeFile))) {
            // 生成包声明
            out.println("package " + packageName + ";");
            out.println();

            // 生成导入语句
            out.println("import org.springframework.ai.tool.annotation.Tool;");
            out.println("import org.springframework.stereotype.Service;");
            out.println("import org.springframework.beans.factory.annotation.Autowired;");
            out.println("import org.springframework.stereotype.Component;");
            // 如果 Service 类在不同包，需要导入
            if (!packageName.equals(processingEnv.getElementUtils()
                    .getPackageOf(typeElement).getQualifiedName().toString())) {
                out.println("import " + typeElement.getQualifiedName().toString() + ";");
            }
            out.println();

            // Write class header comment
            writeFileHeader(out, serviceClassName);

            out.println("@Component");
            // 生成类声明
            out.println("public class " + facadeClassName + " {");

            // 生成 service 实例
            out.println("    @Autowired");
            out.println("    private " + serviceClassName + " service;");
            out.println();

            // 生成所有公共方法
            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getModifiers().contains(Modifier.PUBLIC)) {
                        generateMethodImplementation(out, method);
                    }
                }
            }

            out.println("}");
        }
    }

    private void generateMethodImplementation(PrintWriter out, ExecutableElement method) {
        String methodName = method.getSimpleName().toString();

        // 获取方法的文档注释
        String description = getMethodDescription(method);

        // 生成 @Tool 注解
        out.println("    @Tool(description = \"" + (description != null ? description : methodName) + "\")");

        // 获取原始方法的返回类型
        String returnType = method.getReturnType().toString();

        // 生成方法签名，使用原始参数和返回类型
        out.print("    public " + returnType + " " + methodName + "(");
        
        // 添加原始参数
        List<? extends VariableElement> parameters = method.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            VariableElement param = parameters.get(i);
            if (i > 0) {
                out.print(", ");
            }
            out.print(param.asType().toString() + " " + param.getSimpleName().toString());
        }
        out.println(") {");

        // 生成方法体
        out.println("        try {");

        // 生成方法调用
        out.print("            return service." + methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                out.print(", ");
            }
            out.print(parameters.get(i).getSimpleName().toString());
        }
        out.println(");");

        // 生成异常处理
        out.println("        } catch (Exception e) {");
        out.println("            throw new RuntimeException(e.getMessage(), e);");
        out.println("        }");
        out.println("    }");
        out.println();
    }

    private String getMethodDescription(ExecutableElement method) {
        DocCommentTree docCommentTree = docTrees.getDocCommentTree(method);
        if (docCommentTree != null) {
            String comment = docCommentTree.getFullBody().toString().trim();
            // 清理注释中的换行符和多余空格
            comment = comment.replaceAll("\\s+", " ");
            // 清理特殊字符，避免生成的代码出错
            comment = comment.replaceAll("\"", "'");
            return comment;
        }
        return method.getSimpleName().toString();
    }

    /**
     * Generate file header comment for the generated facade class
     */
    private void writeFileHeader(PrintWriter out, String className) {
        out.println("/**");
        out.println(" * Auto-generated MCP Facade class for " + className);
        out.println(" *");
        out.println(" * @author James Zou");
        out.println(" * @version 1.0.0");
        out.println(" * @since " + java.time.LocalDate.now().toString());
        out.println(" */");
    }

    private void generateDemoProject(String outputPath) throws IOException {
        DemoProjectGenerator generator = new DemoProjectGenerator(
            outputPath,
            "com.demo",
            "mcp-demo",
            "1.0-SNAPSHOT",
            "3.2.3",
            "1.0.0-SNAPSHOT"
        );
        generator.generate();
    }
}