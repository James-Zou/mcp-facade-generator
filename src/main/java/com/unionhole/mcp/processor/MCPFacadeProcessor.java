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


import com.unionhole.mcp.annotation.MCPService;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.io.File;
import java.io.FileWriter;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
/**
 * Annotation processor for generating MCP Facade classes
 *
 * @author James Zou
 * @version 1.0.0
 * @since 2024/03/19
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.unionhole.mcp.annotation.MCPService")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MCPFacadeProcessor extends AbstractProcessor {
    private DocTrees docTrees;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.docTrees = DocTrees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(MCPService.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            MCPService annotation = typeElement.getAnnotation(MCPService.class);
            
            String originalPackage = processingEnv.getElementUtils()
                    .getPackageOf(typeElement).getQualifiedName().toString();
            
            String targetPackage = annotation.packageName().isEmpty() 
                    ? originalPackage 
                    : annotation.packageName();
            
            String className = typeElement.getSimpleName().toString();
            String facadeClassName = className + "Facade";

            try {
                // 获取项目根目录
                String projectRoot = findProjectRoot();
                generateFacadeClass(projectRoot, targetPackage, className, facadeClassName, typeElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
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

    private void generateFacadeClass(String projectRoot, String packageName, 
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

            out.println("import com.unionhole.mcp.vo.MCPRequest;");
            out.println("import com.unionhole.mcp.vo.MCPResponse;");
            out.println("import org.springframework.ai.tool.annotation.Tool;");
            out.println("import org.springframework.stereotype.Service;");
            // 如果 Service 类在不同包，需要导入
            if (!packageName.equals(processingEnv.getElementUtils()
                    .getPackageOf(typeElement).getQualifiedName().toString())) {
                out.println("import " + typeElement.getQualifiedName().toString() + ";");
            }
            out.println();

            // Write class header comment
            writeFileHeader(out, serviceClassName);


            // 生成类声明
            out.println("public class " + facadeClassName + " {");
            
            // 生成 service 实例
            out.println("    private final " + serviceClassName + " service;");
            out.println();
            
            // 生成构造函数
            out.println("    public " + facadeClassName + "(" + serviceClassName + " service) {");
            out.println("        this.service = service;");
            out.println("    }");
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
        
        // 生成方法签名
        out.print("    public MCPResponse " + methodName + "(MCPRequest request) ");
        out.println("{");
        
        // 生成方法体
        out.println("        try {");
        out.println("            // 解析请求参数");
        
        // 生成参数处理代码
        List<? extends VariableElement> parameters = method.getParameters();
        if (!parameters.isEmpty()) {
            for (VariableElement param : parameters) {
                String paramName = param.getSimpleName().toString();
                String paramType = param.asType().toString();
                out.println("            " + paramType + " " + paramName + 
                          " = request.getParameter(\"" + paramName + "\", " + paramType + ".class);");
            }
        }
        
        // 生成方法调用
        out.print("            Object result = service." + methodName + "(");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                out.print(", ");
            }
            out.print(parameters.get(i).getSimpleName().toString());
        }
        out.println(");");
        
        // 生成返回值处理
        out.println("            return MCPResponse.success(result);");
        out.println("        } catch (Exception e) {");
        out.println("            return MCPResponse.error(e.getMessage());");
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
} 