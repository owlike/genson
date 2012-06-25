package org.genson.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.genson.TransformationRuntimeException;


/*
 * TODO remplacer le calcul de la position par un compteur ligne+colonne, et faire deux methodes qui renvoyent les bonnes valeurs,
 *  sans qu'on ait a se soucier de les mettre a jour partout... Du coups on pourra incrementer cursor sans passer par la methode increment().
 * 
 * TODO add checks for number reading, handle overflows : configurable => throw exception or return to the begin of the value (actual behavior)
 */
public class JsonReader implements ObjectReader {
	protected final static int BEGIN_ARRAY = '[';
	protected final static int END_ARRAY = ']';
	protected final static int BEGIN_OBJECT = '{';
	protected final static int END_OBJECT = '}';
	protected final static int QUOTE = '"';

	protected final static int VALUE_SEPARATOR = ',';
	protected final static int NAME_SEPARATOR = ':';
	protected final static String NULL_VALUE = "null";

	protected final static int[] SKIPPED_TOKENS;
	static {
		SKIPPED_TOKENS = new int[128];
		SKIPPED_TOKENS['\t'] = 1;
		SKIPPED_TOKENS['\b'] = 1;
		SKIPPED_TOKENS['\n'] = 1;
		SKIPPED_TOKENS['\r'] = 1;
		SKIPPED_TOKENS['\f'] = 1;
		SKIPPED_TOKENS[' '] = 1;
	}

	/*
	 * Recupere dans Jackson
	 */
	private final static int[] sHexValues = new int[128];
	static {
		Arrays.fill(sHexValues, -1);
		for (int i = 0; i < 10; ++i) {
			sHexValues['0' + i] = i;
		}
		for (int i = 0; i < 6; ++i) {
			sHexValues['a' + i] = 10 + i;
			sHexValues['A' + i] = 10 + i;
		}
	}
	
	private final static int[] _CHAR_TO_INT = new int[58];
	static {
		for (int i = 48; i < 58; i++) _CHAR_TO_INT[i] = i - 48;
	}
	
	private final Reader reader;
	// TODO recyclebuffer
	private final char[] _buffer = new char[2048];
	private int _position;
	private int _cursor;
	private int _buflen;
	
	private char[] _stringBuffer = new char[16];
	private int _stringBufferTail = 0;
	private int _stringBufferLength = _stringBuffer.length;
	
	private String currentName;
	private String _stringValue;
	private long _intValue;
	private double _doubleValue;
	private int _numberLen = 0;
	private Boolean _booleanValue;
	private ValueType valueType;
	private boolean _checkedNext = false;
	private boolean _hasNext = false;
	private boolean _first = false;
	private boolean _metadata_readen = false;
	private Map<String, String> _metadata = new HashMap<String, String>(5);
	
	private final Deque<JsonType> _ctx = new ArrayDeque<JsonType>();
	{
		_ctx.push(JsonType.EMPTY);
	}

	public JsonReader(String source) {
		this(new StringReader(source));
	}

	public JsonReader(Reader reader) {
		this.reader = reader;
		try {
			int token = readNextToken(false);
			if ( BEGIN_ARRAY == token ) setValueType(ValueType.ARRAY);
			else if ( BEGIN_OBJECT == token ) setValueType(ValueType.OBJECT);
			else {
				// ok lets try to read next
				if (_buflen > 0) {
					try {
						consumeValue();
					} catch (IllegalStateException ise) {
						try {
							// we must cheat because consumeString attends the current token to be "
							// and will increment the cursor
							_cursor = -1;
							_position = -1;
    						consumeString('"');
    						_stringValue = new String(_stringBuffer, 0, _stringBufferTail);
    						valueType = ValueType.STRING;
						} catch (RuntimeException re) {
							throw ise;
						}
					}
					if (ValueType.valueOf(valueType.name()) == null)
						throw new TransformationRuntimeException("Failed to instanciate reader, first character was " 
								+ (char) token + " when possible character are [ and {");
				} else setValueType(ValueType.NULL);
			}
		} catch (IOException ioe) {
			throw new TransformationRuntimeException("Failed to instanciate reader!", ioe);
		}
	}

	@Override
	public ObjectReader beginArray() throws IOException {
		begin(BEGIN_ARRAY, JsonType.ARRAY);
		valueType = ValueType.ARRAY;
		if (_metadata_readen) _metadata.clear();
		return this;
	}

	@Override
	public ObjectReader beginObject() throws IOException {
		if (!_metadata_readen) {
    		begin(BEGIN_OBJECT, JsonType.OBJECT);
    		valueType = ValueType.OBJECT;
    		_metadata.clear();
    		readMetadata();
		}
		return this;
	}
	
	public ObjectReader nextObjectMetadata() throws IOException {
		return beginObject();
	}

	@Override
	public ObjectReader endArray() throws IOException {
		end(END_ARRAY, JsonType.ARRAY);
		return this;
	}

	@Override
	public ObjectReader endObject() throws IOException {
		end(END_OBJECT, JsonType.OBJECT);
		_metadata.clear();
		_metadata_readen = false;
		return this;
	}

	@Override
	public String name() {
		return currentName;
	}

	@Override
	public String valueAsString() {
		if (ValueType.STRING.equals(valueType))
			return _stringValue;
		if(ValueType.NULL.equals(valueType)) return NULL_VALUE;
		if (ValueType.INTEGER.equals(valueType)) {
			return String.valueOf(_intValue);
		}
		if (ValueType.DOUBLE.equals(valueType)) {
			return String.valueOf(_doubleValue);
		}
		if (ValueType.BOOLEAN.equals(valueType)) {
			return _booleanValue.toString();
		}
		throw new IllegalStateException("Readen value can not be converted to String");
	}
	
	public int valueAsInt() throws IOException {
		if (ValueType.INTEGER.equals(valueType)) {
			return (int) _intValue;
		}
		if (ValueType.DOUBLE.equals(valueType)) {
			return (int)_doubleValue;
		}
		if (ValueType.NULL.equals(valueType))
			return 0;
		if (ValueType.STRING.equals(valueType))
			return "".equals(_stringValue) ? null : Integer.valueOf(_stringValue);
		throw new IllegalStateException("Readen value is not of type int");
		// TODO NULL? OR STRING?
	}
	
	public long valueAsLong() throws IOException {
		if (ValueType.INTEGER.equals(valueType)) {
			return _intValue;
		}
		if (ValueType.DOUBLE.equals(valueType)) {
			return (long) _doubleValue;
		}
		if (ValueType.NULL.equals(valueType))
			return 0;
		if (ValueType.STRING.equals(valueType))
			return "".equals(_stringValue) ? null : Long.valueOf(_stringValue);
		throw new IllegalStateException("Readen value is not of type int");
	}
	
	public double valueAsDouble() throws IOException {
		if (ValueType.DOUBLE.equals(valueType)) {
			return _doubleValue;
		}
		if (ValueType.INTEGER.equals(valueType)) {
			return _intValue;
		}
		if (ValueType.NULL.equals(valueType))
			return 0;
		if (ValueType.STRING.equals(valueType))
			return "".equals(_stringValue) ? null : Double.valueOf(_stringValue);
		throw new IllegalStateException("Readen value is not of type int");
	}
	
	// TODO should we try here to create the right type based on the string input?
	public Number valueAsNumber() throws IOException {
		if (ValueType.DOUBLE.equals(valueType)) {
			return _doubleValue;
		}
		if (ValueType.INTEGER.equals(valueType)) {
			return _intValue;
		}
		if (ValueType.NULL.equals(valueType))
			return null;
		throw new IllegalStateException("Readen value is not of type Number");
	}
	
	public boolean valueAsBoolean() throws IOException {
		if (ValueType.BOOLEAN.equals(valueType)) {
			return _booleanValue;
		}
		if (ValueType.STRING.equals(valueType))
			return "".equals(_stringValue) ? null : Boolean.valueOf(_stringValue);
		if (ValueType.NULL.equals(valueType)) return false;
		throw new IllegalStateException("Readen value is not of type Boolean");
	}
	
	@Override
	public String metadata(String name) throws IOException {
		if (!_metadata_readen) nextObjectMetadata();
		return _metadata.get(name);
	}

	@Override
	public ValueType getValueType() {
		return valueType;
	}

	public ObjectReader skipValue() throws IOException {

		if (ValueType.ARRAY == valueType || ValueType.OBJECT == valueType) {
			int balance = 0;
			do {
				if (ValueType.ARRAY == valueType) {
					beginArray();
					balance++;
				} else if (ValueType.OBJECT == valueType) {
					beginObject();
					balance++;
				}
				if (hasNext())
					next();
				else {
					JsonType type = _ctx.peek();
					if (JsonType.ARRAY == type) {
						endArray();
						balance--;
					} else if (JsonType.OBJECT == type) {
						endObject();
						balance--;
					}
				}
			} while (balance > 0);
		}

		return this;
	}

	@Override
	public boolean hasNext() throws IOException {
		if (_checkedNext)
			return _hasNext;
		else {
			int token = readNextToken(false);
			checkIllegalEnd(token);
			_hasNext = (token == VALUE_SEPARATOR && !_first) || (_first && (QUOTE == token || token == BEGIN_OBJECT || token == BEGIN_ARRAY
					|| (token > 47 && token < 58) || token == 45 || token == 110 || token == 116 || token == 102));
			_checkedNext = true;

			if (_hasNext && !_first) {
				increment();
			}

			return _hasNext;
		}
	}

	@Override
	public ValueType next() throws IOException {
		_metadata_readen = false;
		_checkedNext = false;
		_hasNext = false;
		_first = false;
		resetNameAndValue();

		int token = readNextToken(false);

		if (token == VALUE_SEPARATOR) {
			increment();
			token = readNextToken(false);
		} else if (JsonType.ARRAY == _ctx.peek()) {
			if (token == BEGIN_ARRAY)
				return setValueType(ValueType.ARRAY);
			if (token == BEGIN_OBJECT)
				return setValueType(ValueType.OBJECT);
		}

		if (JsonType.OBJECT == _ctx.peek()) {
			consumeName(token);
			currentName = new String(_stringBuffer, 0, _stringBufferTail);
			_stringBufferTail = 0;

			if (readNextToken(true) != NAME_SEPARATOR)
				newMisplacedTokenException(_cursor - 1, _position - 1);
		}
		
		return consumeValue();
	}

	protected final ValueType consumeValue() throws IOException {
		int token = readNextToken(false);
		ValueType valueType = null;
		if (token == QUOTE) {
			consumeString(token);
			_stringValue = new String(_stringBuffer, 0, _stringBufferTail);
			_stringBufferTail = 0;
			valueType = ValueType.STRING;
		} else if (token == BEGIN_ARRAY)
			return setValueType(ValueType.ARRAY);
		else if (token == BEGIN_OBJECT)
			return setValueType(ValueType.OBJECT);
		else
			valueType = consumeLiteral();
		
		return setValueType(valueType);
	}
	
	protected final void readMetadata() throws IOException {
		_metadata_readen = true;
		while(true) {
    		int token = readNextToken(false);
    		if ( '"' != token ) return;
    		ensureBufferHas(2, true);
    		
    		if ( '@' == _buffer[_cursor+1] ) {
    			increment();
    			// we cheat here...
    			consumeString(token);
    			String key = new String(_stringBuffer, 0, _stringBufferTail);
    			_stringBufferTail = 0;
    
    			if (readNextToken(true) != NAME_SEPARATOR)
    				newMisplacedTokenException(_cursor - 1, _position - 1);
    			
    			consumeString(readNextToken(false));
    			_metadata.put(key, new String(_stringBuffer, 0, _stringBufferTail));
    			_stringBufferTail = 0;
    			if (readNextToken(false) == VALUE_SEPARATOR) {
    				increment();
    			}
    		} else return;
		}
	}
	
	protected final void begin(int character, JsonType type) throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if (character == token) {
			_ctx.push(type);
		} else newWrongTokenException(character, token, _position - 1);
		_first = true;
		_checkedNext = false;
		_hasNext = false;
	}

	protected final void end(int character, JsonType type) throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if (character == token && type == _ctx.peek()) {
			_ctx.pop();
		} else newWrongTokenException(character, token, _position - 1);
		_first = false;
		_checkedNext = false;
		_hasNext = false;
	}

	protected final ValueType setValueType(ValueType tokenType) {
		this.valueType = tokenType;
		return tokenType;
	}

	protected final void consumeName(int token) throws IOException {
		if (token != QUOTE) newMisplacedTokenException(_cursor, _position);
		increment();
		for ( ; _buflen > -1; ) {
			fillBuffer(true);
			int i = _cursor;
			for (; i < _buflen; i++) {
				if (_buffer[i] == QUOTE) break;
			}
			
			writeToStringBuffer(_buffer, _cursor, i - _cursor);
			_position += i - _cursor + 1;
			_cursor = i + 1;
			if (i < _buflen && _buffer[i] == QUOTE) return;
		}
	}
	
	protected final void consumeString(int token) throws IOException {
		if (token != QUOTE) newMisplacedTokenException(_cursor, _position);
		increment();
		for ( ; _buflen > -1; ) {
			if (_cursor >= _buflen) {
				_buflen = reader.read(_buffer);
				_cursor = 0;
				checkIllegalEnd(_buflen);
			}

			int i = _cursor;
			for (; i < _buflen;) {
				if (_buffer[i] == '\\') {
					writeToStringBuffer(_buffer, _cursor, i - _cursor);
					_cursor = i;
					increment();
					if (_stringBufferLength <= (_stringBufferTail+1)) expandStringBuffer(16);
					_stringBuffer[_stringBufferTail++] = readEscaped();
					i = _cursor;
				}
				else if (_buffer[i] == QUOTE) break;
				else i++;
			}

			writeToStringBuffer(_buffer, _cursor, i - _cursor);
			_position += i - _cursor + 1;
			_cursor = i + 1;
			if (i < _buflen && _buffer[i] == QUOTE)
				return;
		}
	}

	protected final ValueType consumeLiteral() throws IOException {
		fillBuffer(true);
		int token = _buffer[_cursor];

		if ((token > 47 && token < 58) || token == 45) {
			ValueType valueType = null;
			int sign = 1;
			if (token == 45) {
				increment();
				sign = -1;
			}
			_intValue = consumeInt();
			if (isEOF()) return ValueType.INTEGER;
					
			if (_buffer[_cursor] == 46) {
				increment();
				_doubleValue = consumeInt();
				_doubleValue = sign * (_intValue + _doubleValue/Math.pow(10, _numberLen));
				valueType = ValueType.DOUBLE;
			} else {
				_intValue *= sign;
				valueType = ValueType.INTEGER;
			}
			
			if (isEOF() || ensureBufferHas(2, false) < 0) return valueType;

			char ctoken = _buffer[_cursor];
			if (ctoken == 'e' || ctoken == 'E') {
				increment();
				ctoken = _buffer[_cursor];
				if (ctoken == '-'||ctoken == '+'||(ctoken > 47 && ctoken < 58)) {
					if(ctoken == '-'||ctoken == '+') increment();
					double val = consumeInt();
					double exp = 0;
					boolean limit = false;
					if(ctoken=='-') {
						exp = 1/Math.pow(10, val);
						if (exp == 0) {
							exp = Math.pow(10, -val+1);
							limit = true;
						}
					} else {
						exp = Math.pow(10, val);
					}
					if (ValueType.INTEGER.equals(valueType)) {
						_intValue = (long) (_intValue * exp);
					} else {
						_doubleValue = _doubleValue * exp;
						if (limit) _doubleValue = _doubleValue/10d;
					}
				} else newWrongTokenException("'-' or '+' or '' (same as +)", ctoken, _position);
			}

			return valueType;
		} else {
			ensureBufferHas(4, true); 

			if ((_buffer[_cursor] == 'N' || _buffer[_cursor] == 'n')
					&& (_buffer[_cursor + 1] == 'U' || _buffer[_cursor + 1] == 'u')
					&& (_buffer[_cursor + 2] == 'L' || _buffer[_cursor + 2] == 'l')
					&& (_buffer[_cursor + 3] == 'L' || _buffer[_cursor + 3] == 'l')) {
				_cursor += 4;
				_position += 4;
				return ValueType.NULL;
			}
			
			if ((_buffer[_cursor] == 'T' || _buffer[_cursor] == 't')
					&& (_buffer[_cursor + 1] == 'R' || _buffer[_cursor + 1] == 'r')
					&& (_buffer[_cursor + 2] == 'U' || _buffer[_cursor + 2] == 'u')
					&& (_buffer[_cursor + 3] == 'E' || _buffer[_cursor + 3] == 'e')) {
				_booleanValue = true;
				_cursor += 4;
				_position += 4;
				return ValueType.BOOLEAN;
			}
			ensureBufferHas(5, true);
			
			if ((_buffer[_cursor] == 'F' || _buffer[_cursor] == 'f')
					&& (_buffer[_cursor + 1] == 'A' || _buffer[_cursor + 1] == 'a')
					&& (_buffer[_cursor + 2] == 'L' || _buffer[_cursor + 2] == 'l')
					&& (_buffer[_cursor + 3] == 'S' || _buffer[_cursor + 3] == 's')
					&& (_buffer[_cursor + 4] == 'E' || _buffer[_cursor + 4] == 'e')) {
				_booleanValue = false;
				_cursor += 5;
				_position += 5;
				return ValueType.BOOLEAN;
			} else {
				throw new IllegalStateException("Illegal character around position " + _position
						+ " awaited for literal (number, boolean or null) but read '" + _buffer[_cursor] + "'!");
			}
		}
	}

	private final long consumeInt() throws IOException {
		boolean stop = false;
		long value = 0;
		_numberLen = 0;
		for ( ; _buflen > -1; ) {
			fillBuffer(true);
			int i = _cursor;
			for (; i < _buflen; i++) {
				if ((_buffer[i] < 48 || _buffer[i] > 57)) {
					stop = true;
					break;
				}
				_numberLen++;
				value = 10 * value + _CHAR_TO_INT[_buffer[i]];
			}

			//_sb.append(buffer, cursor, i - cursor);
			_position += i - _cursor;
			_cursor = i;
			if (stop) {
				if (_numberLen == 0) newWrongTokenException("numeric value", _buffer[i], _position);
				return value;
			}
		}
		return value;
	}

	protected final int readNextToken(boolean consume) throws IOException {
		boolean stop = false;
		int oldCursor = _cursor;

		for ( ; _buflen > -1; ) {
			fillBuffer(true);

			for (; _cursor < _buflen; _cursor++) {
				if (_buffer[_cursor] < 128 && SKIPPED_TOKENS[_buffer[_cursor]] == 0) {
					stop = true;
					break;
				}
			}

			// TODO attention c'est faux
			_position += _cursor - oldCursor;
			oldCursor = _cursor - 1;

			if (stop) {
				if (consume) {
					_cursor++;
					_position++;
					return _buffer[_cursor - 1];
				} else
					return _buffer[_cursor];
			}
		}

		return -1;
	}

	protected final char readEscaped() throws IOException {
		fillBuffer(true);

		char token = _buffer[_cursor];
		increment();
		switch (token) {
		case 'b':
			return '\b';
		case 't':
			return '\t';
		case 'n':
			return '\n';
		case 'f':
			return '\f';
		case 'r':
			return '\r';
		case QUOTE:
		case '/':
		case '\\':
			return (char) token;

		case 'u':
			break;

		default:
			newMisplacedTokenException(_cursor - 1, _position - 1);
		}

		int value = 0;
		if (ensureBufferHas(4, false) < 0) {
			throw new IllegalStateException("Expected 4 hex-digit for character escape sequence!");
			// System.arraycopy(buffer, cursor, buffer, 0, buflen-cursor);
			// buflen = buflen - cursor + reader.read(buffer, buflen-cursor, cursor);
			// cursor = 0;
		}
		for (int i = 0; i < 4; ++i) {
			int ch = _buffer[_cursor++];
			int digit = (ch > 127) ? -1 : sHexValues[ch];
			if (digit < 0) {
				throw new IllegalStateException("Wrong character '" + ch
						+ "' expected a hex-digit for character escape sequence!");
			}
			value = (value << 4) | digit;
		}

		return (char) value;
	}

	private final void writeToStringBuffer(final char[] data, final int offset, final int length) throws IOException {
		if (_stringBufferLength <= (_stringBufferTail+length)) {
			expandStringBuffer(length);
		}
		System.arraycopy(data, offset, _stringBuffer, _stringBufferTail, length);
		_stringBufferTail += length;
	}
	
	private final void expandStringBuffer(int length) {
		char[] extendedStringBuffer = new char[_stringBufferLength * 2 + length];
		System.arraycopy(_stringBuffer, 0, extendedStringBuffer, 0, _stringBufferTail);
		_stringBuffer = extendedStringBuffer;
		_stringBufferLength = extendedStringBuffer.length;
	}
	
	private final int fillBuffer(boolean doThrow) throws IOException {
		if (_cursor < _buflen) return _buflen;
		_buflen = reader.read(_buffer);
		checkIllegalEnd(_buflen);
		_cursor = 0;
		return _buflen;
	}
	
	private final int ensureBufferHas(int minLength, boolean doThrow) throws IOException {
		int actualLen = _buflen - _cursor;
		if ( actualLen >= minLength ) {
			return actualLen;
		}
		// TODO cas ou cursor 0 optimiser?
		System.arraycopy(_buffer, _cursor, _buffer, 0, actualLen);
		for (; actualLen < minLength; ) {
			int len = reader.read(_buffer, actualLen, _buffer.length - actualLen);
			if (len < 0) {
				if (doThrow)
					throw new IllegalStateException("Encountered end of stream, incomplete json!");
				else return len;
			}
			actualLen += len;
		}
		_buflen = actualLen;
		_cursor = 0;
		return actualLen;
	}
	
	protected final boolean isEOF() throws IOException {
		return _buflen < 0 || fillBuffer(false) < 0;
	}
	
	private final void resetNameAndValue() {
		_doubleValue = 0;
		_intValue = 0;
		_booleanValue = null;
		currentName = null;
		_stringValue = null;
	}

	private final void increment() {
		_cursor++;
		_position++;
	}

	private final void newWrongTokenException(String awaited, int token, int position) {
		throw new IllegalStateException("Illegal character at position " + position + " expected "
				+ awaited + " but read '" + (char) token + "' !");
	}
	
	private final void newWrongTokenException(int awaitedChar, int token, int position) {
		throw new IllegalStateException("Illegal character at position " + position + " expected "
				+ (char) awaitedChar + " but read '" + (char) token + "' !");
	}

	private final void newMisplacedTokenException(int cursor, int position) {
		throw new IllegalStateException("Encountred misplaced character '" + _buffer[cursor] + "' at position "
				+ position);
	}

	private final void checkIllegalEnd(int token) throws IOException {
		if (token == -1 && JsonType.EMPTY != _ctx.peek())
			throw new IOException("Incomplete data or malformed json : encoutered end of stream!");
	}

}
