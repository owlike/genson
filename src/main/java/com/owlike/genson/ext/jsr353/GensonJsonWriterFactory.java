package com.owlike.genson.ext.jsr353;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

public class GensonJsonWriterFactory implements JsonWriterFactory {
  private final JsonGeneratorFactory generatorFactory;

  public GensonJsonWriterFactory() {
    this(Collections.<String, Object>emptyMap());
  }

  public GensonJsonWriterFactory(Map<String, ?> config) {
    this.generatorFactory = new GensonJsonGeneratorFactory(config);
  }

  @Override
  public JsonWriter createWriter(final Writer writer) {
    return new JsonWriter() {
      private final JsonGenerator generator = generatorFactory.createGenerator(writer);
      private boolean written = false;

      @Override
      public void writeArray(JsonArray array) {
        checkWritten();
        generator.write(array);
      }

      @Override
      public void writeObject(JsonObject object) {
        checkWritten();
        generator.write(object);
      }

      @Override
      public void write(JsonStructure value) {
        checkWritten();
        generator.write(value);
      }

      @Override
      public void close() {
        generator.close();
      }

      private void checkWritten() {
        if (written) throw new IllegalStateException();
        written = true;
      }
    };
  }

  @Override
  public JsonWriter createWriter(OutputStream out) {
    try {
      return createWriter(new OutputStreamWriter(out, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new JsonException("Charset UTF-8 is not supported.", e);
    }
  }

  @Override
  public JsonWriter createWriter(OutputStream out, Charset charset) {
    return createWriter(new OutputStreamWriter(out, charset));
  }

  @Override
  public Map<String, ?> getConfigInUse() {
    return generatorFactory.getConfigInUse();
  }
}
