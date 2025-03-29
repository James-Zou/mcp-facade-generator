<div align="left">

# MCP Facade Generator

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/org.example/mcp-facade-generator.svg)](https://search.maven.org/search?q=g:com.unionhole.mcp:mcp-facade-generator)
[![Java Support](https://img.shields.io/badge/Java-17+-green.svg)](https://openjdk.java.net/)
[![GitHub Release](https://img.shields.io/github/v/release/james-zou/mcp-facade-generator)](https://github.com/james-zou/mcp-facade-generator/releases)

- 🚀**一个强大的 MCP 协议 Facade 生成器，支持自动生成 MCP 协议接口实现**
- 🔌**不用关心 MCP server的开发过程，快速将现有业务接口接入 MCP 协议**
- 🚀**支持一键生成springboot+MCP demo工程**
## 目录

- [特性](#特性)
- [快速开始](#快速开始)
- [使用说明](#使用说明)
- [配置选项](#配置选项)
- [示例](#示例)
- [API 文档](#api-文档)
- [贡献指南](#贡献指南)
- [版本历史](#版本历史)
- [许可证](#许可证)

## ✨ 特性

- 🛠️ **自动生成** - 编译时自动生成 MCP Facade 实现
- 🎯 **简单集成** - 仅需添加依赖和注解即可使用
- 🔌 **灵活扩展** - 支持自定义包名和方法描述
- 📝 **注释继承** - 自动继承原始服务的方法注释
- 🛡️ **异常处理** - 统一的异常处理机制
- 🔄 **类型转换** - 完整的参数类型转换支持

## 快速开始

### 1、引入Maven 依赖 
``` java
<dependency>
    <groupId>com.unionhole</groupId>
    <artifactId>mcp-facade-generator</artifactId>
    <version>1.0.1</version>
    <scope>provided</scope>
</dependency>
```

### 2、引入编译插件
``` java
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
       <source>8</source>
       <target>8</target>
       <annotationProcessors>
          <annotationProcessor>com.unionhole.mcp.processor.MCPFacadeProcessor</annotationProcessor>
       </annotationProcessors>
    </configuration>
</plugin>
```
### 3、在服务类上添加注解
``` java
@MCPService(packageName = "com.example.demo.mcp")
```
### 4、在服务方法上添加注释
``` java
例如下面注释"Get weather information by city name"
/**
* Get weather information by city name
* @return
  */
  public String getWeather(String cityName) {
  // Implementation
  return  null;
  }

```
### 5、注解说明
``` xml
#### @MCPService
用于标记需要生成 Facade 的服务类。

参数：
- `value`: 服务名称（可选）
- `packageName`: 生成的 Facade 类的包名（可选）

#### @Tool
用于标记 Facade 方法的描述信息。

参数：
- `description`: 方法描述

#### @MCPMethod

用于标记需要在 Facade 中生成的方法。如果服务类中的方法没有此注解，在 1.0.1 版本后将不会被生成到 Facade 中。

参数：
- `description`：方法描述，将用于生成 @Tool 注解的描述（可选，默认使用方法的 JavaDoc）
```
### Demo 项目生成

从 1.0.1 版本开始，支持在编译时自动生成一个完整的示例项目。默认情况下，demo 项目生成功能是禁用的。你可以通过以下两种方式启用 demo 项目生成：

#### 方式一：Maven 编译器参数配置（推荐）

在使用 mcp-facade-generator 的项目的 pom.xml 中添加以下配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.unionhole</groupId>
                        <artifactId>mcp-facade-generator</artifactId>
                        <version>1.0.1</version>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-Amcp.demo.output=true</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 方式二：Spring Boot 配置文件 + Maven 配置（不推荐）

1. 在 `application.properties` 中添加配置：
```properties
# 设置为 true 启用 demo 项目生成
mcp.demo.output=true
```

或者在 `application.yml` 中：
```yaml
mcp:
  demo:
    output: true
```

2. 在 pom.xml 中添加配置（必需）：
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.unionhole</groupId>
                        <artifactId>mcp-facade-generator</artifactId>
                        <version>1.0.1</version>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-AmcpConfigFile=${project.basedir}/src/main/resources/application.properties</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **重要说明：**
> 1. 必须在 Maven 编译器插件中配置 `annotationProcessorPaths`，否则注解处理器不会被激活
> 2. 使用方式二时，配置文件必须在编译时存在，且路径必须正确
> 3. 推荐使用方式一，直接通过编译器参数指定配置，更加可靠
> 4. 如果同时使用了两种配置方式，编译器参数的优先级更高
> 5. 当启用 demo 项目生成时，项目将自动生成在当前工程的 `demo` 目录下

生成的 Demo 项目包含：
- 完整的项目结构
- Spring Boot + Spring AI MCP 配置
- 示例服务和 Facade 类
- 可运行的测试用例

### 生成的 Demo 项目验证

生成完成后，你可以：

1. 进入生成的 demo 项目目录
2. 执行 Maven 命令进行测试：
```bash
cd /path/to/demo/project
mvn clean test
```

如果测试通过，说明 demo 项目生成成功。

### 生成规则

- 会为所有 public 方法生成对应的 Facade 方法
- 方法参数会被转换为 MCPRequest
- 返回值会被包装在 MCPResponse 中
- 异常会被统一处理并转换为错误响应

### demo示例
``` java
package com.example.demo.service;


import com.unionhole.mcp.annotation.MCPService;
import org.springframework.stereotype.Service;

/**
 * @author roderick.zou
 * @Description:
 * @date 2025/3/19 5:05 PM
 */
@MCPService(packageName = "com.example.demo.mcp")
@Service
public class WeatherService {
    /**
     * Get weather information by city name
     * @return
     */
    public String getWeather(String cityName) {
        // Implementation
        return  null;
    }

    /**
     * Get weather information by city name1
     * @return
     */
    public String getWeather1(String cityName) {
        // Implementation
        return  null;
    }
}
```
### 生成示例
``` java
package com.example.demo.mcp;

import com.unionhole.mcp.vo.MCPRequest;
import com.unionhole.mcp.vo.MCPResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import com.example.demo.service.WeatherService;

/**
 * Auto-generated MCP Facade class for WeatherService
 *
 * @author James Zou
 * @version 1.0.0
 * @since 2025-03-19
 */
public class WeatherServiceFacade {
    private final WeatherService service;

    public WeatherServiceFacade(WeatherService service) {
        this.service = service;
    }

    @Tool(description = "Get weather information by city name")
    public MCPResponse getWeather(MCPRequest request) {
        try {
            // 解析请求参数
            java.lang.String cityName = request.getParameter("cityName", java.lang.String.class);
            Object result = service.getWeather(cityName);
            return MCPResponse.success(result);
        } catch (Exception e) {
            return MCPResponse.error(e.getMessage());
        }
    }

    @Tool(description = "Get weather information by city name1")
    public MCPResponse getWeather1(MCPRequest request) {
        try {
            // 解析请求参数
            java.lang.String cityName = request.getParameter("cityName", java.lang.String.class);
            Object result = service.getWeather1(cityName);
            return MCPResponse.success(result);
        } catch (Exception e) {
            return MCPResponse.error(e.getMessage());
        }
    }

}

```

## 🔄 版本历史

### v1.0.1 (2024-03-28)
- 优化 Facade 生成逻辑，移除 MCPRequest/MCPResponse 包装
- 增加 @MCPMethod 注解支持，实现方法级别的生成控制
- 新增 Demo 项目生成功能，支持通过配置自动生成示例工程
- 改进异常处理机制，直接抛出原始异常
- 优化文档和注释生成

### v1.0.0 (2024-03-19)
- ✨ 初始版本发布
- 🎉 支持基本的 Facade 生成功能
- 💪 支持 JDK 17

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交改动：`git commit -am 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 👥 维护者

- James Zou ([@james-zou](https://github.com/james-zou))

## 🙏 鸣谢

感谢所有为这个项目做出贡献的开发者！

---

## 环境要求

### Java 版本要求
- JDK 17 或更高版本
- 确保 JAVA_HOME 环境变量正确设置
- 确保 Maven 使用正确的 JDK 版本

### 版本检查
1. 检查 Java 版本：
```bash
java -version
```
应显示 17 或更高版本

2. 检查 Maven 使用的 Java 版本：
```bash
mvn -v
```
确保显示的 Java 版本是 17 或更高版本

### 常见问题解决

1. 如果遇到 "无效的目标发行版：17.x.x" 错误：

   a. 检查并设置 JAVA_HOME：
   ```bash
   # Windows
   echo %JAVA_HOME%
   # Linux/Mac
   echo $JAVA_HOME
   ```

   b. 确保 JAVA_HOME 指向 JDK 17 安装目录

   c. 在项目的 pom.xml 中明确指定编译器版本：
   ```xml
   <properties>
       <java.version>17</java.version>
       <maven.compiler.source>${java.version}</maven.compiler.source>
       <maven.compiler.target>${java.version}</maven.compiler.target>
   </properties>

   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-compiler-plugin</artifactId>
               <version>3.11.0</version>
               <configuration>
                   <source>${java.version}</source>
                   <target>${java.version}</target>
                   <encoding>UTF-8</encoding>
                   <annotationProcessors>
                       <annotationProcessor>com.unionhole.mcp.processor.MCPFacadeProcessor</annotationProcessor>
                   </annotationProcessors>
               </configuration>
           </plugin>
       </plugins>
   </build>
   ```

2. 如果使用 IDE（如 IntelliJ IDEA）：
   - 确保项目结构设置（Project Structure）中的 JDK 版本为 17
   - 确保 Maven 设置中使用的 JDK 版本为 17
   - 刷新 Maven 项目配置

3. 对于 Windows 用户：
   - 检查系统环境变量中是否正确设置 JAVA_HOME
   - 确保 Path 变量包含 %JAVA_HOME%\bin

4. 对于 Linux/Mac 用户：
   - 可以使用 sdkman 管理 Java 版本：
   ```bash
   # 安装 sdkman
   curl -s "https://get.sdkman.io" | bash
   # 安装 JDK 17
   sdk install java 17.0.x-zulu
   # 设置默认版本
   sdk default java 17.0.x-zulu
   ```

**[⬆ 返回顶部](#mcp-facade-generator)**

如果这个项目对你有帮助，请给一个 ⭐️！

</div>


