package com.owlike.genson.ext.jsr353;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import com.owlike.genson.stream.JsonWriter;

public class GensonJsonGeneratorFactory implements JsonGeneratorFactory {
    private final boolean prettyPrint;
    private final boolean htmlSafe;
    private final boolean skipNull;

    public GensonJsonGeneratorFactory() {
        prettyPrint = false;
        htmlSafe = false;
        skipNull = false;
    }

    public GensonJsonGeneratorFactory(Map<String, ?> config) {
        prettyPrint = toBoolean(config, JsonGenerator.PRETTY_PRINTING);
        htmlSafe = toBoolean(config, GensonJsonGenerator.HTML_SAFE);
        skipNull = toBoolean(config, GensonJsonGenerator.SKIP_NULL);
    }

    @Override public JsonGenerator createGenerator(Writer writer) {
        return new GensonJsonGenerator(new JsonWriter(writer, skipNull, htmlSafe, prettyPrint));
    }

    @Override public JsonGenerator createGenerator(OutputStream out) {
        try {
            return new GensonJsonGenerator(new JsonWriter(new OutputStreamWriter(out, "UTF-8"),
                    skipNull, htmlSafe, prettyPrint));
        }
        catch (UnsupportedEncodingException e) {
            throw new JsonException("Charset UTF-8 is not supported.", e);
        }
    }

    @Override public JsonGenerator createGenerator(OutputStream out, Charset charset) {
        return new GensonJsonGenerator(new JsonWriter(new OutputStreamWriter(out), skipNull,
                htmlSafe, prettyPrint));
    }

    @Override public Map<String, ?> getConfigInUse() {
        Map<String, Boolean> config = new HashMap<String, Boolean>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        return config;
    }

    private boolean toBoolean(Map<String, ?> config, String key) {
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
