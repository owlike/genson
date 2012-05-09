package org.likeit.transformation.stream;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

public class JsonWriter implements ObjectWriter {
	private final Writer writer;
	private final boolean htmlSafe;
	
	protected static final char BEGIN_ARRAY = '[';
	protected static final char END_ARRAY = ']';
	protected static final char BEGIN_OBJECT = '{';
	protected static final char END_OBJECT = '}';
	protected static final char NAME_BEGIN = '"';
	protected static final char NAME_END = '"';
	protected static final char VALUE_BEGIN = '\"';
	protected static final char VALUE_END = '\"';

	protected static final char VALUE_SEPARATOR = ',';
	protected static final char NAME_SEPARATOR = ':';
	protected final static String NULL_VALUE = "null";

	private final Stack<JsonType> _ctx;
	private boolean _hasPrevious;
	private String _name;
	
	private final boolean skipNull;
	
	private static final String[] REPLACEMENT_CHARS;
	private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
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
		HTML_SAFE_REPLACEMENT_CHARS['"'] = "&#034;";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "&#039;";//"\\u0027";
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "&lt;";//"\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "&gt;";//"\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "&amp;";//"\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
	}

	public JsonWriter(Writer writer) {
		this.writer = writer;
		this._ctx = new Stack<JsonType>();
		_ctx.push(JsonType.EMPTY);
		this._hasPrevious = false;
		this.skipNull = false;
		this.htmlSafe = false;
	}
	
	public JsonWriter(Writer writer, boolean skipNull, boolean htmlSafe) {
		this.writer = writer;
		this._ctx = new Stack<JsonType>();
		_ctx.push(JsonType.EMPTY);
		this._hasPrevious = false;
		this.skipNull = skipNull;
		this.htmlSafe = htmlSafe;
	}

	void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}
	
	public JsonWriter beginArray() throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		_ctx.push(JsonType.ARRAY);
		deferredName();
		writer.write(BEGIN_ARRAY);
		_hasPrevious = false;
		return this;
	}
	
	public JsonWriter endArray() throws IOException {
		JsonType jt = _ctx.pop();
		
		if ( jt != JsonType.ARRAY ) throw new IllegalStateException("No beginArray!");
		writer.write(END_ARRAY);
		_hasPrevious = true;
    	return this;
	}
	
	public JsonWriter beginObject() throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		_ctx.push(JsonType.OBJECT);
		deferredName();
		writer.write(BEGIN_OBJECT);
		_hasPrevious = false;
		return this;
	}
	
	protected JsonWriter separate() throws IOException {
		if ( _hasPrevious ) writer.write(VALUE_SEPARATOR);
		return this;
	}
	
	public JsonWriter endObject() throws IOException {
		JsonType jt = _ctx.pop();
		if ( jt != JsonType.OBJECT ) throw new IllegalStateException("No beginObject!");
		writer.write(END_OBJECT);
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter name(String name) throws IOException {
		_name = name;
		return this;
	}
	
	protected JsonWriter deferredName() throws IOException {
		if ( _name != null && _ctx.peek() != JsonType.ARRAY ) {
    		separate();
    		writer.write(NAME_BEGIN);
    		writer.write(_name);	
    		writer.write(NAME_END);
    		writer.write(NAME_SEPARATOR);
    		_name = null;
		}
		return this;
	}

	public JsonWriter value(int value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		deferredName();
		writer.write(Integer.toString(value));
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter value(double value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		deferredName();
		writer.write(Double.toString(value));
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter value(long value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		deferredName();
		writer.write(Long.toString(value));
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter value(boolean value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		deferredName();
		writer.write(Boolean.toString(value));
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter value(Number value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		deferredName();
		writer.write(value.toString());
		_hasPrevious = true;
		return this;
	}
	
	public JsonWriter value(String value) throws IOException {
		if ( _ctx.peek() == JsonType.ARRAY ) separate();
		
		deferredName();
		
		String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS
				: REPLACEMENT_CHARS;
	
		writer.write(VALUE_BEGIN);
		int last = 0;
		int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
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
				writer.write(value, last, i - last);
			}
			writer.write(replacement);
			last = i + 1;
		}
		if (last < length) {
			writer.write(value, last, length - last);
		}
		writer.write(VALUE_END);
	
		_hasPrevious = true;
		
		return this;
	}

	@Override
	public ObjectWriter valueNull() throws IOException {
		if ( skipNull ) {
			_name = null;
		} else {
			if ( _ctx.peek() == JsonType.ARRAY ) separate();
			deferredName();
			writer.write(NULL_VALUE);
			_hasPrevious = true;
		}
		return null;
	}
}

