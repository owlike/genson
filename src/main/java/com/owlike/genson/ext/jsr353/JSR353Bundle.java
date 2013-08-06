package com.owlike.genson.ext.jsr353;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
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
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

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
        
        @Override public void serialize(JsonValue value, ObjectWriter writer, Context ctx)
                throws TransformationException, IOException {
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

        private void writeArray(JsonArray array, ObjectWriter writer, Context ctx) throws IOException, TransformationException {
            writer.beginArray();
            for (JsonValue value : array) serialize(value, writer, ctx);
            writer.endArray();
        }
        
        private void writeObject(JsonObject object, ObjectWriter writer, Context ctx) throws IOException, TransformationException {
            writer.beginObject();
            for (Entry<String, JsonValue> e : object.entrySet()) {
                writer.writeName(e.getKey());
                serialize(e.getValue(), writer, ctx);
            }
            writer.endObject();
        }

        @Override public JsonValue deserialize(ObjectReader reader, Context ctx)
                throws TransformationException, IOException {
            return null;
        }

    }
}
