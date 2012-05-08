package org.likeit.transformation.serialization;


/**
 * 
 * Les methodes de la forme getXXX(T obj) vont etre appeles lors de la serialization en json avec en argument l'objet
 * courrant de type T (la classe du bean).
 * Les implementations doivent etre stateless et thread safe.
 * 
 * @param <T> type du bean sur lequel la view est applique
 */
public interface BeanView<T> {
	
}