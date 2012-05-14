package org.likeit.json.bean;

public class Primitives {
	private int intPrimitive;
	private Integer integerObject;
	private double doublePrimitive;
	private Double doubleObject;
	private String text;
	private boolean booleanPrimitive;
	private boolean booleanObject;
	
	
	public Primitives() {}
	
	public Primitives(int intPrimitive, Integer integerObject,
			double doublePrimitive, Double doubleObject, String text,
			boolean booleanPrimitive, boolean booleanObject) {
		super();
		this.intPrimitive = intPrimitive;
		this.integerObject = integerObject;
		this.doublePrimitive = doublePrimitive;
		this.doubleObject = doubleObject;
		this.text = text;
		this.booleanPrimitive = booleanPrimitive;
		this.booleanObject = booleanObject;
	}
	
	
	public String jsonString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"intPrimitive\":")
			.append(intPrimitive)
			.append(",\"integerObject\":")
			.append(integerObject)
			.append(",\"doublePrimitive\":")
			.append(doublePrimitive)
			.append(",\"doubleObject\":")
			.append(doubleObject)
			.append(",\"booleanPrimitive\":")
			.append(booleanPrimitive)
			.append(",\"booleanObject\":")
			.append(booleanObject)
			.append(",\"text\":")
			.append("\"" + text + "\"")
			.append('}');
		return sb.toString();
	}
	
	public int getIntPrimitive() {
		return intPrimitive;
	}
	public void setIntPrimitive(int intPrimitive) {
		this.intPrimitive = intPrimitive;
	}
	public Integer getIntegerObject() {
		return integerObject;
	}
	public void setIntegerObject(Integer integerObject) {
		this.integerObject = integerObject;
	}
	public double getDoublePrimitive() {
		return doublePrimitive;
	}
	public void setDoublePrimitive(double doublePrimitive) {
		this.doublePrimitive = doublePrimitive;
	}
	public Double getDoubleObject() {
		return doubleObject;
	}
	public void setDoubleObject(Double doubleObject) {
		this.doubleObject = doubleObject;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isBooleanPrimitive() {
		return booleanPrimitive;
	}
	public void setBooleanPrimitive(boolean booleanPrimitive) {
		this.booleanPrimitive = booleanPrimitive;
	}
	public boolean isBooleanObject() {
		return booleanObject;
	}
	public void setBooleanObject(boolean booleanObject) {
		this.booleanObject = booleanObject;
	}
}
