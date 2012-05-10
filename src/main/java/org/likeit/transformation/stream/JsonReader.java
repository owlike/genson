package org.likeit.transformation.stream;

import java.io.IOException;
import java.io.InputStream;
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
		SKIPPED_TOKENS[VALUE_SEPARATOR] = 1;
	}
	
	private final InputStream is;
	byte[] buffer = new byte[1024];
	private int position;
	private int localPos;
	private int end;
	private String currentName;
	private String currentValue;
	private Deque<JsonType> _ctx;
	
	public JsonReader(InputStream is) {
		this.is = is;
		_ctx = new ArrayDeque<JsonType>();
		_ctx.add(JsonType.EMPTY);
	}
	
	@Override
	public void beginArray() {
		
	}

	@Override
	public void beginObject() {
		// TODO Module de remplacement de méthode auto-généré
		
	}

	@Override
	public void endArray() {
		// TODO Module de remplacement de méthode auto-généré
		
	}

	@Override
	public void endObject() {
		// TODO Module de remplacement de méthode auto-généré
		
	}

	@Override
	public boolean hasNext() {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}

	@Override
	public String name() {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public ObjectReader next() throws IOException {
		int token = readNextToken();
		
		
		
		return null;
	}

	@Override
	public String value() {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}
	
	protected String readString() {
		StringBuilder sb = new StringBuilder();
		
		int len = -1;
		
		
		return sb.toString();
	}

	private void read() throws IOException {
		int len = is.read(buffer, localPos, buffer.length - localPos);
	}
	
	protected int readNextToken() throws IOException {
		
		int token = -1; 
		// on lit les char blancs jusqu'au prochain charactere indiquant un type
		while ( (token = is.read()) > -1 && SKIPPED_TOKENS[token] == 1 ) {
			position++;
		}
		
		switch (token) {
			case QUOTE: {
				JsonType type = _ctx.peek();
				if ( type == JsonType.NAME || type == JsonType.LITERAL ) {
					_ctx.pop();
				} else {
					throw new IllegalStateException("Encountred misplaced character '" + (char)token + "' at position " + position);
				}
				break;
			}
			case VALUE_SEPARATOR: {
				
				break;
			}
			case BEGIN_ARRAY: {
				_ctx.push(JsonType.ARRAY);
				break;
			}
			case END_ARRAY: {
				JsonType type = _ctx.pop();
				if ( type != JsonType.ARRAY )
					throw new IllegalStateException("Encoutred an end array character without a begin array at position " + position);
				break;
			}
			case BEGIN_OBJECT: {
				_ctx.push(JsonType.OBJECT);
				break;
			}
			case END_OBJECT: {
				JsonType type = _ctx.pop();
				if ( type != JsonType.OBJECT )
					throw new IllegalStateException("Encoutred an end object character without a begin object at position " + position);
				break;
			}
			case -1:
				if ( _ctx.peek() != JsonType.EMPTY )
					throw new IllegalStateException("Encountred end of document with missing characters!");
				break;
			default:
				throw new IllegalStateException("Encoutred misplaced character '" + (char)token + "' at position " + position);
	}
		
		return token;
	}
}
