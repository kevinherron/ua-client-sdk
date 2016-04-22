/*
 * Copyright 2016 Kevin Herron
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

package com.digitalpetri.opcua.sdk.client.methods;

import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.DiagnosticInfo;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;

public class UaMethodException extends UaException {

    private final StatusCode[] inputArgumentResults;
    private final DiagnosticInfo[] inputArgumentDiagnostics;

    public UaMethodException(StatusCode statusCode,
                             StatusCode[] inputArgumentResults,
                             DiagnosticInfo[] inputArgumentDiagnostics) {
        super(statusCode);

        this.inputArgumentResults = inputArgumentResults;
        this.inputArgumentDiagnostics = inputArgumentDiagnostics;
    }

    public UaMethodException(long statusCode,
                             StatusCode[] inputArgumentResults,
                             DiagnosticInfo[] inputArgumentDiagnostics) {
        super(statusCode);

        this.inputArgumentResults = inputArgumentResults;
        this.inputArgumentDiagnostics = inputArgumentDiagnostics;
    }

    public StatusCode[] getInputArgumentResults() {
        return inputArgumentResults;
    }

    public DiagnosticInfo[] getInputArgumentDiagnostics() {
        return inputArgumentDiagnostics;
    }
    
}
