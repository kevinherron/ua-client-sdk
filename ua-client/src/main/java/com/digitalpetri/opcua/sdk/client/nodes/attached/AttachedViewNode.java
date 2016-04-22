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
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaViewNode;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;

public class AttachedViewNode extends AttachedNode implements UaViewNode {

    public AttachedViewNode(OpcUaClient client, NodeId nodeId) {
        super(client, nodeId);
    }

    @Override
    public CompletableFuture<DataValue> readContainsNoLoops() {
        return readAttribute(AttributeId.ContainsNoLoops);
    }

    @Override
    public CompletableFuture<DataValue> readEventNotifier() {
        return readAttribute(AttributeId.EventNotifier);
    }

    @Override
    public CompletableFuture<StatusCode> writeContainsNoLoops(DataValue value) {
        return writeAttribute(AttributeId.ContainsNoLoops, value);
    }

    @Override
    public CompletableFuture<StatusCode> writeEventNotifier(DataValue value) {
        return writeAttribute(AttributeId.EventNotifier, value);
    }
    
}
