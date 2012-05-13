package org.likeit.transformation.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.IllegalSelectorException;
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
	// buffer size doit etre > 5 (pour pouvoir contenir FALSE, TRUE et NULL en entier)
	private char[] buffer = new char[1024];
	private int position;
	private int cursor;
	private int buflen;
	private StringBuilder sb = new StringBuilder();
	private String currentName;
	private String currentValue;
	private boolean checkedNext = false;
	private boolean hasNext = false;
	private boolean _first = false;
	
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
	public ObjectReader beginArray() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( BEGIN_ARRAY == token ) {
			_ctx.push(JsonType.ARRAY);
		} else throw newWrongTokenException(BEGIN_ARRAY, token, position-1);
		_first = true;
		return this;
	}

	@Override
	public ObjectReader beginObject() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( BEGIN_OBJECT == token ) {
			_ctx.push(JsonType.OBJECT);
		} else throw newWrongTokenException(BEGIN_OBJECT, token, position-1);
		_first = true;
		return this;
	}

	@Override
	public ObjectReader endArray() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( END_ARRAY == token && JsonType.ARRAY == _ctx.peek() ) {
			_ctx.pop();
		} else throw newWrongTokenException(END_ARRAY, token, position-1);
		_first = false;
		return this;
	}

	@Override
	public ObjectReader endObject() throws IOException {
		int token = readNextToken(true);
		checkIllegalEnd(token);
		if ( END_OBJECT == token && JsonType.OBJECT == _ctx.peek() ) {
			_ctx.pop();
		} else throw newWrongTokenException(END_OBJECT, token, position-1);
		_first = false;
		return this;
	}

	@Override
	public boolean hasNext() throws IOException {
		if ( checkedNext ) return hasNext;
		else {
			int token = readNextToken(false);
			checkIllegalEnd(token);
			hasNext = (_first && (QUOTE == token || (token > 47 && token < 58) || token == 45 || token == 110 || token == 116 || token == 102 ))
					|| token == VALUE_SEPARATOR;
			checkedNext = true;
			return hasNext;
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
		_first = false;

		if ( readNextToken(false) == VALUE_SEPARATOR ) increment();
		
		if ( JsonType.OBJECT == _ctx.peek() ) {
			consumeString();
			
			currentName = sb.toString();
			sb.setLength(0);
			
			if ( readNextToken(true) != NAME_SEPARATOR ) throw newMisplacedTokenException(cursor-1, position-1);
		}
		
		int token = readNextToken(false);
		if ( token == QUOTE ) consumeString();
		else consumeLiteral();
		
		currentValue = sb.toString();
		sb.setLength(0);
		
		return this;
	}

	private void consumeString() throws IOException {
		if ( readNextToken(true) != QUOTE ) throw newMisplacedTokenException(cursor-1, position-1);
		
		while (buflen > -1 ) {
			if ( cursor >= buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
				// TODO calc pos?
			}

			int i = cursor;
			for ( ; i < buflen; i++ ) {
				if ( buffer[i] == '\\' ) {
					// TODO calc position
					sb.append(buffer, cursor, i-cursor);
					cursor = i;
					readEscaped();
					i = cursor;
				}
				else if ( buffer[i] == QUOTE ) break;
			}
			
			sb.append(buffer, cursor, i-cursor);
			position += i - cursor + 1;
			cursor = i + 1;
			if ( i < buflen && buffer[i] == QUOTE ) return;
		}
	}

	private void consumeLiteral() throws IOException {
		if ( cursor >= buflen ) {
			buflen = reader.read(buffer);
			cursor = 0;
		}
		
		int token = buffer[cursor];
		
		if ( (token > 47 && token < 58) || token == 45 ) {
			if ( token == 45 ) {
				sb.append('-');
				increment();
			}
			consumeInt();
			if ( buffer[cursor] == 46 ) {
				sb.append('.');
				cursor++;
				consumeInt();
			}
		} else {
			if ( (buflen-cursor) < 5 ) {
				System.arraycopy(buffer, cursor, buffer, 0, buflen-cursor);
				buflen = cursor + reader.read(buffer, cursor, cursor);
				cursor = 0;
			}
			
			if ( (buffer[cursor] == 'N' || buffer[cursor] == 'n')
				&& (buffer[cursor+1] == 'U' || buffer[cursor+1] == 'u')
				&& (buffer[cursor+2] == 'L' || buffer[cursor+2] == 'l')
				&& (buffer[cursor+3] == 'L' || buffer[cursor+3] == 'l')) {
				sb.append(NULL_VALUE);
				cursor += 4;
				position +=  4;
			} else if ( (buffer[cursor] == 'T' || buffer[cursor] == 't')
						&& (buffer[cursor+1] == 'R' || buffer[cursor+1] == 'r')
						&& (buffer[cursor+2] == 'U' || buffer[cursor+2] == 'u')
						&& (buffer[cursor+3] == 'E' || buffer[cursor+3] == 'e')) {
				sb.append("true");
				cursor += 4;
				position +=  4;
			} else if ( (buffer[cursor] == 'F' || buffer[cursor] == 'f')
						&& (buffer[cursor+1] == 'A' || buffer[cursor+1] == 'a')
						&& (buffer[cursor+2] == 'L' || buffer[cursor+2] == 'l')
						&& (buffer[cursor+3] == 'S' || buffer[cursor+3] == 's')
						&& (buffer[cursor+4] == 'E' || buffer[cursor+4] == 'e')) {
				sb.append("false");
				cursor += 5;
				position +=  5;
			} else {
				throw new IllegalStateException("Illegal character around position " + position + " awaited for literal (number, boolean or null)");
			}
		}
	}
	
	private void consumeInt() throws IOException {
		boolean stop = false;
		while (buflen > -1 ) {
			if ( cursor >= buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
			}

			int i = cursor;
			for ( ; i < buflen; i++ ) {
				if ( (buffer[i] < 48 || buffer[i] > 57) ) {
					stop = true;
					break;
				}
			}
			
			sb.append(buffer, cursor, i-cursor);
			position += i - cursor;
			cursor = i;
			if ( stop ) return;
		}
	}
	
	// TODO consume boolean
	private int readNextToken(boolean consume) throws IOException {
		boolean stop = false;
		int oldCursor = cursor;
		
		while ( buflen > -1 ) {
			if ( cursor >= buflen ) {
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
					return buffer[cursor-1];
				} else return buffer[cursor];
			}
		}
		
		return -1;
	}
	
	 private char readEscaped() throws IOException {
		increment();
        while ( buflen > -1 ) {
        	if ( cursor >= buflen ) {
				buflen = reader.read(buffer);
				cursor = 0;
				checkIllegalEnd(buflen);
			}
        	
        	int token = buffer[cursor];
    
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
                    throw newMisplacedTokenException(cursor, position);
            }
    
            int value = 0;
//            for (int i = 0; i < 4; ++i) {
//                if (_inputPtr >= _inputEnd) {
//                    if (!loadMore()) {
//                        _reportInvalidEOF(" in character escape sequence");
//                    }
//                }
//                int ch = (int) _inputBuffer[_inputPtr++];
//                int digit = CharTypes.charToHex(ch);
//                if (digit < 0) {
//                    _reportUnexpectedChar(ch, "expected a hex-digit for character escape sequence");
//                }
//                value = (value << 4) | digit;
//            }

            return (char) value;
        }
        return 0;
    }
	
	private void increment() {
		cursor++;
		position++;
	}
	
	private IllegalStateException newWrongTokenException(int awaitedChar, int token, int position) {
		return new IllegalStateException("Illegal character at position " + position + " awaited " + (char)awaitedChar + " but read '" + (char)token + "' !");
	}
	
	private IllegalStateException newMisplacedTokenException(int cursor, int position) {
		return new IllegalStateException("Encountred misplaced character '" + buffer[cursor] + "' at position " + position);
	}
	
	private void checkIllegalEnd(int token) {
		if ( token == -1 && JsonType.EMPTY != _ctx.peek() ) 
			throw new IllegalStateException("Incomplete data or malformed json : encoutered end of stream!");
	}
	
}
