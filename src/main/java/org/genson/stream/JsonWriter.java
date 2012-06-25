package org.genson.stream;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonWriter implements ObjectWriter {
	private final Writer writer;
	private final boolean htmlSafe;

	protected final static char BEGIN_ARRAY = '[';
	protected final static char END_ARRAY = ']';
	protected final static char BEGIN_OBJECT = '{';
	protected final static char END_OBJECT = '}';
	protected final static char QUOTE = '"';
	protected final static char VALUE_BEGIN = '\"';
	protected final static char VALUE_END = '\"';

	protected final static char VALUE_SEPARATOR = ',';
	protected final static char NAME_SEPARATOR = ':';
	/*
	 * Recupere dans gson
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
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
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
	private final int _LIMIT_WRITE_TO_BUFFER = 64;

	private final Deque<JsonType> _ctx = new ArrayDeque<JsonType>();
	private boolean _hasPrevious;
	private String _name;
	private final boolean skipNull;

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

	void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		flushBuffer();
		writer.flush();
	}

	public JsonWriter beginArray() throws IOException {
		return begin(JsonType.ARRAY, BEGIN_ARRAY);
	}

	public JsonWriter beginObject() throws IOException {
		return begin(JsonType.OBJECT, BEGIN_OBJECT);
	}

	protected final JsonWriter begin(final JsonType jsonType, final char token) throws IOException {
		separateAndFlush();
		lazyName();
		_ctx.push(jsonType);
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = token;
		_hasPrevious = false;
		return this;
	}

	public JsonWriter endArray() throws IOException {
		return end(JsonType.ARRAY, END_ARRAY);
	}

	public JsonWriter endObject() throws IOException {
		return end(JsonType.OBJECT, END_OBJECT);
	}

	private final JsonWriter end(final JsonType jsonType, final char token) throws IOException {
		JsonType jt = _ctx.pop();
		if (jt != jsonType)
			throw new IllegalStateException("No " + jsonType.name());
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = token;
		_hasPrevious = true;
		return this;
	}

	/**
	 * As a small optimization lets take into account the character ',' in the others methods
	 * calling this one, so we avoid a lot of microscopic flush...
	 */
	private final JsonWriter separate() throws IOException {
		if (_hasPrevious)
			_buffer[_len++] = VALUE_SEPARATOR;
		return this;
	}

	private final JsonWriter separateAndFlush() throws IOException {
		if (_ctx.peek() == JsonType.ARRAY && _hasPrevious) {
			if ((_len + 1) >= _bufferSize)
				flushBuffer();
			_buffer[_len++] = VALUE_SEPARATOR;
		}
		return this;
	}

	public JsonWriter writeName(final String name) throws IOException {
		_name = name;
		return this;
	}

	private final JsonWriter lazyName() throws IOException {
		if (_name != null && _ctx.peek() != JsonType.ARRAY) {
			final int l = _name.length();
			// hum I dont think there may be names with a length near to 1024... we flush only once
			if ((_len + 4 + l) >= _bufferSize)
				flushBuffer();
			separate();
			_buffer[_len++] = QUOTE;
			writeToBuffer(_name, 0, l);
			_buffer[_len++] = QUOTE;
			_buffer[_len++] = NAME_SEPARATOR;
			_name = null;
		}
		return this;
	}

	public JsonWriter writeValue(int value) throws IOException {
		separateAndFlush();
		lazyName();
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
		separateAndFlush();
		lazyName();
		writeToBuffer(Double.toString(value), 0);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(long value) throws IOException {
		separateAndFlush();
		lazyName();
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

	public JsonWriter writeValue(final boolean value) throws IOException {
		separateAndFlush();
		lazyName();
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
		separateAndFlush();
		lazyName();
		writeToBuffer(value.toString(), 0);
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeUnsafeValue(final String value) throws IOException {
		separateAndFlush();
		lazyName();
		writeToBuffer(value.toCharArray(), 0, value.length());
		_hasPrevious = true;
		return this;
	}

	public JsonWriter writeValue(final String value) throws IOException {
		separateAndFlush();

		lazyName();

		final String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		if ((_len + 1) >= _bufferSize)
			flushBuffer();
		_buffer[_len++] = VALUE_BEGIN;
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

		_buffer[_len++] = VALUE_END;

		_hasPrevious = true;

		return this;
	}

	@Override
	public ObjectWriter writeNull() throws IOException {
		if (skipNull) {
			_name = null;
		} else {
			separateAndFlush();
			lazyName();
			writeToBuffer(NULL_VALUE, 0, 4);
			_hasPrevious = true;
		}
		return this;
	}

	@Override
	public ObjectWriter metadata(String name, String value) throws IOException {
		if (_ctx.peek() != JsonType.OBJECT)
			throw new IllegalStateException(
					"Metadata is allowed in objects only and must be written first!");

		// again I dont think a name will reach 1024 so its not worth to handle that case
		if ((_len + 5 + name.length()) >= _bufferSize)
			flushBuffer();
		separate();
		_buffer[_len++] = QUOTE;
		_buffer[_len++] = '@';
		writeToBuffer(name, 0, name.length());
		_buffer[_len++] = QUOTE;
		_buffer[_len++] = NAME_SEPARATOR;

		writeValue(value);
		return this;
	}

	private final void writeToBuffer(final char[] data, final int offset, final int length) throws IOException {
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

	private final void writeToBuffer(final String data, final int offset, final int length) throws IOException {
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
