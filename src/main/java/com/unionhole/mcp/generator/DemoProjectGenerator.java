package com.unionhole.mcp.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DemoProjectGenerator {
    private final String outputPath;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String springBootVersion;
    private final String springAiVersion;

    public DemoProjectGenerator(String outputPath, String groupId, String artifactId, 
                              String version, String springBootVersion, String springAiVersion) {
        this.outputPath = outputPath;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.springBootVersion = springBootVersion;
        this.springAiVersion = springAiVersion;
    }

    public void generate() throws IOException {
        // 创建项目根目录
        Path projectRoot = Paths.get(outputPath, artifactId);
        Files.createDirectories(projectRoot);

        // 生成pom.xml
        generatePom(projectRoot);

        // 生成 README.md
        generateReadme(projectRoot);
        
        // 创建源代码目录结构
        String packagePath = groupId.replace('.', '/');
        Path srcPath = projectRoot.resolve("src/main/java/" + packagePath);
        Files.createDirectories(srcPath);

        // 创建服务目录
        Path servicePath = srcPath.resolve("service");
        Files.createDirectories(servicePath);

        // 生成配置文件目录
        Path resourcesPath = projectRoot.resolve("src/main/resources");
        Files.createDirectories(resourcesPath);

        // 生成主要的类文件
        generateMainClass(srcPath);
        generateConfigClass(srcPath);
        generateApplicationProperties(resourcesPath);
        generateTestClass(projectRoot.resolve("src/test/java/" + packagePath));
        
        // 生成示例服务类
        generateExampleService(servicePath);
    }

    private void generatePom(Path projectRoot) throws IOException {
        try (PrintWriter writer = new PrintWriter(projectRoot.resolve("pom.xml").toFile())) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"");
            writer.println("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            writer.println("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
            writer.println("    <modelVersion>4.0.0</modelVersion>");
            writer.println("    <parent>");
            writer.println("        <groupId>org.springframework.boot</groupId>");
            writer.println("        <artifactId>spring-boot-starter-parent</artifactId>");
            writer.println("        <version>" + springBootVersion + "</version>");
            writer.println("        <relativePath/>");
            writer.println("    </parent>");
            writer.println();
            writer.println("    <groupId>" + groupId + "</groupId>");
            writer.println("    <artifactId>" + artifactId + "</artifactId>");
            writer.println("    <version>" + version + "</version>");
            writer.println();
            writer.println("    <properties>");
            writer.println("        <java.version>17</java.version>");
            writer.println("        <spring-ai.version>" + springAiVersion + "</spring-ai.version>");
            writer.println("    </properties>");
            writer.println();
            writer.println("    <dependencyManagement>");
            writer.println("        <dependencies>");
            writer.println("            <dependency>");
            writer.println("                <groupId>org.springframework.ai</groupId>");
            writer.println("                <artifactId>spring-ai-bom</artifactId>");
            writer.println("                <version>${spring-ai.version}</version>");
            writer.println("                <type>pom</type>");
            writer.println("                <scope>import</scope>");
            writer.println("            </dependency>");
            writer.println("        </dependencies>");
            writer.println("    </dependencyManagement>");
            writer.println();
            writer.println("    <repositories>");
            writer.println("        <repository>");
            writer.println("            <id>spring-milestones</id>");
            writer.println("            <name>Spring Milestones</name>");
            writer.println("            <url>https://repo.spring.io/milestone</url>");
            writer.println("            <snapshots>");
            writer.println("                <enabled>false</enabled>");
            writer.println("            </snapshots>");
            writer.println("        </repository>");
            writer.println("        <repository>");
            writer.println("            <id>spring-snapshots</id>");
            writer.println("            <name>Spring Snapshots</name>");
            writer.println("            <url>https://repo.spring.io/snapshot</url>");
            writer.println("            <releases>");
            writer.println("                <enabled>false</enabled>");
            writer.println("            </releases>");
            writer.println("        </repository>");
            writer.println("    </repositories>");
            writer.println();
            writer.println("    <dependencies>");
            writer.println("        <!-- Spring Boot Dependencies -->");
            writer.println("        <dependency>");
            writer.println("            <groupId>org.springframework.boot</groupId>");
            writer.println("            <artifactId>spring-boot-starter</artifactId>");
            writer.println("            <exclusions>");
            writer.println("                <exclusion>");
            writer.println("                    <artifactId>spring-boot-starter-logging</artifactId>");
            writer.println("                    <groupId>org.springframework.boot</groupId>");
            writer.println("                </exclusion>");
            writer.println("            </exclusions>");
            writer.println("        </dependency>");
            writer.println();
            writer.println("        <!-- Spring AI Dependencies -->");
            writer.println("        <dependency>");
            writer.println("            <groupId>org.springframework.ai</groupId>");
            writer.println("            <artifactId>spring-ai-core</artifactId>");
            writer.println("        </dependency>");
            writer.println("        <dependency>");
            writer.println("            <groupId>org.springframework.ai</groupId>");
            writer.println("            <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>");
            writer.println("        </dependency>");
            writer.println();
            writer.println("        <!-- Web Dependencies -->");
            writer.println("        <dependency>");
            writer.println("            <groupId>org.springframework.boot</groupId>");
            writer.println("            <artifactId>spring-boot-starter-web</artifactId>");
            writer.println("        </dependency>");
            writer.println("        <dependency>");
            writer.println("            <groupId>org.springframework.boot</groupId>");
            writer.println("            <artifactId>spring-boot-starter-log4j2</artifactId>");
            writer.println("        </dependency>");
            writer.println();
            writer.println("        <!-- MCP Facade Generator -->");
            writer.println("        <dependency>");
            writer.println("            <groupId>com.unionhole</groupId>");
            writer.println("            <artifactId>mcp-facade-generator</artifactId>");
            writer.println("            <version>1.0.1</version>");
            writer.println("        </dependency>");
            writer.println("    </dependencies>");
            writer.println();
            writer.println("    <build>");
            writer.println("        <plugins>");
            writer.println("            <plugin>");
            writer.println("                <groupId>org.springframework.boot</groupId>");
            writer.println("                <artifactId>spring-boot-maven-plugin</artifactId>");
            writer.println("            </plugin>");
            writer.println("            <plugin>");
            writer.println("                <groupId>org.apache.maven.plugins</groupId>");
            writer.println("                <artifactId>maven-compiler-plugin</artifactId>");
            writer.println("                <version>3.8.1</version>");
            writer.println("                <configuration>");
            writer.println("                    <source>${java.version}</source>");
            writer.println("                    <target>${java.version}</target>");
            writer.println("                    <annotationProcessors>");
            writer.println("                         <annotationProcessor>com.unionhole.mcp.processor.MCPFacadeProcessor</annotationProcessor>");
            writer.println("                    </annotationProcessors>");
            writer.println("                    <compilerArgs>");
            writer.println("                         <arg>-Amcp.demo.output=false</arg>");
            writer.println("                    </compilerArgs>");
            writer.println("                </configuration>");
            writer.println("            </plugin>");
            writer.println("        </plugins>");
            writer.println("    </build>");
            writer.println("</project>");
        }
    }

    private void generateReadme(Path projectRoot) throws IOException {
        try (PrintWriter writer = new PrintWriter(projectRoot.resolve("README.md").toFile())) {
            writer.println("# MCP Demo Project");
            writer.println();
            writer.println("这是一个使用 Spring Boot 和 Spring AI MCP 的示例项目，展示了如何集成和使用 MCP 功能。");
            writer.println();
            writer.println("## 项目结构");
            writer.println();
            writer.println("```");
            writer.println("mcp-demo/");
            writer.println("├── pom.xml                 # POM 文件");
            writer.println("├── README.md               # 本文档");
            writer.println("└── src/");
            writer.println("    ├── main/");
            writer.println("    │   ├── java/");
            writer.println("    │   │   └── com/demo/");
            writer.println("    │   │       ├── McpDemoApplication.java    # 应用入口");
            writer.println("    │   │       ├── service/");
            writer.println("    │   │       │   └── WeatherService.java    # 示例服务");
            writer.println("    │   │       └── config/");
            writer.println("    │   │           └── McpServerConfig.java   # MCP 配置");
            writer.println("    │   └── resources/");
            writer.println("    │       └── application.properties         # 应用配置");
            writer.println("    └── test/");
            writer.println("        └── java/");
            writer.println("            └── com/demo/");
            writer.println("                └── ClientSseTest.java         # 测试客户端");
            writer.println("```");
            writer.println();
            writer.println("## 快速开始");
            writer.println();
            writer.println("### 1. 环境要求");
            writer.println("- JDK 17 或更高版本");
            writer.println("- Maven 3.6 或更高版本");
            writer.println();
            writer.println("### 2. 构建项目");
            writer.println("```bash");
            writer.println("mvn clean install");
            writer.println("```");
            writer.println();
            writer.println("### 3. 运行应用");
            writer.println("```bash");
            writer.println("cd mcp-demo-launcher");
            writer.println("mvn spring-boot:run");
            writer.println("```");
            writer.println();
            writer.println("## 框架说明");
            writer.println();
            writer.println("### 1. Maven 依赖配置");
            writer.println();
            writer.println("项目使用了以下主要依赖：");
            writer.println("```xml");
            writer.println("<dependencies>");
            writer.println("    <!-- Spring AI Core -->");
            writer.println("    <dependency>");
            writer.println("        <groupId>org.springframework.ai</groupId>");
            writer.println("        <artifactId>spring-ai-core</artifactId>");
            writer.println("        <version>${spring-ai.version}</version>");
            writer.println("    </dependency>");
            writer.println();
            writer.println("    <!-- MCP Server WebMVC -->");
            writer.println("    <dependency>");
            writer.println("        <groupId>org.springframework.ai</groupId>");
            writer.println("        <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>");
            writer.println("        <version>${spring-ai.version}</version>");
            writer.println("    </dependency>");
            writer.println("    <!-- 添加 MCP Facade 生成器依赖 -->");
            writer.println("    <dependency>");
            writer.println("        <groupId>com.unionhole</groupId>");
            writer.println("        <artifactId>mcp-facade-generator</artifactId>");
            writer.println("        <version>${mcp-facade.version}</version>");
            writer.println("    </dependency>");

            writer.println("</dependencies>");
            writer.println("```");
            writer.println();
            writer.println("### 2. MCP 配置说明");
            writer.println();
            writer.println("在 `application.properties` 中配置 MCP 服务器：");
            writer.println("```properties");
            writer.println("# MCP 服务器配置");
            writer.println("spring.ai.mcp.server.enabled=true");
            writer.println("spring.ai.mcp.server.resource-change-notification=true");
            writer.println("spring.ai.mcp.server.prompt-change-notification=true");
            writer.println("spring.ai.mcp.server.tool-change-notification=true");
            writer.println("spring.ai.mcp.server.name=mcp-demo-service");
            writer.println("spring.ai.mcp.server.version=1.0.0");
            writer.println("spring.ai.mcp.server.type=SYNC");
            writer.println("spring.ai.mcp.server.sse-message-endpoint=/mcp/messages");
            writer.println("```");
            writer.println();
            writer.println("### 3. MCP Tools 配置");
            writer.println();
            writer.println("通过 `McpServerConfig` 类配置 MCP Tools：");
            writer.println("```java");
            writer.println("@Configuration");
            writer.println("public class McpServerConfig {");
            writer.println("    @Bean");
            writer.println("    public ToolCallbackProvider autoRegisterTools(ApplicationContext applicationContext) {");
            writer.println("        // 获取所有带有 @Component 注解且类名以 Facade 结尾的 bean");
            writer.println("        String[] beanNames = applicationContext.getBeanNamesForAnnotation(Component.class);");
            writer.println("        List<Object> facadeBeans = new ArrayList<>();");
            writer.println("        for (String beanName : beanNames) {");
            writer.println("            if (beanName.endsWith(\"Facade\")) {");
            writer.println("                facadeBeans.add(applicationContext.getBean(beanName));");
            writer.println("            }");
            writer.println("        }");
            writer.println("        return MethodToolCallbackProvider.builder()");
            writer.println("                .toolObjects(facadeBeans.toArray())");
            writer.println("                .build();");
            writer.println("    }");
            writer.println("}");
            writer.println("```");
            writer.println();
            writer.println("### 4. 业务服务开发");
            writer.println();
            writer.println("1. 创建服务类并添加 `@MCPService` 注解：");
            writer.println("```java");
            writer.println("@MCPService");
            writer.println("@Service");
            writer.println("public class WeatherService {");
            writer.println("    public String getWeather(String cityName) {");
            writer.println("        // 实现业务逻辑");
            writer.println("        return \"Weather info for \" + cityName;");
            writer.println("    }");
            writer.println("}");
            writer.println("```");
            writer.println();
            writer.println("2. 使用 `@MCPMethod` 注解标记需要暴露的方法：");
            writer.println("```java");
            writer.println("@MCPMethod(description = \"获取天气信息\")");
            writer.println("public String getWeather(String cityName) {");
            writer.println("    // 方法实现");
            writer.println("}");
            writer.println("```");
            writer.println();
            writer.println("### 5. 客户端测试");
            writer.println();
            writer.println("使用提供的 `ClientSseTest` 类进行测试：");
            writer.println("```java");
            writer.println("public class ClientSseTest {");
            writer.println("    public static void main(String[] args) {");
            writer.println("        var transport = new HttpClientSseClientTransport(\"http://localhost:8080\");");
            writer.println("        var client = McpClient.sync(transport).build();");
            writer.println();
            writer.println("        try {");
            writer.println("            client.initialize();");
            writer.println("            client.ping();");
            writer.println();
            writer.println("            // 列出可用的工具");
            writer.println("            McpSchema.ListToolsResult toolsList = client.listTools();");
            writer.println("            System.out.println(\"Available Tools = \" + toolsList);");
            writer.println();
            writer.println("            client.closeGracefully();");
            writer.println("        } catch (Exception e) {");
            writer.println("            e.printStackTrace();");
            writer.println("        }");
            writer.println("    }");
            writer.println("}");
            writer.println("```");
            writer.println();
            writer.println("## 注意事项");
            writer.println();
            writer.println("1. 确保正确配置了 Spring AI 和 MCP 的版本");
            writer.println("2. 所有 Facade 类都应该使用 `@Component` 注解");
            writer.println("3. 建议使用 `@MCPMethod` 注解来提供方法的描述信息");
            writer.println("4. 异常处理应该在服务层统一处理");
            writer.println();
            writer.println("## 常见问题解决");
            writer.println();
            writer.println("### 1. 编译时出现 IllegalArgumentException");
            writer.println();
            writer.println("如果在编译过程中遇到以下错误：");
            writer.println("```");
            writer.println("java: java.lang.IllegalArgumentException");
            writer.println("```");
            writer.println();
            writer.println("**解决方案：**");
            writer.println();
            writer.println("在 IntelliJ IDEA 中添加 VM 选项：");
            writer.println();
            writer.println("1. 打开 Settings (Ctrl + Alt + S)");
            writer.println("2. 导航到 Build, Execution, Deployment > Compiler");
            writer.println("3. 在 \"Build process VM options\" 字段中添加：");
            writer.println("   ```");
            writer.println("   -Djps.track.ap.dependencies=false");
            writer.println("   ```");
            writer.println("4. 点击 Apply 和 OK");
            writer.println("5. 重新构建项目");
            writer.println();
            writer.println("## 参考文档");
            writer.println();
            writer.println("- [Spring AI 文档](https://docs.spring.io/spring-ai/reference/index.html)");
            writer.println("- [MCP Facade Generator](https://github.com/James-Zou/mcp-facade-generator)");
            writer.println();
        }
    }

    private void generateMainClass(Path srcPath) throws IOException {
        Path mainClassPath = srcPath.resolve("McpDemoApplication.java");
        try (PrintWriter writer = new PrintWriter(mainClassPath.toFile())) {
            writer.println("package " + groupId + ";");
            writer.println();
            writer.println("import org.springframework.boot.SpringApplication;");
            writer.println("import org.springframework.boot.autoconfigure.SpringBootApplication;");
            writer.println();
            writer.println("@SpringBootApplication");
            writer.println("public class McpDemoApplication {");
            writer.println("    public static void main(String[] args) {");
            writer.println("        SpringApplication.run(McpDemoApplication.class, args);");
            writer.println("    }");
            writer.println("}");
        }
    }

    private void generateConfigClass(Path srcPath) throws IOException {
        Path configPath = srcPath.resolve("config");
        Files.createDirectories(configPath);
        
        try (PrintWriter writer = new PrintWriter(configPath.resolve("McpServerConfig.java").toFile())) {
            writer.println("package " + groupId + ".config;");
            writer.println();
            writer.println("import org.springframework.ai.tool.method.MethodToolCallbackProvider;");
            writer.println("import org.springframework.ai.tool.ToolCallbackProvider;");
            writer.println("import org.springframework.context.ApplicationContext;");
            writer.println("import org.springframework.context.annotation.Bean;");
            writer.println("import org.springframework.context.annotation.Configuration;");
            writer.println("import org.springframework.stereotype.Component;");
            writer.println("import java.util.ArrayList;");
            writer.println("import java.util.List;");
            writer.println();
            writer.println("@Configuration");
            writer.println("public class McpServerConfig {");
            writer.println("    @Bean");
            writer.println("    public ToolCallbackProvider autoRegisterTools(ApplicationContext applicationContext) {");
            writer.println("        String[] beanNames = applicationContext.getBeanNamesForAnnotation(Component.class);");
            writer.println("        List<Object> facadeBeans = new ArrayList<>();");
            writer.println("        for (String beanName : beanNames) {");
            writer.println("            if (beanName.endsWith(\"Facade\")) {");
            writer.println("                facadeBeans.add(applicationContext.getBean(beanName));");
            writer.println("            }");
            writer.println("        }");
            writer.println("        return MethodToolCallbackProvider.builder()");
            writer.println("                .toolObjects(facadeBeans.toArray())");
            writer.println("                .build();");
            writer.println("    }");
            writer.println("}");
        }
    }

    private void generateApplicationProperties(Path resourcesPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(resourcesPath.resolve("application.properties").toFile())) {
            writer.println("server.port=8080");
            writer.println("spring.application.name=mcp-demo");
            writer.println("spring.main.banner-mode=off");
            writer.println();
            writer.println("# MCP Configuration");
            writer.println("spring.ai.mcp.server.enabled=true");
            writer.println("spring.ai.mcp.server.resource-change-notification=true");
            writer.println("spring.ai.mcp.server.prompt-change-notification=true");
            writer.println("spring.ai.mcp.server.tool-change-notification=true");
            writer.println("spring.ai.mcp.server.name=mcp-demo-service");
            writer.println("spring.ai.mcp.server.version=1.0.0");
            writer.println("spring.ai.mcp.server.type=SYNC");
            writer.println("spring.ai.mcp.server.sse-message-endpoint=/mcp/messages");
        }
    }

    private void generateTestClass(Path testPath) throws IOException {
        Files.createDirectories(testPath);
        
        try (PrintWriter writer = new PrintWriter(testPath.resolve("ClientSseTest.java").toFile())) {
            writer.println("package " + groupId + ";");
            writer.println();
            writer.println("import io.modelcontextprotocol.client.McpClient;");
            writer.println("import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;");
            writer.println("import io.modelcontextprotocol.spec.McpSchema;");
            writer.println("import java.util.Map;");
            writer.println();
            writer.println("public class ClientSseTest {");
            writer.println("    public static void main(String[] args) {");
            writer.println("        var transport = new HttpClientSseClientTransport(\"http://localhost:8080\");");
            writer.println("        var client = McpClient.sync(transport).build();");
            writer.println();
            writer.println("        try {");
            writer.println("            client.initialize();");
            writer.println("            client.ping();");
            writer.println();
            writer.println("            // List available tools");
            writer.println("            McpSchema.ListToolsResult toolsList = client.listTools();");
            writer.println("            System.out.println(\"Available Tools = \" + toolsList);");
            writer.println();
            writer.println("            client.closeGracefully();");
            writer.println("        } catch (Exception e) {");
            writer.println("            e.printStackTrace();");
            writer.println("        }");
            writer.println("    }");
            writer.println("}");
        }
    }

    private void generateExampleService(Path servicePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(servicePath.resolve("WeatherService.java").toFile())) {
            writer.println("package " + groupId + ".service;");
            writer.println();
            writer.println("import com.unionhole.mcp.annotation.MCPService;");
            writer.println("import com.unionhole.mcp.annotation.MCPMethod;");
            writer.println("import org.springframework.stereotype.Service;");
            writer.println();
            writer.println("@MCPService(packageName = \"" + groupId + ".mcp\")");
            writer.println("@Service");
            writer.println("public class WeatherService {");
            writer.println();
            writer.println("    /**");
            writer.println("     * Get weather information by city name");
            writer.println("     * @param cityName The name of the city");
            writer.println("     * @return Weather information for the specified city");
            writer.println("     */");
            writer.println("    @MCPMethod(description = \"Get weather information for a specific city\")");
            writer.println("    public String getWeather(String cityName) {");
            writer.println("        // This is a demo implementation");
            writer.println("        return \"The weather in \" + cityName + \" is sunny with a temperature of 25°C\";");
            writer.println("    }");
            writer.println("}");
        }
    }
} 