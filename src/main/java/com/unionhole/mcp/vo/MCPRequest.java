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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
/**
 * MCP Request wrapper class
 *
 * @author James Zou
 * @version 1.0.0
 * @since 2024/03/19
 */
public class MCPRequest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> parameters;

    public MCPRequest(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public <T> T getParameter(String name, Class<T> type) {
        Object value = parameters.get(name);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, type);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
} 