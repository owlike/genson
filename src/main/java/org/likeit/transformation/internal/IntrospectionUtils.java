package org.likeit.transformation.internal;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;


public final class IntrospectionUtils {
	
	// TODO 
	/**
	 * Regarde si oClazz a implemente une interface de type clazz avec un seul argument generique (le type parameter), par exemple Serializer<List<Integer>>
	 * @param clazz la classe que l'on cherche (ex Serializer)
	 * @param parameter le type generique attendu List<Integer>
	 * @param oClazz - la classe implementant p-e cette interface
	 * @return le type trouve qui en l'occurence serait Serializer<Collection<Number>> si la classe implemente l'interface Serializer de cette facon, null sinon
	 */
	public static Type lookupInterfaceWithGenerics(Class<?> clazz, Type parameter, Class<?> oClazz, boolean strictMatch) {
		if ( oClazz == null ) return null;
		
		Type[] types = oClazz.getGenericInterfaces();
				
		for ( Type t : types ) {
			if ( t instanceof ParameterizedType ) {
				ParameterizedType tmp = (ParameterizedType) t;
				if ( clazz.equals((Class<?>) tmp.getRawType()) ) {
					Type[] parameterizedTypes = tmp.getActualTypeArguments();
					
					if ( parameterizedTypes != null && parameterizedTypes.length > 0 ) {
						for ( int i = 0; i < parameterizedTypes.length; i++ ) {
    						Type paramType = parameterizedTypes[i];
    						if ( match(parameter, paramType, strictMatch) ) 
    							return tmp;
						}
					}
				} else {
					Type returnType = lookupInterfaceWithGenerics(clazz, parameter, ((Class<?>) tmp.getRawType()).getSuperclass(), strictMatch);
					if ( returnType != null ) return returnType;
				}
			}
		}
		
		return lookupInterfaceWithGenerics(clazz, parameter, oClazz.getSuperclass(), strictMatch);
	}
	
	public static Class<?> getRawType(Type type) {
		if ( type instanceof Class<?> )
			return (Class<?>) type;
		else if ( type instanceof ParameterizedType ) {
			ParameterizedType pType = (ParameterizedType) type;
			return (Class<?>) pType.getRawType();
		} throw new UnsupportedOperationException("Case not implemented!");
	}
	
	// TODO gerer les autres types? est-ce qu'il a bien d'autres cas possibles???
	public static Type getCollectionType(Type type) {
		if ( type instanceof ParameterizedType ) {
			ParameterizedType pType = (ParameterizedType) type;
			Type[] argTypes = pType.getActualTypeArguments();
			if ( argTypes == null || argTypes.length != 1 ) throw new IllegalArgumentException("Type " + type + " is not a collection!");
			return argTypes[0];
		} else if ( type instanceof GenericArrayType ) {
			GenericArrayType gaType = (GenericArrayType) type;
			return gaType.getGenericComponentType();
		} else if ( type instanceof Class<?> ) {
			Class<?> clazz = (Class<?>) type;
			if ( clazz.isArray() ) return clazz.getComponentType();
		}
		
		throw new IllegalArgumentException("Could not extract argument type of " + type);
	}
	
	/**
	 * Compare les deux types en profondeur, si ce sont des ParameterizedType on va comparer de facon recursive.
	 * On compare les classes avec isAssignableFrom, donc si type est List<Integer> et oType Collection<Number>,
	 * cette methode va regarder que les types de type peuvent etre castes en oType. Donc List en Collection et Integer en Number.
	 * 
	 * @param type le type connu
	 * @param oType un type qui serait egal ou atteignable a partir de type (voir exemple)
	 * @return vrai ou faux
	 */
	public static boolean match(Type type, Type oType, boolean strictMatch) {
		boolean match = false;
		
//		if ( type.getClass().equals(oType.getClass()) ) { // on check qu'on compare le meme type (parametrized, class, etc)
			if ( type instanceof Class ) {
				Class<?> oClass = null;
				if ( oType instanceof ParameterizedType ) {
					oClass = (Class<?>) ((ParameterizedType) oType).getRawType();
					match = strictMatch ? oClass.equals((Class<?>) type) : oClass.isAssignableFrom((Class<?>) type);
				} else if ( oType instanceof WildcardType ) {
					WildcardType wOType = (WildcardType) oType;
					if ( wOType.getUpperBounds().length > 0 ) match = match(type, wOType.getUpperBounds()[0], false);
					else if ( wOType.getLowerBounds().length > 0 ) match = match(wOType.getLowerBounds()[0], type, false);
				} else {
					oClass = (Class<?>) oType;
					match = strictMatch ? oClass.equals((Class<?>) type) : oClass.isAssignableFrom((Class<?>) type);
				}
				
			} else if ( type.getClass().equals(oType.getClass()) && type instanceof ParameterizedType ) {
				ParameterizedType t1 = (ParameterizedType) type;
				ParameterizedType t2 = (ParameterizedType) oType;
				Type[] t1s = t1.getActualTypeArguments();
				Type[] t2s = t2.getActualTypeArguments();
				
				match = match(t1.getRawType(), t2.getRawType(), strictMatch);
				
				if ( t1s.length == t2s.length ) {
					for ( int i = 0; i < t1s.length && match; i++ )
						match = match(t1s[i], t2s[i], strictMatch);
				}
			}
//		} 
		
		return match;
	}
	
	public static List<DataAccessor> introspectBeanModifiers(Class<?> clazz) {
		List<DataAccessor> setters = new ArrayList<DataAccessor>();
		Method[] methods = clazz.getDeclaredMethods();
		
		for ( Method m : methods ) {
			int modifier = m.getModifiers();
			boolean ok = Modifier.isPublic(modifier) && !Modifier.isTransient(modifier)
					 && !Modifier.isAbstract(modifier) && !Modifier.isStatic(modifier);
			if ( ok && m.getName().startsWith(DataAccessor.SET_PREFIX) && m.getName().length() > (DataAccessor.SET_PREFIX.length()+1) ) {
				setters.add(new DataAccessor(m));
			}
			
		}
		
		return setters;
	}
	
	public static List<DataAccessor> introspectBeanAccessors(Class<?> clazz, Object[] params) {
		Method[] methods = clazz.getDeclaredMethods();
		List<DataAccessor> getters = new ArrayList<DataAccessor>();
		
		for ( Method m : methods ) {
			int modifier = m.getModifiers();
			boolean ok = Modifier.isPublic(modifier) && !Modifier.isTransient(modifier)
					 && !Modifier.isAbstract(modifier) && !Modifier.isStatic(modifier);
			
			if ( ok && ((m.getName().length() > (DataAccessor.GET_PREFIX.length()+1) && m.getName().startsWith(DataAccessor.GET_PREFIX))
					|| (m.getName().length() > (DataAccessor.IS_PREFIX.length()+1) && m.getName().startsWith(DataAccessor.IS_PREFIX)
							&& (Boolean.class.equals(m.getGenericReturnType()) 
							|| boolean.class.equals(m.getGenericReturnType())))) ) {
				Class<?>[] methodParams = m.getParameterTypes();
				
				if ( methodParams.length > 0 ) {
					ok = (params != null && methodParams.length == params.length);
					for ( int i = 0; i < methodParams.length && ok; i++ ) ok = methodParams[i].isAssignableFrom(params[i].getClass());
				}
				
				if ( ok ) {
					getters.add(new DataAccessor(m));
				}
			}
		}
		
		// pour gerer les methods herites
		Class<?> superClass = clazz.getSuperclass();
		if ( superClass != null && !superClass.isAssignableFrom(Object.class)  )
			getters.addAll(introspectBeanAccessors(superClass, params));
		
		return getters;
	}
}
