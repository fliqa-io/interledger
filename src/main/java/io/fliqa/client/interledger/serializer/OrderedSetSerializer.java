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
package io.fliqa.client.interledger.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A custom serializer for {@link Set} collections that serializes the elements
 * in a deterministic order based on their string representations.
 * <p>
 * The {@code OrderedSetSerializer} ensures that the serialization output
 * for a {@link Set} is consistent irrespective of the initial ordering
 * of the elements within the set. It accomplishes this by converting each
 * element in the set to its {@code toString()} representation, sorting these
 * string representations in natural order, and then writing them as a JSON array.
 * <p>
 * This serializer is useful in scenarios where consistent JSON output is
 * required, such as when generating hashes, for debugging purposes, or for
 * API clients requiring deterministic outputs for identical data sets.
 *
 * @param <T> the type of elements contained in the set to be serialized
 * @see JsonSerializer
 */
public class OrderedSetSerializer<T> extends JsonSerializer<Set<T>> {

    @Override
    public void serialize(Set<T> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();

        // order item by toString() value
        List<String> list = value.stream().map(Object::toString).sorted().toList();
        for (String item : list) {
            gen.writeString(item);
        }

        gen.writeEndArray();
    }
}
