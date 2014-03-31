package com.owlike.genson.ext.jsr353;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.Genson;
import com.owlike.genson.Genson.Builder;
import com.owlike.genson.TransformationException;
import com.owlike.genson.ext.GensonBundle;
import com.owlike.genson.stream.JsonWriter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

// TODO register a unknown type converter overriding the existing one, would allow to deser to 
// jsr structures instead of Map and Lists.
public class JSR353Bundle extends GensonBundle {
    static final JsonBuilderFactory factory = JsonProvider.provider().createBuilderFactory(
            new HashMap<String, String>());

    @Override public void configure(Builder builder) {
        builder.withConverterFactory(new Factory<Converter<JsonValue>>() {
            @Override public Converter<JsonValue> create(Type type, Genson genson) {
                return new JsonValueConverter();
            }
        });
    }

    public class JsonValueConverter implements Converter<JsonValue> {

        @Override public void serialize(JsonValue value, ObjectWriter writer, Context ctx) throws IOException {
            ValueType type = value.getValueType();
            if (ValueType.STRING == type) writer.writeValue(((JsonString) value).getString());
            else if (ValueType.ARRAY == type) writeArray((JsonArray) value, writer, ctx);
            else if (ValueType.OBJECT == type) writeObject((JsonObject) value, writer, ctx);
            else if (ValueType.NULL == type) writer.writeNull();
            else if (ValueType.NUMBER == type) {
                JsonNumber num = (JsonNumber) value;
                if (num.isIntegral()) writer.writeValue(num.longValue());
                else writer.writeValue(num.bigDecimalValue());
            } else if (ValueType.FALSE == type) writer.writeValue(false);
            else if (ValueType.TRUE == type) writer.writeValue(true);
            else {
                throw new IllegalStateException("Unknown ValueType " + type);
            }
        }

        private void writeArray(JsonArray array, ObjectWriter writer, Context ctx) throws IOException {
            writer.beginArray();
            for (JsonValue value : array)
                serialize(value, writer, ctx);
            writer.endArray();
        }

        private void writeObject(JsonObject object, ObjectWriter writer, Context ctx) throws IOException {
            writer.beginObject();
            for (Entry<String, JsonValue> e : object.entrySet()) {
                writer.writeName(e.getKey());
                serialize(e.getValue(), writer, ctx);
            }
            writer.endObject();
        }

        @Override public JsonValue deserialize(ObjectReader reader, Context ctx) throws IOException {
            com.owlike.genson.stream.ValueType type = reader.getValueType();
            if (com.owlike.genson.stream.ValueType.OBJECT == type) {
                return deserObject(reader, ctx);
            } else if (com.owlike.genson.stream.ValueType.ARRAY == type) {
                return deserArray(reader, ctx);
            } else {
                // let's allow using literal JsonValues outside of JsonArray or JsonObject
                // thus we need this dummy builder to not by pass the creation mechanism
                if (com.owlike.genson.stream.ValueType.STRING == type) {
                    return factory.createArrayBuilder().add(reader.valueAsString()).build().get(0);
                } else if (com.owlike.genson.stream.ValueType.BOOLEAN == type) {
                    return reader.valueAsBoolean() ? JsonValue.TRUE : JsonValue.FALSE;
                } else if (com.owlike.genson.stream.ValueType.NULL == type) {
                    return JsonValue.NULL;
                } else if (com.owlike.genson.stream.ValueType.INTEGER == type) {
                    return factory.createArrayBuilder().add(reader.valueAsLong()).build().get(0);
                } else if (com.owlike.genson.stream.ValueType.DOUBLE == type) {
                    return factory.createArrayBuilder().add(reader.valueAsDouble()).build().get(0);
                }
            }
            
            throw new IllegalStateException("Unsupported ValueType " + type);
        }

        public JsonValue deserObject(ObjectReader reader, Context ctx) throws IOException {
            JsonObjectBuilder builder = factory.createObjectBuilder();
            reader.beginObject();

            while (reader.hasNext()) {
                com.owlike.genson.stream.ValueType type = reader.next();
                String name = reader.name();
                if (com.owlike.genson.stream.ValueType.STRING == type) {
                    builder.add(name, reader.valueAsString());
                } else if (com.owlike.genson.stream.ValueType.BOOLEAN == type) {
                    builder.add(name, reader.valueAsBoolean());
                } else if (com.owlike.genson.stream.ValueType.NULL == type) {
                    builder.addNull(name);
                } else if (com.owlike.genson.stream.ValueType.INTEGER == type) {
                    builder.add(name, reader.valueAsLong());
                } else if (com.owlike.genson.stream.ValueType.DOUBLE == type) {
                    builder.add(name, reader.valueAsDouble());
                } else builder.add(name, deserialize(reader, ctx));
            }

            reader.endObject();
            return builder.build();
        }

        public JsonValue deserArray(ObjectReader reader, Context ctx) throws IOException{
            JsonArrayBuilder builder = factory.createArrayBuilder();
            reader.beginArray();

            while (reader.hasNext()) {
                com.owlike.genson.stream.ValueType type = reader.next();
                if (com.owlike.genson.stream.ValueType.STRING == type) {
                    builder.add(reader.valueAsString());
                } else if (com.owlike.genson.stream.ValueType.BOOLEAN == type) {
                    builder.add(reader.valueAsBoolean());
                } else if (com.owlike.genson.stream.ValueType.NULL == type) {
                    builder.addNull();
                } else if (com.owlike.genson.stream.ValueType.INTEGER == type) {
                    builder.add(reader.valueAsLong());
                } else if (com.owlike.genson.stream.ValueType.DOUBLE == type) {
                    builder.add(reader.valueAsDouble());
                } else builder.add(deserialize(reader, ctx));
            }
            
            reader.endArray();
            return builder.build();
        }
    }

    static String toString(JsonValue value) {
        StringWriter sw = new StringWriter();
        com.owlike.genson.stream.JsonWriter writer = new JsonWriter(sw);
        GensonJsonGenerator generator = new GensonJsonGenerator(writer);
        generator.write(value);
        generator.close();
        return sw.toString();
    }

    static boolean toBoolean(Map<String, ?> config, String key) {
        if (config == null) return false;

        if (config.containsKey(key)) {
            Object value = config.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            } else return false;
        } else return false;
    }
}
