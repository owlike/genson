package com.owlike.genson.ext.jsr353;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import com.owlike.genson.stream.JsonReader;

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
        // TODO hum will do that latter, looks like an extreme case...
        return null;
    }

    @Override public JsonParser createParser(JsonArray array) {
        // TODO hum will do that latter, looks like an extreme case...
        return null;
    }

    @Override public Map<String, ?> getConfigInUse() {
        Map<String, Boolean> config = new HashMap<String, Boolean>();
        config.put(GensonJsonParser.STRICT_DOUBLE_PARSE, true);
        return null;
    }

}
