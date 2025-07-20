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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fliqa.client.interledger.utils.Assert;

/**
 * Represents an interaction reference for the GNAP protocol.
 * 
 * <p>This class contains a reference to a completed user interaction that
 * can be used to continue the grant process. The interaction reference is
 * typically provided by the authorization server after a user has completed
 * the required interaction steps, such as authentication or consent.
 * 
 * <p>The interaction reference serves as a proof that the user interaction
 * has been completed successfully and can be used by the client to continue
 * the access grant process without requiring the user to repeat the
 * interaction.
 * 
 * <p>This is commonly used in scenarios where the client needs to continue
 * a grant request after the user has completed an interaction in a separate
 * context, such as a web browser or mobile app.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessInteract
 * @see InteractContinue
 * @see InteractFinish
 */
public class InteractRef {

    /**
     * The interaction reference token from the authorization server.
     * 
     * <p>This reference token is provided by the authorization server after
     * the user has completed the required interaction. It serves as proof
     * that the interaction was completed successfully and can be used to
     * continue the grant process.
     * 
     * <p>The interaction reference is typically a unique, opaque identifier
     * that the authorization server can use to associate the client's
     * continuation request with the completed user interaction.
     * 
     * <p>Example: "4IFWWIKYBC2PQ6U56NL1"
     */
    @JsonProperty("interact_ref")
    String interactRef;

    /**
     * Creates a new InteractRef instance with the specified interaction reference.
     * 
     * <p>This factory method provides a convenient way to create an interaction
     * reference object with the specified reference token. The reference token
     * is typically obtained from the authorization server after the user has
     * completed the required interaction.
     * 
     * @param interactRef the interaction reference token from the authorization server
     * @return a new InteractRef instance with the specified reference
     * @throws IllegalArgumentException if interactRef is null or empty
     */
    public static InteractRef build(String interactRef) {
        Assert.notNullOrEmpty(interactRef, "interactRef cannot be null or empty.");
        InteractRef ref = new InteractRef();
        ref.interactRef = interactRef;
        return ref;
    }
}
