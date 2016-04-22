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

package com.digitalpetri.opcua.sdk.client.nodes.attached;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaMethodNode;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;

public class AttachedMethodNode extends AttachedNode implements UaMethodNode {

    public AttachedMethodNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    @Override
    public CompletableFuture<DataValue> readExecutable() {
        return readAttribute(AttributeId.Executable);
    }

    @Override
    public CompletableFuture<DataValue> readUserExecutable() {
        return readAttribute(AttributeId.UserExecutable);
    }

    @Override
    public CompletableFuture<StatusCode> writeExecutable(DataValue value) {
        return writeAttribute(AttributeId.Executable, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeUserExecutable(DataValue value) {
        return writeAttribute(AttributeId.UserExecutable, value);
    }

}
