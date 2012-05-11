package org.likeit.transformation.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;

public class JsonReader implements ObjectReader {
	protected static final int BEGIN_ARRAY = '[';
	protected static final int END_ARRAY = ']';
	protected static final int BEGIN_OBJECT = '{';
	protected static final int END_OBJECT = '}';
	protected static final int QUOTE = '"';
	
	protected static final int VALUE_SEPARATOR = ',';
	protected static final int NAME_SEPARATOR = ':';
	protected final static String NULL_VALUE = "null";
	
	private static int[][] VALID_TOKENS = new int[JsonType.LAST.ordinal()][128];
	static {
		VALID_TOKENS[JsonType.ARRAY.ordinal()][BEGIN_ARRAY] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][END_ARRAY] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][BEGIN_OBJECT] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][END_OBJECT] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][QUOTE] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][VALUE_SEPARATOR] = 1;
		VALID_TOKENS[JsonType.ARRAY.ordinal()][END_OBJECT] = 1;
		
	}
	
	
	protected static final int[] SKIPPED_TOKENS;
	static {
		SKIPPED_TOKENS = new int[128];
		SKIPPED_TOKENS['\t'] = 1;
		SKIPPED_TOKENS['\b'] = 1;
		SKIPPED_TOKENS['\n'] = 1;
		SKIPPED_TOKENS['\r'] = 1;
		SKIPPED_TOKENS['\f'] = 1;
		SKIPPED_TOKENS[' '] = 1;
	}
	
	private final Reader reader;
	char[] buffer = new char[1024];
	private int position;
	private int cursor;
	private int buflen;
	private StringBuilder sb = new StringBuilder();
	private String currentName;
	private String currentValue;
	private boolean checkedNext = false;
	private boolean hasNext = false;
	private Deque<JsonType> _ctx = new ArrayDeque<JsonType>(16);
	{
		_ctx.push(JsonType.EMPTY);
	}

	public JsonReader(String source) {
		this(new StringReader(source));
	}
	
	public JsonReader(Reader reader) {
		this.reader = reader;
	}
	
	@Override
	public void beginArray() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( BEGIN_ARRAY == token ) {
			_ctx.push(JsonType.ARRAY);
		}
		next();
	}

	@Override
	public void beginObject() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( BEGIN_OBJECT == token ) {
			_ctx.push(JsonType.OBJECT);
		}
		next();
	}

	@Override
	public void endArray() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( END_ARRAY == token && JsonType.ARRAY == _ctx.peek() ) {
			_ctx.pop();
		}
	}

	@Override
	public void endObject() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( END_OBJECT == token && JsonType.OBJECT == _ctx.peek() ) {
			_ctx.pop();
		}
	}

	@Override
	public boolean hasNext() throws IOException {
		if ( checkedNext ) return hasNext;
		else {
			int token = readNextToken(true);
			checkIllegalEnd(token);
			return token == VALUE_SEPARATOR;
		}
	}

	@Override
	public String name() {
		return currentName;
	}
	
	@Override
	public String value() {
		return currentValue;
	}

	@Override
	public ObjectReader next() throws IOException {
		checkedNext = false;
		hasNext = false;
		
		if ( JsonType.OBJECT == _ctx.peek() ) {
			if ( readNextToken(false) == VALUE_SEPARATOR ) increment();
			consumeString();
			
			currentName = sb.toString();
			sb.setLength(0);
			
			if ( readNextToken(true) != NAME_SEPARATOR ) throw newMisplacedTokenException(cursor-1, position-1);
		}
		
		int token = readNextToken(false);
		if ( token == QUOTE ) {
			consumeString();
		} else if ( token > 47 && token < 58 ) consumeNumber();
		else consumeNull();
		
		currentValue = sb.toString();
		sb.setLength(0);
		
		return this;
	}

	// ne doit pas etre appele si c'est un tableau
	private void consumeString() throws IOException {
		if ( readNextToken(true) != QUOTE ) throw newMisplacedTokenException(cursor-1, position-1);
		
		while (cursor > -1 ) {
			if ( cursor == buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
			}

			int i = cursor;
			for ( ; i < buflen; i++ ) {
				if ( buffer[i] == '\\' ) i++; // skip le char suivant
				else if ( buffer[i] == QUOTE ) break;
			}
			
			sb.append(buffer, cursor, i-cursor);
			position += i - cursor + 1;
			cursor = i + 1;
			if ( buffer[i] == QUOTE ) return;
		}
	}
	
	private void consumeNumber() throws IOException {
		while (cursor > -1 ) {
			if ( cursor == buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
			}

			int i = cursor;
			for ( ; i < buflen; i++ ) {
				if ( buffer[i] < 48 || buffer[i] > 57 ) {
					sb.append(buffer, cursor, i);
					cursor = i;
					position += cursor;
					return;
				}
			}
		}
	}
	
	// TODO
	private void consumeNull() throws IOException {
		while (cursor > -1 ) {
			if ( cursor == buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
			}

			int i = cursor;
			for ( ; i < buflen; i++ ) {
				if ( buffer[i] < 48 || buffer[i] > 57 ) {
					sb.append(buffer, cursor, i);
					cursor = i;
					position += cursor;
					return;
				}
			}
		}
	}
	
	// TODO consume boolean
	protected int readNextToken(boolean consume) throws IOException {
		boolean stop = false;
		int oldCursor = cursor;
		
		while ( cursor > -1 ) {
			if ( cursor == buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
			}
			
			for ( ; cursor < buflen; cursor++ ) {
				if ( buffer[cursor] < 128 && SKIPPED_TOKENS[buffer[cursor]] == 0 ) {
					stop = true;
					break;
				}
			}
			
			// TODO attention c'est faux
			position += cursor - oldCursor;
			oldCursor = cursor - 1;
			
			if ( stop ) {
				if ( consume ) {
					cursor++;
					position++;
				}
				return buffer[cursor-1];
			}
		}
		
		return cursor;
	}
	
	protected void increment() {
		cursor++;
		position++;
	}
	
	private IllegalStateException newMisplacedTokenException(int cursor, int position) {
		return new IllegalStateException("Encountred misplaced character '" + buffer[cursor] + "' at position " + position);
	}
	
	private void checkIllegalEnd(int token) {
		if ( token == -1 && JsonType.EMPTY != _ctx.peek() ) 
			throw new IllegalStateException("Incomplete data or malformed json : encoutered end of stream!");
	}
	
//	switch (token) {
//	case QUOTE: {
//		JsonType type = _ctx.peek();
//		if ( type == JsonType.NAME || type == JsonType.LITERAL ) {
//			_ctx.pop();
//		} else {
//			throw new IllegalStateException("Encountred misplaced character '" + (char)token + "' at position " + position);
//		}
//		break;
//	}
//	case VALUE_SEPARATOR: {
//		
//		break;
//	}
//	case BEGIN_ARRAY: {
//		_ctx.push(JsonType.ARRAY);
//		break;
//	}
//	case END_ARRAY: {
//		JsonType type = _ctx.pop();
//		if ( type != JsonType.ARRAY )
//			throw new IllegalStateException("Encoutred an end array character without a begin array at position " + position);
//		break;
//	}
//	case BEGIN_OBJECT: {
//		_ctx.push(JsonType.OBJECT);
//		break;
//	}
//	case END_OBJECT: {
//		JsonType type = _ctx.pop();
//		if ( type != JsonType.OBJECT )
//			throw new IllegalStateException("Encoutred an end object character without a begin object at position " + position);
//		break;
//	}
//	case -1:
//		if ( _ctx.peek() != JsonType.EMPTY )
//			throw new IllegalStateException("Encountred end of document with missing characters!");
//		break;
//	default:
//		throw new IllegalStateException("Encoutred misplaced character '" + (char)token + "' at position " + position);
//}
//
//return token;
}
