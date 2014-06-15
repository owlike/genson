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
    prettyPrint = JSR353Bundle.toBoolean(config, JsonGenerator.PRETTY_PRINTING);
    htmlSafe = JSR353Bundle.toBoolean(config, GensonJsonGenerator.HTML_SAFE);
    skipNull = JSR353Bundle.toBoolean(config, GensonJsonGenerator.SKIP_NULL);
  }

  @Override
  public JsonGenerator createGenerator(Writer writer) {
    return new GensonJsonGenerator(new JsonWriter(writer, skipNull, htmlSafe, prettyPrint));
  }

  @Override
  public JsonGenerator createGenerator(OutputStream out) {
    try {
      return new GensonJsonGenerator(new JsonWriter(new OutputStreamWriter(out, "UTF-8"),
        skipNull, htmlSafe, prettyPrint));
    } catch (UnsupportedEncodingException e) {
      throw new JsonException("Charset UTF-8 is not supported.", e);
    }
  }

  @Override
  public JsonGenerator createGenerator(OutputStream out, Charset charset) {
    return new GensonJsonGenerator(new JsonWriter(new OutputStreamWriter(out), skipNull,
      htmlSafe, prettyPrint));
  }

  @Override
  public Map<String, ?> getConfigInUse() {
    Map<String, Boolean> config = new HashMap<String, Boolean>();
    config.put(JsonGenerator.PRETTY_PRINTING, prettyPrint);
    config.put(GensonJsonGenerator.HTML_SAFE, htmlSafe);
    config.put(GensonJsonGenerator.SKIP_NULL, skipNull);
    return config;
  }
}
