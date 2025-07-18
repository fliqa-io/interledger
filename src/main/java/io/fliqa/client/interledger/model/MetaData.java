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

import java.util.Set;

/**
 * Represents metadata associated with Interledger payment operations.
 * 
 * <p>This class contains optional metadata that can be attached to payments
 * to provide additional context, references, or descriptive information.
 * Metadata is useful for tracking, reconciliation, and providing human-readable
 * information about payment transactions.
 * 
 * <p>All fields are optional and will be excluded from JSON serialization
 * when null, helping to keep the payload minimal.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
public class MetaData {

    /**
     * An external identifier for the payment.
     * 
     * <p>This field can be used to reference the payment in external systems
     * such as accounting systems, invoicing systems, or other business applications.
     * It provides a way to correlate Interledger payments with business transactions.
     * 
     * <p>Examples of external IDs include:
     * <ul>
     *   <li>Invoice numbers (e.g., "INV-2023-001")</li>
     *   <li>Order identifiers (e.g., "ORDER-12345")</li>
     *   <li>Transaction references (e.g., "TXN-ABC123")</li>
     * </ul>
     */
    @JsonProperty(value = "externalId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String externalId;

    /**
     * A set of key-value metadata items providing additional context.
     * 
     * <p>This field contains structured metadata as a collection of key-value pairs.
     * It allows for flexible, extensible metadata that can be customized based on
     * specific business requirements or use cases.
     * 
     * <p>Common use cases include:
     * <ul>
     *   <li>Payment descriptions and notes</li>
     *   <li>Customer or merchant information</li>
     *   <li>Transaction categorization</li>
     *   <li>Regulatory or compliance data</li>
     * </ul>
     * 
     * @see MetaDataItem
     */
    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<MetaDataItem> value;
}
