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

package com.digitalpetri.opcua.sdk.client.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.stack.core.types.structured.AddNodesItem;
import com.digitalpetri.opcua.stack.core.types.structured.AddNodesResponse;
import com.digitalpetri.opcua.stack.core.types.structured.AddReferencesItem;
import com.digitalpetri.opcua.stack.core.types.structured.AddReferencesResponse;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteNodesItem;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteNodesResponse;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteReferencesItem;
import com.digitalpetri.opcua.stack.core.types.structured.DeleteReferencesResponse;

public interface NodeManagementServices {

    CompletableFuture<AddNodesResponse> addNodes(List<AddNodesItem> nodesToAdd);

    CompletableFuture<AddReferencesResponse> addReferences(List<AddReferencesItem> referencesToAdd);

    CompletableFuture<DeleteNodesResponse> deleteNodes(List<DeleteNodesItem> nodesToDelete);

    CompletableFuture<DeleteReferencesResponse> deleteReferences(List<DeleteReferencesItem> referencesToDelete);

}
