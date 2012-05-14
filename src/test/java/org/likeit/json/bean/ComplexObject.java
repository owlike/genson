package org.likeit.json.bean;

import java.util.List;

public class ComplexObject {
	private Primitives primitives;
	private List<Primitives> listOfPrimitives;
	private Primitives[] arrayOfPrimitives;
	
	public ComplexObject() {}
	
	public ComplexObject(Primitives primitives,
			List<Primitives> listOfPrimitives, Primitives[] arrayOfPrimitives) {
		super();
		this.primitives = primitives;
		this.listOfPrimitives = listOfPrimitives;
		this.arrayOfPrimitives = arrayOfPrimitives;
	}
	
	public String jsonString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{\"primitives\":")
			.append(primitives.jsonString())
			.append(",\"listOfPrimitives\":[");
			
		for ( int i = 0; i < listOfPrimitives.size()-1; i++ ) {
			sb.append(listOfPrimitives.get(i).jsonString()).append(',');
		}
		
		sb.append(listOfPrimitives.get(listOfPrimitives.size()-1).jsonString());
		sb.append("],\"arrayOfPrimitives\":[");
		
		for ( int i = 0; i < arrayOfPrimitives.length-1; i++ ) {
			sb.append(arrayOfPrimitives[i].jsonString()).append(',');
		}
		sb.append(arrayOfPrimitives[arrayOfPrimitives.length-1].jsonString());
		sb.append("]}");
		return sb.toString();
	}

	public Primitives getPrimitives() {
		return primitives;
	}

	public void setPrimitives(Primitives primitives) {
		this.primitives = primitives;
	}

	public List<Primitives> getListOfPrimitives() {
		return listOfPrimitives;
	}

	public void setListOfPrimitives(List<Primitives> listOfPrimitives) {
		this.listOfPrimitives = listOfPrimitives;
	}

	public Primitives[] getArrayOfPrimitives() {
		return arrayOfPrimitives;
	}

	public void setArrayOfPrimitives(Primitives[] arrayOfPrimitives) {
		this.arrayOfPrimitives = arrayOfPrimitives;
	}
	
}
