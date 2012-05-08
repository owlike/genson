package org.likeit.transformation.stream;

public interface ObjectReader {
	public void beginObject();
	public void endObject();
	public void beginArray();
	public void endArray();
	
	/**
	 * 
	 * @return avance sur la suivante valeur (ou nom+valeur), si on arrive à la fin d'un tableau
	 * ou d'un objet ça renvoie null. Il faut alors appeler endArray ou endObject pour passer a la suite au niveau
	 * d'au dessus.
	 */
	public ObjectReader next();
	public boolean hasNext();
	
	public String name();
	public String value();
}
