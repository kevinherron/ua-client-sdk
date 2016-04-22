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

package com.digitalpetri.opcua.sdk.client.api.nodes.detached;

import java.util.Optional;

import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.NodeClass;

public interface UaDetachedNode {

    /**
     * See OPC-UA Part 3, section 5.2.2.
     *
     * @return the NodeId ({@link NodeId}) of this node.
     */
    NodeId getNodeId();

    /**
     * See OPC-UA Part 3, section 5.2.3.
     *
     * @return the NodeClass ({@link NodeClass}) of this node.
     */
    NodeClass getNodeClass();

    /**
     * See OPC-UA Part 3, section 5.2.4.
     *
     * @return the BrowseName ({@link QualifiedName}) of this node.
     */
    QualifiedName getBrowseName();

    /**
     * See OPC-UA Part 3, section 5.2.5.
     *
     * @return the DisplayName ({@link QualifiedName}) of this node.
     */
    LocalizedText getDisplayName();

    /**
     * See OPC-UA Part 3, section 5.2.6.
     *
     * @return if this attribute is present, the Description ({@link LocalizedText}).
     */
    Optional<LocalizedText> getDescription();

    /**
     * See OPC-UA Part 3, section 5.2.7.
     *
     * @return if this attribute is present, the WriteMask ({@link UInteger}).
     */
    Optional<UInteger> getWriteMask();

    /**
     * See OPC-UA Part 3, section 5.2.8.
     *
     * @return if this attribute is present, the UserWriteMask ({@link UInteger}).
     */
    Optional<UInteger> getUserWriteMask();

}
