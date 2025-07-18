/*
 * Copyright 2025 Fliqa
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
package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.OrderedSetSerializer;

import java.net.URI;
import java.util.Set;

/**
 * Represents an access item that defines permissions for a specific resource type.
 * 
 * <p>An access item combines a resource type with a set of allowed actions to create
 * specific access permissions. Access items are used in access grants to define
 * what operations a client is authorized to perform on different types of payment
 * resources.
 * 
 * <p>The access item can optionally include limits to constrain the scope of
 * operations and specific identifiers to restrict access to particular resources.
 * 
 * @see AccessItemType
 * @see AccessAction
 * @see Limits
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
public class AccessItem {

    /**
     * The type of resource this access item applies to.
     * 
     * <p>Specifies whether this access item grants permissions for incoming
     * payments, outgoing payments, or quotes. The resource type determines
     * what kind of operations can be performed.
     */
    @JsonProperty(value = "type", required = true)
    public AccessItemType accessType;

    /**
     * The set of actions that are permitted for this resource type.
     * 
     * <p>Defines the specific operations that can be performed on the resource
     * type. Actions can include create, read, complete, list, and others
     * depending on the resource type and use case.
     * 
     * <p>Actions are serialized in a consistent order to ensure deterministic
     * JSON output.
     */
    @JsonProperty(value = "actions", required = true)
    @JsonSerialize(using = OrderedSetSerializer.class)
    public Set<AccessAction> actions;

    /**
     * Optional identifier that restricts access to a specific resource.
     * 
     * <p>When specified, this access item only applies to the resource
     * identified by this URI. This is commonly used for outgoing payments
     * where access should be limited to a specific payment resource.
     * 
     * <p>This field is required for outgoing payment access items and
     * optional for other resource types.
     */
    @JsonProperty("identifier")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI identifier;

    /**
     * Optional limits that constrain the scope of operations.
     * 
     * <p>Defines limits such as maximum amounts, specific receivers, or
     * time intervals that apply to operations performed using this access
     * item. Limits provide additional security and control over payment
     * operations.
     */
    @JsonProperty("limits")
    public Limits limits;

    /**
     * Configures this access item for outgoing payment operations.
     * 
     * <p>This convenience method sets up the access item with the specified
     * identifier and debit amount limit for outgoing payment operations.
     * It creates a new Limits object and configures it with the provided
     * debit amount.
     * 
     * @param identifier the specific outgoing payment resource identifier
     * @param debitAmount the maximum amount that can be debited
     * @return this AccessItem instance for method chaining
     */
    public AccessItem accessOutgoing(URI identifier, InterledgerAmount debitAmount) {
        this.identifier = identifier;
        limits = new Limits();
        limits.debitAmount = debitAmount;
        return this;
    }
}
