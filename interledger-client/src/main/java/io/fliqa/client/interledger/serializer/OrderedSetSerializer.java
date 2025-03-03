package io.fliqa.client.interledger.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
