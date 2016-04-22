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

package com.digitalpetri.opcua.sdk.client.api.nodes.attached;

import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;

public interface UaViewNode extends UaNode {

    /**
     * Read the ContainsNoLoops attribute {@link DataValue}.
     *
     * @return the ContainsNoLoops attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readContainsNoLoops();

    /**
     * Read the ContainsNoLoops attribute value.
     *
     * @return the ContainsNoLoops attribute value.
     */
    default CompletableFuture<Boolean> readContainsNoLoopsAttribute() {
        return readContainsNoLoops().thenApply(v -> (Boolean) v.getValue().getValue());
    }

    /**
     * Read the EventNotifier attribute {@link DataValue}.
     *
     * @return the EventNotifier attribute {@link DataValue}.
     */
    CompletableFuture<DataValue> readEventNotifier();

    /**
     * Read the EventNotifier attribute value.
     *
     * @return the EventNotifier attribute value.
     */
    default CompletableFuture<UByte> readEventNotifierAttribute() {
        return readEventNotifier().thenApply(v -> (UByte) v.getValue().getValue());
    }

    /**
     * Write a {@link DataValue} to the ContainsNoLoops attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeContainsNoLoops(DataValue value);

    /**
     * Write a {@link Boolean} to the ContainsNoLoops attribute.
     *
     * @param containsNoLoops the {@link Boolean} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeContainsNoLoopsAttribute(Boolean containsNoLoops) {
        return writeContainsNoLoops(new DataValue(new Variant(containsNoLoops)));
    }

    /**
     * Write a {@link DataValue} to the EventNotifier attribute.
     *
     * @param value the {@link DataValue} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    CompletableFuture<StatusCode> writeEventNotifier(DataValue value);

    /**
     * Write a {@link UByte} to the EventNotifier attribute.
     *
     * @param eventNotifier the {@link UByte} to write.
     * @return the {@link StatusCode} of the write operation.
     */
    default CompletableFuture<StatusCode> writeEventNotifierAttribute(UByte eventNotifier) {
        return writeEventNotifier(new DataValue(new Variant(eventNotifier)));
    }
    
}
