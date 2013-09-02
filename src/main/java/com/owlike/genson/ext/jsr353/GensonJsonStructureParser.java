package com.owlike.genson.ext.jsr353;

import java.math.BigDecimal;
import java.util.Iterator;

import javax.json.JsonArray;
import javax.json.JsonStructure;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

public class GensonJsonStructureParser implements JsonParser {

    private JsonStructure source;
    private Iterator<Event> structIterator;
    
    public GensonJsonStructureParser(JsonStructure source) {
        this.source = source;
    }
    
    @Override public boolean hasNext() {
        return structIterator.hasNext();
    }
    
    boolean hasNextInArray() {
        final JsonArray array = (JsonArray) source;
        
    }

    @Override public Event next() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public String getString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public boolean isIntegralNumber() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override public int getInt() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override public long getLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override public BigDecimal getBigDecimal() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public JsonLocation getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public void close() {
        // TODO Auto-generated method stub
        
    }

}
