package com.owlike.genson.ext.jsr353;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import static javax.json.stream.JsonParser.Event.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

public class GensonJsonReaderFactory implements javax.json.JsonReaderFactory {
    private final GensonJsonParserFactory parserFactory;
    private final GensonJsonBuilderFactory builderFactory;

    public GensonJsonReaderFactory() {
        this(Collections.<String, Object>emptyMap());
    }

    public GensonJsonReaderFactory(Map<String, ?> config) {
        parserFactory = new GensonJsonParserFactory(config);
        builderFactory = new GensonJsonBuilderFactory();
    }

    @Override
    public JsonReader createReader(final Reader reader) {
        return new JsonReader() {
            private final JsonParser parser = parserFactory.createParser(reader);
            private boolean readed = false;

            @Override
            public JsonStructure read() {
                checkNotReadedAndRead();

                if (parser.hasNext()) {
                    Event evt = parser.next();
                    if (START_OBJECT == evt) {
                        return read(builderFactory.createObjectBuilder()).build();
                    } else if (START_ARRAY == evt) {
                        return read(builderFactory.createArrayBuilder()).build();
                    } else throw new JsonException("Expected START_OBJECT or START_ARRAY but got " + evt);
                }

                throw new JsonException("Empty stream");
            }

            @Override
            public JsonObject readObject() {
                checkNotReadedAndRead();

                if (parser.hasNext()) {
                    Event evt = parser.next();
                    if (START_OBJECT == evt) {
                        return read(builderFactory.createObjectBuilder()).build();
                    } else throw new JsonException("Expected " + START_OBJECT + " but got " + evt);
                }

                throw new JsonException("Empty stream");
            }

            @Override
            public JsonArray readArray() {
                checkNotReadedAndRead();

                if (parser.hasNext()) {
                    Event evt = parser.next();
                    if (START_ARRAY == evt) {
                        return read(builderFactory.createArrayBuilder()).build();
                    } else throw new JsonException("Expected " + START_ARRAY + " but got " + evt);
                }

                throw new JsonException("Empty stream");
            }

            private JsonArrayBuilder read(JsonArrayBuilder arrayBuilder) {
                while (parser.hasNext()) {
                    Event evt = parser.next();
                    switch (evt) {
                        case VALUE_STRING:
                            arrayBuilder.add(parser.getString());
                            break;
                        case VALUE_NUMBER:
                            if (parser.isIntegralNumber()) arrayBuilder.add(parser.getLong());
                            else arrayBuilder.add(parser.getBigDecimal());
                            break;
                        case VALUE_NULL:
                            arrayBuilder.addNull();
                            break;
                        case VALUE_FALSE:
                            arrayBuilder.add(JsonValue.FALSE);
                            break;
                        case VALUE_TRUE:
                            arrayBuilder.add(JsonValue.TRUE);
                            break;
                        case START_OBJECT:
                            arrayBuilder.add(
                                    read(builderFactory.createObjectBuilder())
                            );
                            break;
                        case START_ARRAY:
                            arrayBuilder.add(
                                    read(builderFactory.createArrayBuilder())
                            );
                            break;
                        case END_ARRAY:
                            return arrayBuilder;
                        default:
                            throw new JsonException("Unexpected event " + evt);
                    }
                }

                throw new IllegalStateException();
            }

            private JsonObjectBuilder read(JsonObjectBuilder objectBuilder) {
                String name = null;

                while (parser.hasNext()) {
                    Event evt = parser.next();
                    switch (evt) {
                        case KEY_NAME:
                            name = parser.getString();
                            break;
                        case VALUE_STRING:
                            objectBuilder.add(name, parser.getString());
                            break;
                        case VALUE_NUMBER:
                            if (parser.isIntegralNumber()) objectBuilder.add(name, parser.getLong());
                            else objectBuilder.add(name, parser.getBigDecimal());
                            break;
                        case VALUE_NULL:
                            objectBuilder.addNull(name);
                            break;
                        case VALUE_FALSE:
                            objectBuilder.add(name, JsonValue.FALSE);
                            break;
                        case VALUE_TRUE:
                            objectBuilder.add(name, JsonValue.TRUE);
                            break;
                        case START_OBJECT:
                            objectBuilder.add(
                                    name, read(builderFactory.createObjectBuilder())
                            );
                            break;
                        case START_ARRAY:
                            objectBuilder.add(
                                    name, read(builderFactory.createArrayBuilder())
                            );
                            break;
                        case END_OBJECT:
                            return objectBuilder;
                        default:
                            throw new JsonException("Unknown Event " + evt);
                    }
                }

                throw new IllegalStateException();
            }

            @Override
            public void close() {
                parser.close();
            }

            private void checkNotReadedAndRead() {
                if (readed) throw new IllegalStateException();
                readed = true;
            }
        };
    }

    @Override
    public JsonReader createReader(InputStream in) {
        try {
            return createReader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new JsonException("Charset UTF-8 is not supported.", e);
        }
    }

    @Override
    public JsonReader createReader(InputStream in, Charset charset) {
        return createReader(new InputStreamReader(in, charset));
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return parserFactory.getConfigInUse();
    }
}
