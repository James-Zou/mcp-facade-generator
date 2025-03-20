<div align="left">

# MCP Facade Generator

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/org.example/mcp-facade-generator.svg)](https://search.maven.org/search?q=g:com.unionhole.mcp:mcp-facade-generator)
[![Java Support](https://img.shields.io/badge/Java-17+-green.svg)](https://openjdk.java.net/)
[![GitHub Release](https://img.shields.io/github/v/release/james-zou/mcp-facade-generator)](https://github.com/james-zou/mcp-facade-generator/releases)

- 🚀**一个强大的 MCP 协议 Facade 生成器，支持自动生成 MCP 协议接口实现**
- 🔌**不用关心 MCP server的开发过程，快速将现有业务接口接入 MCP 协议**
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
    <groupId>com.unionhole.mcp</groupId>
    <artifactId>mcp-facade-generator</artifactId>
    <version>1.0.0</version>
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


```

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



**[⬆ 返回顶部](#mcp-facade-generator)**

如果这个项目对你有帮助，请给一个 ⭐️！

</div>


