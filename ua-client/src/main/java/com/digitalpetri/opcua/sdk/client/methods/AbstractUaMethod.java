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

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.api.UaClient;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.structured.CallMethodRequest;

public abstract class AbstractUaMethod {

    private final UaClient client;
    private final NodeId objectId;
    private final NodeId methodId;

    public AbstractUaMethod(UaClient client, NodeId objectId, NodeId methodId) {
        this.client = client;
        this.objectId = objectId;
        this.methodId = methodId;
    }

    public CompletableFuture<Variant[]> invoke(Variant[] inputArguments) {
        CallMethodRequest request = new CallMethodRequest(
                objectId, methodId, inputArguments);

        return client.call(request).thenCompose(result -> {
            StatusCode statusCode = result.getStatusCode();

            if (statusCode.isGood()) {
                Variant[] outputArguments = result.getOutputArguments();

                return CompletableFuture.completedFuture(outputArguments);
            } else {
                UaMethodException ex = new UaMethodException(
                        statusCode,
                        result.getInputArgumentResults(),
                        result.getInputArgumentDiagnosticInfos()
                );

                CompletableFuture<Variant[]> f = new CompletableFuture<>();
                f.completeExceptionally(ex);
                return f;
            }
        });
    }

}
