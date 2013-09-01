package com.owlike.genson.ext.jsr353;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.json.JsonException;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import com.owlike.genson.stream.JsonStreamException;
import com.owlike.genson.stream.JsonType;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ValueType;

import static com.owlike.genson.stream.ValueType.*;

public class GensonJsonParser implements JsonParser {
    public static final String STRICT_DOUBLE_PARSE = "GensonJsonParser.strictDoubleParse";

    private static final int NONE = 0, KEY = 1, VALUE = 2;

    private final ObjectReader reader;
    private int evtType = NONE;

    public GensonJsonParser(ObjectReader reader) {
        this.reader = reader;
    }

    @Override public boolean hasNext() {
        try {
            return reader.hasNext() || reader.enclosingType() != JsonType.EMPTY;
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    private Event currentValue(ValueType type) throws IOException {
        if (type == ARRAY) {
            reader.beginArray();
            return Event.START_ARRAY;
        } else if (type == OBJECT) {
            reader.beginObject();
            return Event.START_OBJECT;
        } else if (type == STRING) {
            return Event.VALUE_STRING;
        } else if (type == NULL) {
            return Event.VALUE_NULL;
        } else if (type == BOOLEAN) {
            return reader.valueAsBoolean() ? Event.VALUE_TRUE : Event.VALUE_FALSE;
        } else if (type == INTEGER || type == DOUBLE) { return Event.VALUE_NUMBER; }

        throw new JsonException("Unknown ValueType " + type);
    }

    @Override public Event next() {
        try {
            JsonType enclosingType = reader.enclosingType();

            // read the value of an object key/value pair
            if (evtType == KEY) {
                evtType = NONE;
                return currentValue(reader.getValueType());
            } else if (reader.hasNext()) {
                ValueType valueType = reader.next();

                // we are in an object make the pair and keep value evt for next call to next()
                if (enclosingType == JsonType.OBJECT) {
                    evtType = KEY;
                    return Event.KEY_NAME;
                } else {
                    // this means it is an array, then just read current value and dont care about
                    // the next evt
                    evtType = NONE;
                    return currentValue(valueType);
                }
            } else {
                evtType = NONE;
                if (enclosingType == JsonType.OBJECT) {
                    reader.endObject();
                    return Event.END_OBJECT;
                } else if (enclosingType == JsonType.ARRAY) {
                    reader.endArray();
                    return Event.END_ARRAY;
                }
                throw new JsonException("Reached end of stream, next should not be called.");
            }
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    @Override public String getString() {
        try {
            if (KEY == evtType) return reader.name();
            else return reader.valueAsString();
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    @Override public boolean isIntegralNumber() {
        return reader.getValueType() == INTEGER;
    }

    @Override public int getInt() {
        try {
            return reader.valueAsInt();
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    @Override public long getLong() {
        try {
            return reader.valueAsLong();
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    @Override public BigDecimal getBigDecimal() {
        // TODO
        try {
            return new BigDecimal(reader.valueAsString());
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
        catch (JsonStreamException e) {
            throw _wrapException(e);
        }
    }

    @Override public JsonLocation getLocation() {
        return new Location(reader.row(), reader.column());
    }

    @Override public void close() {
        try {
            reader.close();
        }
        catch (IOException e) {
            throw _wrapException(e);
        }
    }

    private JsonException _wrapException(Exception e) {
        JsonException newException = null;
        if (e instanceof JsonStreamException) {
            JsonStreamException jse = (JsonStreamException) e;
            newException =
                    new JsonParsingException(e.getMessage(), e, new Location(jse.getRow(),
                            jse.getColumn()));
        } else newException = new JsonException(e.getMessage(), e);

        return JsonStreamException.niceTrace(newException);
    }

    private class Location implements JsonLocation {
        final long lineNumber;
        final long columnNumber;

        public Location(long lineNumber, long columnNumber) {
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        @Override public long getStreamOffset() {
            return -1;
        }

        @Override public long getLineNumber() {
            return lineNumber;
        }

        @Override public long getColumnNumber() {
            return columnNumber;
        }
    }
}
