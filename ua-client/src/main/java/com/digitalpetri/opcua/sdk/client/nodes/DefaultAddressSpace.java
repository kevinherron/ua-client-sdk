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

package com.digitalpetri.opcua.sdk.client.nodes;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.nodes.AddressSpace;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaDataTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaMethodNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaObjectNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaObjectTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaReferenceTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaViewNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedDataTypeNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedMethodNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedObjectNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedObjectTypeNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedReferenceTypeNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedVariableNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedVariableTypeNode;
import com.digitalpetri.opcua.sdk.client.nodes.attached.AttachedViewNode;
import com.digitalpetri.opcua.stack.core.AttributeId;
import com.digitalpetri.opcua.stack.core.UaException;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;
import com.digitalpetri.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.digitalpetri.opcua.stack.core.types.structured.ReadResponse;
import com.digitalpetri.opcua.stack.core.types.structured.ReadValueId;

import static com.google.common.collect.Lists.newArrayList;

public class DefaultAddressSpace implements AddressSpace {

    private final OpcUaClient client;

    public DefaultAddressSpace(OpcUaClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<UaNode> getNode(NodeId nodeId) {
        ReadValueId readValueId = new ReadValueId(
                nodeId, AttributeId.NodeClass.uid(), null, QualifiedName.NULL_VALUE);

        CompletableFuture<ReadResponse> future =
                client.read(0.0, TimestampsToReturn.Neither, newArrayList(readValueId));

        return future.thenCompose(response -> {
            DataValue value = response.getResults()[0];
            NodeClass nodeClass = (NodeClass) value.getValue().getValue();

            if (nodeClass != null) {
                client.getNodeCache().putAttribute(nodeId, AttributeId.NodeClass, value);

                return CompletableFuture.completedFuture(createNode(nodeId, nodeClass));
            } else {
                return failedFuture(new UaException(value.getStatusCode(), "NodeClass was null"));
            }
        });
    }

    @Override
    public UaDataTypeNode getDataTypeNode(NodeId nodeId) {
        return new AttachedDataTypeNode(client, nodeId);
    }

    @Override
    public UaMethodNode getMethodNode(NodeId nodeId) {
        return new AttachedMethodNode(client, nodeId);
    }

    @Override
    public UaObjectNode getObjectNode(NodeId nodeId) {
        return new AttachedObjectNode(client, nodeId);
    }

    @Override
    public UaObjectTypeNode getObjectTypeNode(NodeId nodeId) {
        return new AttachedObjectTypeNode(client, nodeId);
    }

    @Override
    public UaReferenceTypeNode getReferenceTypeNode(NodeId nodeId) {
        return new AttachedReferenceTypeNode(client, nodeId);
    }

    @Override
    public UaVariableNode getVariableNode(NodeId nodeId) {
        return new AttachedVariableNode(client, nodeId);
    }

    @Override
    public UaVariableTypeNode getVariableTypeNode(NodeId nodeId) {
        return new AttachedVariableTypeNode(client, nodeId);
    }

    @Override
    public UaViewNode getViewNode(NodeId nodeId) {
        return new AttachedViewNode(client, nodeId);
    }

    private UaNode createNode(NodeId nodeId, NodeClass nodeClass) {
        switch (nodeClass) {
            case DataType:
                return new AttachedDataTypeNode(client, nodeId);
            case Method:
                return new AttachedMethodNode(client, nodeId);
            case Object:
                return new AttachedObjectNode(client, nodeId);
            case ObjectType:
                return new AttachedObjectTypeNode(client, nodeId);
            case ReferenceType:
                return new AttachedReferenceTypeNode(client, nodeId);
            case Variable:
                return new AttachedVariableNode(client, nodeId);
            case VariableType:
                return new AttachedVariableTypeNode(client, nodeId);
            case View:
                return new AttachedViewNode(client, nodeId);
            default:
                throw new IllegalStateException("unhandled NodeClass: " + nodeClass);
        }
    }


    private static <T> CompletableFuture<T> failedFuture(StatusCode statusCode) {
        return failedFuture(new UaException(statusCode));
    }

    private static <T> CompletableFuture<T> failedFuture(UaException exception) {
        CompletableFuture<T> f = new CompletableFuture<>();
        f.completeExceptionally(exception);
        return f;
    }

}
