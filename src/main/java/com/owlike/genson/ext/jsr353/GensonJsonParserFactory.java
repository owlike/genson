package com.owlike.genson.ext.jsr353;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import com.owlike.genson.stream.JsonReader;
import com.owlike.genson.stream.JsonWriter;

public class GensonJsonParserFactory implements JsonParserFactory {
    private final boolean strictDoubleParse;

    public GensonJsonParserFactory() {
        strictDoubleParse = false;
    }

    public GensonJsonParserFactory(Map<String, ?> config) {
        strictDoubleParse = JSR353Bundle.toBoolean(config, GensonJsonParser.STRICT_DOUBLE_PARSE);
    }

    @Override public JsonParser createParser(Reader reader) {
        return new GensonJsonParser(new JsonReader(reader, strictDoubleParse, false));
    }

    @Override public JsonParser createParser(InputStream in) {
        return new GensonJsonParser(new JsonReader(new InputStreamReader(in), strictDoubleParse, false));
    }

    @Override public JsonParser createParser(InputStream in, Charset charset) {
        return new GensonJsonParser(new JsonReader(new InputStreamReader(in, charset), strictDoubleParse, false));
    }

    @Override public JsonParser createParser(JsonObject obj) {
        return parserForJsonStructure(obj);
    }

    @Override public JsonParser createParser(JsonArray array) {
        return parserForJsonStructure(array);
    }

    // TODO: OK I know pretty slow to do that, but what a pain to also implement this...will do it latter
    private JsonParser parserForJsonStructure(JsonStructure jsonStructure) {
        StringWriter sw = new StringWriter();
        GensonJsonGenerator generator = new GensonJsonGenerator(new JsonWriter(sw));
        generator.write(jsonStructure);
        generator.flush();

        return createParser(new StringReader(sw.toString()));
    }

    @Override public Map<String, ?> getConfigInUse() {
        Map<String, Boolean> config = new HashMap<String, Boolean>();
        config.put(GensonJsonParser.STRICT_DOUBLE_PARSE, true);
        return config;
    }

}
