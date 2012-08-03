package org.genson.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.genson.GenericType;
import org.junit.Test;

import static com.google.gson.internal.$Gson$Types.*;

import static org.genson.reflect.TypeUtil.*;
import static org.junit.Assert.*;

public class TypeUtilTest {
	@SuppressWarnings("rawtypes")
	@Test public void testGenericType() {
		assertTrue(match(new GenericType<List<Number>>(){}.getType(), tListE, true));
		try {
			new GenericType(){};
			fail();
		} catch (RuntimeException re) { }
	}
	
	@Test public void testGetRawType() {
		assertEquals(ParametrizedClass.class, getRawClass(ParametrizedClass.class));
		assertEquals(Number.class, getRawClass(Number.class));
	}
	
	@Test public void testGetCollectionType() throws SecurityException, NoSuchFieldException {
		assertEquals(Object.class, getCollectionType(Collection.class));
		assertEquals(Number.class, getCollectionType(tListN));
		assertEquals(Number.class, getCollectionType(tListE));
		assertEquals(Object.class, getCollectionType(tListF));
		assertEquals(Object.class, getCollectionType(tListI));
		assertEquals(Number.class, getCollectionType(tListIEN));
		ParameterizedType colType = (ParameterizedType)getCollectionType(tListCN);
		assertEquals(Collection.class, colType.getRawType());
	}
	
	@Test public void testParameterOf() throws SecurityException, NoSuchFieldException {
		assertNull(typeOf(0, Number.class));
		assertEquals( Number.class, expand(typeOf(0, tListN), ParametrizedClass.class));
		
		// doit retourner Number, car dans la declaration de la classe ParametrizedClass, E extends Number
		assertEquals(Number.class, expand(typeOf(0, tListE), ParametrizedClass.class));
		// doit retourner Object, car dans la declaration de la classe ParametrizedClass, il n'y a aucune borne pour F
		assertEquals(Object.class, expand(typeOf(0, tListF), ParametrizedClass.class));
		// doit retourner Object, car c'est un wildcard sans borne
		assertEquals(Object.class, expand(typeOf(0, tListI), ParametrizedClass.class));
		// doit retourner Number, car la borne sup du wildcard est Number
		assertEquals(Number.class, expand(typeOf(0, tListIEN), ParametrizedClass.class));
		
		/* doit retourner Object puisque ? super X, correspond a toutes les superclasses de X dont Object
		 * par contre dans la methode match il faut faire un peu plus de choses car ce n'est pas le cas! */
		assertEquals(Object.class, expand(typeOf(0, tListISI), ParametrizedClass.class));
		
		/*
		 *  doit retourner Collection<?> equivalent a Collection<Object>
		 *  car la borne sup du type C est Collection<?> dans la definition de la classe
		 */
		Type wildcardCollectionType = expand(typeOf(0, tListC), ParametrizedClass.class);
		assertEquals(Collection.class, getRawClass(wildcardCollectionType));
		assertEquals(Object.class, expand(typeOf(0, wildcardCollectionType), ParametrizedClass.class));
		
		wildcardCollectionType = expand(typeOf(0, tListCN), ParametrizedClass.class);
		assertEquals(Collection.class, getRawClass(wildcardCollectionType));
		assertEquals(Number.class, expand(typeOf(0, wildcardCollectionType), ParametrizedClass.class));
		
		// equivalent a <?> <Object> et rien
		assertEquals(Object.class, expand(typeOf(0, List.class), ParametrizedClass.class));
	}
	
	@Test public void testTypeMatch() throws SecurityException, NoSuchFieldException {
		assertTrue(match(Integer.class, Number.class, false));
		assertTrue(match(Number.class, Number.class, false));
		assertFalse(match(Number.class, Integer.class, false));
		assertTrue(match(Number.class, Number.class, true));
		assertFalse(match(Double.class, Number.class, true));
		
		assertTrue(match(tListEInt, tListE, false));
		assertFalse(match(tListEInt, tListE, true));
		
		assertTrue(match(tListEInt, tColEInt, false));
		assertFalse(match(tListEInt, tColEInt, true));
		
		assertTrue(match(new Number[0].getClass(), tArrayE, true));
		assertFalse(match(new Integer[0].getClass(), tArrayE, true));
		assertTrue(match(new Integer[0].getClass(), tArrayF, false));
		assertFalse(match(new Integer[0].getClass(), tArrayF, true));
		assertFalse(match(tArrayC, tArrayCN, false));
		assertTrue(match(tArrayCN, tArrayC, false));
	}
	
	@Test public void testLookupWithGenerics() {
		assertNotNull(lookupWithGenerics(Collection.class, Object.class, List.class, false));
		assertNotNull(lookupWithGenerics(Collection.class, Number.class, List.class, false));
		assertNull(lookupWithGenerics(Collection.class, Number.class, List.class, true));
		
		assertNotNull(lookupWithGenerics(GenericInterface.class, Integer.class, ImplI.class, false));
		assertNull(lookupWithGenerics(GenericInterface.class, Integer.class, ImplI.class, true));
		assertNotNull(lookupWithGenerics(GenericInterface.class, Number.class, ImplI.class, false));
		assertNotNull(lookupWithGenerics(GenericInterface.class, Number.class, ImplI.class, true));

		assertNotNull(lookupWithGenerics(GenericClass.class, Integer.class, ImplI.class, true));
		assertNull(lookupWithGenerics(GenericClass.class, Number.class, ImplI.class, false));
		
	}

	@Test public void testMapGenerics() {
		assertEquals(String.class, expand(typeOf(0, tMapE), null));
		assertEquals(Number.class, expand(typeOf(1, tMapE), ParametrizedClass.class));
		assertFalse(match(tMapE, tMapI, false));
		assertFalse(match(tMapE, tMapI, true));
		assertTrue(match(tMapI, tMapE, false));
		assertFalse(match(tMapI, tMapE, true));
	}
	
	private static interface GenericInterface<E> {}
	
	private static class GenericClass<E> {}
	
	private static class ImplI extends GenericClass<Integer> implements GenericInterface<Number> {
	}
	
	@SuppressWarnings("unused")
	private static class ParametrizedClass<E extends Number, F, C extends Collection<?>, CN extends Collection<? extends Number>> {
		
		public List<Number> listN;
		public List<E> listE;
		public List<F> listF;
		public List<?> listI;
		public List<? extends Number> listIEN;
		public List<? super Integer> listISI;
		public List<C> listC;
		public List<CN> listCN;
		public List<Integer> listInt;
		public List<? extends Integer> listEInt;
		public Collection<? extends Integer> colEInt;
		public Map<String, E> mapE;
		public Map<String, ? extends Integer> mapI;
		public E[] arrayE;
		public F[] arrayF;
		public C[] arrayC;
		public CN[] arrayCN;
	}
	
	public static Type tListN;
	public static Type tListE;
	public static Type tListF;
	public static Type tListI;
	public static Type tListIEN;
	public static Type tListISI;
	public static Type tListC;
	public static Type tListCN;
	public static Type tListInt;
	public static Type tListEInt;
	public static Type tColEInt;
	public static Type tMapE;
	public static Type tMapI;
	public static Type tArrayE;
	public static Type tArrayF;
	public static Type tArrayC;
	public static Type tArrayCN;
	static {
		try {
			tListN = ParametrizedClass.class.getField("listN").getGenericType();
			tListE = ParametrizedClass.class.getField("listE").getGenericType();
			tListF = ParametrizedClass.class.getField("listF").getGenericType();
			tListI = ParametrizedClass.class.getField("listI").getGenericType();
			tListIEN = ParametrizedClass.class.getField("listIEN").getGenericType();
			tListISI = ParametrizedClass.class.getField("listISI").getGenericType();
			tListC = ParametrizedClass.class.getField("listC").getGenericType();
			tListCN = ParametrizedClass.class.getField("listCN").getGenericType();
			tListInt = ParametrizedClass.class.getField("listInt").getGenericType();
			tListEInt = ParametrizedClass.class.getField("listEInt").getGenericType();
			tColEInt = ParametrizedClass.class.getField("colEInt").getGenericType();
			tMapE = ParametrizedClass.class.getField("mapE").getGenericType();
			tMapI = ParametrizedClass.class.getField("mapI").getGenericType();
			tArrayE = ParametrizedClass.class.getField("arrayE").getGenericType();
			tArrayF = ParametrizedClass.class.getField("arrayF").getGenericType();
			tArrayC = ParametrizedClass.class.getField("arrayC").getGenericType();;
			tArrayCN = ParametrizedClass.class.getField("arrayCN").getGenericType();;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
}
