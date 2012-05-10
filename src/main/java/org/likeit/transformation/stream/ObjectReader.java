package org.likeit.transformation.stream;

import java.io.IOException;

public interface ObjectReader {
	public void beginObject() throws IOException;
	public void endObject() throws IOException;
	public void beginArray() throws IOException;
	public void endArray() throws IOException;
	
	/**
	 * 
	 * @return avance sur la suivante valeur (ou nom+valeur), si on arrive à la fin d'un tableau
	 * ou d'un objet ça renvoie null. Il faut alors appeler endArray ou endObject pour passer a la suite au niveau
	 * d'au dessus.
	 */
	public ObjectReader next() throws IOException;
	public boolean hasNext() throws IOException;
	
	public String name() throws IOException;
	public String value() throws IOException;
}
