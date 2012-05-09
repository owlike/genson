package org.likeit.transformation.stream;

import java.io.InputStream;
import java.util.Stack;

public class JsonReader implements ObjectReader {
	protected static final int BEGIN_ARRAY = '[';
	protected static final int END_ARRAY = ']';
	protected static final int BEGIN_OBJECT = '{';
	protected static final int END_OBJECT = '}';
	protected static final int NAME_BEGIN = '"';
	protected static final int NAME_END = '"';
	protected static final int VALUE_BEGIN = '\"';
	protected static final int VALUE_END = '\"';

	protected static final int VALUE_SEPARATOR = ',';
	protected static final int NAME_SEPARATOR = ':';
	protected final static String NULL_VALUE = "null";
	
	private final InputStream is;
	private String currentName;
	private String currentValue;
	private Stack<JsonType> _ctx;
	
	public JsonReader(InputStream is) {
		this.is = is;
		_ctx = new Stack<JsonType>();
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
	public ObjectReader next() {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public String value() {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	protected void readNext() {
		JsonType currentType = _ctx.peek();
//		is.read();
	}
}
