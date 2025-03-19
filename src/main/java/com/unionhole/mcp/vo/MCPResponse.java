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
package com.unionhole.mcp.vo;
/**
 * MCP Response wrapper class
 *
 * @author James Zou
 * @version 1.0.0
 * @since 2024/03/19
 */
public class MCPResponse {
    private final boolean success;
    private final Object data;
    private final String message;
    private final String code;

    private MCPResponse(boolean success, Object data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public static MCPResponse success(Object data) {
        return new MCPResponse(true, data, "success", "200");
    }

    public static MCPResponse error(String message) {
        return new MCPResponse(false, null, message, "500");
    }

    public static MCPResponse error(String message, String code) {
        return new MCPResponse(false, null, message, code);
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
} 