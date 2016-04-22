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

package com.digitalpetri.opcua.sdk.client.api.nodes;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaDataTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaMethodNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaObjectNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaObjectTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaReferenceTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaVariableTypeNode;
import com.digitalpetri.opcua.sdk.client.api.nodes.attached.UaViewNode;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;

public interface AddressSpace {

    CompletableFuture<UaNode> getNode(NodeId nodeId);

    UaDataTypeNode getDataTypeNode(NodeId nodeId);

    UaMethodNode getMethodNode(NodeId nodeId);

    UaObjectNode getObjectNode(NodeId nodeId);

    UaObjectTypeNode getObjectTypeNode(NodeId nodeId);

    UaReferenceTypeNode getReferenceTypeNode(NodeId nodeId);

    UaVariableNode getVariableNode(NodeId nodeId);

    UaVariableTypeNode getVariableTypeNode(NodeId nodeId);

    UaViewNode getViewNode(NodeId nodeId);

}
