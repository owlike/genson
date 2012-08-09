package org.genson.stream;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonWriter implements ObjectWriter {
	/*
	 * TODO try to do something different and faster, optimize writeValue(String)
	 */
	private final static String[] REPLACEMENT_CHARS;
	private final static String[] HTML_SAFE_REPLACEMENT_CHARS;
	static {
		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
	}

	private final static char[] _INT_TO_CHARARRAY = new char[10];
	static {
		for (int i = 0; i < 10; i++) {
			_INT_TO_CHARARRAY[i] = (char) (i + 48);
		}
	}
	private final static char[] NULL_VALUE = { 'n', 'u', 'l', 'l' };
	private final static char[] TRUE_VALUE = { 't', 'r', 'u', 'e' };
	private final static char[] FALSE_VALUE = { 'f', 'a', 'l', 's', 'e' };
	// seems to work well, but maybe a smaller value would be better?
	private final static int _LIMIT_WRITE_TO_BUFFER = 64;

	private final boolean htmlSafe;
	private final boolean skipNull;

	private final Writer writer;
	private final Deque<JsonType> _ctx = new ArrayDeque<JsonType>();
	private boolean _hasPrevious;
	private String _name;

	// TODO recyclebuffer increases a bit performances
	private final char[] _buffer = new char[1024];
	private final int _bufferSize = _buffer.length;
	private int _len = 0;

	public JsonWriter(Writer writer) {
		this(writer, false, false);
	}

	public JsonWriter(Writer writer, final boolean skipNull, final boolean htmlSafe) {
		this.writer = writer;
		this.skipNull = skipNull;
		this.htmlSafe = htmlSafe;
		_ctx.push(JsonType.EMPTY);
	}

	public void close() throws IOException {
		flush();
		writer.close();
	}

	public void flush() throws IOException {
		flushBuffer();
		writer.flush();
	}

	public JsonWriter beginArray() throws IOException {
		return begin(JsonType.ARRAY, '[');
	}

	public JsonWriter beginObject() throws IOException {
		if (_ctx.peek() == JsonType.METADATA) {
			_ctx.pop();
			return this;
		}
		return begin(JsonType.OBJECT, '{');
	}

	protected final JsonWriter begin(final JsonType jsonType, final char token) throws IOException {
		beforeValue();
		_ctx.push(jsonType);
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = token;
		_hasPrevious = false;
		return this;
	}

	public JsonWriter endArray() throws IOException {
		return end(JsonType.ARRAY, ']');
	}

	public JsonWriter endObject() throws IOException {
		return end(JsonType.OBJECT, '}');
	}

	private final JsonWriter end(final JsonType jsonType, final char token) throws IOException {
		JsonType jt = _ctx.pop();
		if (jt != jsonType)
			throw new IllegalStateException("Expect type " + jsonType.name() + " but was written "
					+ jt.name() + ", you must call the adequate beginXXX method before endXXX.");
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = token;
		_hasPrevious = true;
		return this;
	}

	private final JsonWriter beforeValue() throws IOException {
		/*
		 * for the moment this does not work, however I should add some "state verifications" for
		 * metadata feature... otherwise it may be difficult to use and debug it...
		 */
		// if (_ctx.peek() == JsonType.METADATA)
		// throw new IllegalStateException(
		// "You have written metadata to the writer but did not call beginObject after it."
		// + "Metadata is not allowed in array or for literal values.");

		if (_ctx.peek() == JsonType.ARRAY && _hasPrevious) {
			if ((_len + 1) >= _bufferSize)
				flushBuffer();
			_buffer[_len++] = ',';
		}

		if (_name != null && _ctx.peek() != JsonType.ARRAY) {
			final int l = _name.length();
			// hum I dont think there may be names with a length near to 1024... we flush only once
			if ((_len + 4 + l) >= _bufferSize)
				flushBuffer();
			if (_hasPrevious)
				_buffer[_len++] = ',';
			_buffer[_len++] = '"';
			writeToBuffer(_name, 0, l);
			_buffer[_len++] = '"';
			_buffer[_len++] = ':';
			_name = null;
		}
		return this;
	}

	public JsonWriter writeName(final String name) throws IOException {
		_name = name;
		return this;
	}

	public JsonWriter writeValue(int value) throws IOException {
		beforeValue();
		// ok so the buffer must always be bigger than the max length of a long
		if ((_len + 11) >= _bufferSize)
			flushBuffer();
		if (value < 0) {
			_buffer[_len++] = '-';
			value *= -1;
		}
		writeInt(value);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(final double value) throws IOException {
		beforeValue();
		writeToBuffer(Double.toString(value), 0);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(long value) throws IOException {
		beforeValue();
		// ok so the buffer must always be bigger than the max length of a long
		if ((_len + 21) >= _bufferSize)
			flushBuffer();
		if (value < 0) {
			_buffer[_len++] = '-';
			value *= -1;
		}
		writeInt(value);
		_hasPrevious = true;
		return this;
	}
	
	public ObjectWriter writeValue(short value) throws IOException {
		beforeValue();
		// ok so the buffer must always be bigger than the max length of a short
		if ((_len + 5) >= _bufferSize)
			flushBuffer();
		if (value < 0) {
			_buffer[_len++] = '-';
			value *= -1;
		}
		writeInt(value);
		_hasPrevious = true;
		return this;
	}

	public ObjectWriter writeValue(float value) throws IOException {
		beforeValue();
		writeToBuffer(Float.toString(value), 0);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(final boolean value) throws IOException {
		beforeValue();
		if (value)
			writeToBuffer(TRUE_VALUE, 0, 4);
		else
			writeToBuffer(FALSE_VALUE, 0, 5);
		_hasPrevious = true;
		return this;
	}

	protected final int writeInt(long value) throws IOException {
		final int len = (int) Math.log10(value) + 1;
		if (value == 0) {
			_buffer[_len++] = '0';
			return 1;
		}

		int pos = _len + len - 1;
		long intPart;
		for (; value > 0;) {
			intPart = value / 10;
			_buffer[pos--] = _INT_TO_CHARARRAY[(int) (value - (intPart * 10))];
			value = intPart;
		}

		_len += len;
		return len;
	}

	public JsonWriter writeValue(final Number value) throws IOException {
		beforeValue();
		writeToBuffer(value.toString(), 0);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeUnsafeValue(final String value) throws IOException {
		beforeValue();
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = '"';
		writeToBuffer(value.toCharArray(), 0, value.length());
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = '"';
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(final String value) throws IOException {
		beforeValue();
		final String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = '"';
		int last = 0;
		final int length = value.length();
		final char[] carray = value.toCharArray();
		for (int i = 0; i < length; i++) {
			char c = carray[i];
			String replacement;
			if (c < 128) {
				replacement = replacements[c];
				if (replacement == null) {
					continue;
				}
			} else if (c == '\u2028') {
				replacement = "\\u2028";
			} else if (c == '\u2029') {
				replacement = "\\u2029";
			} else {
				continue;
			}
			if (last < i) {
				writeToBuffer(carray, last, i - last);
			}

			writeToBuffer(replacement, 0, replacement.length());
			last = i + 1;
		}
		if (last < length) {
			writeToBuffer(carray, last, length - last);
		}
		if ((_len + 1) >= _bufferSize)
			flushBuffer();

		_buffer[_len++] = '"';

		_hasPrevious = true;

		return this;
	}

	public ObjectWriter writeNull() throws IOException {
		if (skipNull) {
			_name = null;
		} else {
			beforeValue();
			writeToBuffer(NULL_VALUE, 0, 4);
			_hasPrevious = true;
		}
		return this;
	}

	public ObjectWriter beginNextObjectMetadata() throws IOException {
		// this way we can use this method multiple times in different converters before calling
		// beginObject
		if (_ctx.peek() != JsonType.METADATA) {
			beginObject();
			_ctx.push(JsonType.METADATA);
		}
		return this;
	}

	public ObjectWriter writeMetadata(String name, String value) throws IOException {
		writeName('@' + name);
		writeValue(value);
		return this;
	}

	private final void writeToBuffer(final char[] data, final int offset, final int length)
			throws IOException {
		if (length < _LIMIT_WRITE_TO_BUFFER && length < (_bufferSize - _len)) {
			System.arraycopy(data, offset, _buffer, _len, length);
			_len += length;
		} else {
			flushBuffer();
			writer.write(data, offset, length);
		}
	}

	private final void writeToBuffer(final String data, final int offset) throws IOException {
		writeToBuffer(data, offset, data.length());
	}

	private final void writeToBuffer(final String data, final int offset, final int length)
			throws IOException {
		if (length < _LIMIT_WRITE_TO_BUFFER && length < (_bufferSize - _len)) {
			data.getChars(offset, length, _buffer, _len);
			_len += length;
		} else {
			flushBuffer();
			writer.write(data, offset, length);
		}
	}

	private final void flushBuffer() throws IOException {
		if (_len > 0) {
			writer.write(_buffer, 0, _len);
			_len = 0;
		}
	}

	public Writer unwrap() {
		return writer;
	}
}
