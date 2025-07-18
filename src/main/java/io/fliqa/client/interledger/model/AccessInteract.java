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

import java.util.List;

/**
 * Represents the interaction requirements for a GNAP access request.
 * 
 * <p>This class defines how a client can interact with the authorization server
 * to obtain consent from the resource owner. It specifies the interaction
 * methods that the client supports and how the authorization server should
 * communicate the results back to the client.
 * 
 * <p>The interaction flow in GNAP typically involves:
 * <ol>
 *   <li>The client specifies supported interaction methods (start)</li>
 *   <li>The authorization server chooses one and initiates user interaction</li>
 *   <li>The user completes the interaction (consent, authentication, etc.)</li>
 *   <li>The authorization server communicates the result back (finish)</li>
 * </ol>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see InteractFinish
 * @see GrantAccessRequest
 */
public class AccessInteract {

    /**
     * The interaction methods that the client supports.
     * 
     * <p>This list contains the interaction methods that the client is
     * capable of handling. Common interaction methods include:
     * <ul>
     *   <li>"redirect" - Browser-based redirect interaction</li>
     *   <li>"app" - Application-based interaction</li>
     *   <li>"device" - Device flow interaction</li>
     *   <li>"user_code" - User code-based interaction</li>
     * </ul>
     * 
     * <p>The authorization server will choose one of these methods to
     * initiate the interaction with the resource owner.
     * 
     * <p>Example: ["redirect", "app"]
     */
    @JsonProperty(value = "start", required = true)
    public List<String> start;

    /**
     * Optional specification for how the authorization server should communicate
     * the interaction result back to the client.
     * 
     * <p>This field defines the mechanism that the authorization server should
     * use to notify the client when the interaction is complete. If not
     * specified, the client may need to poll the authorization server or
     * use other means to determine when the interaction is finished.
     * 
     * @see InteractFinish
     */
    @JsonProperty("finish")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InteractFinish finish;
}
