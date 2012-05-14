package org.likeit.transformation.stream;

import java.io.IOException;

public interface ObjectReader {
	public ObjectReader beginObject() throws IOException;
	public ObjectReader endObject() throws IOException;
	public ObjectReader beginArray() throws IOException;
	public ObjectReader endArray() throws IOException;
	
	/**
	 * 
	 * @return avance sur la suivante valeur (ou nom+valeur), si on arrive à la fin d'un tableau
	 * ou d'un objet ça renvoie null. Il faut alors appeler endArray ou endObject pour passer a la suite au niveau
	 * d'au dessus.
	 */
	public TokenType next() throws IOException;
	public boolean hasNext() throws IOException;
	
	public TokenType getTokenType();
	public String name() throws IOException;
	public String value() throws IOException;
}
