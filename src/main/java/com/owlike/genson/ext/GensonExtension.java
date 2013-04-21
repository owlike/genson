package com.owlike.genson.ext;

import java.util.List;

import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.internal.ContextualFactory;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanPropertyFactory;
import com.owlike.genson.reflect.PropertyNameResolver;

/**
 * Extensions allow to package all kind of Genson customizations into a single module and register them all together. 
 * Extensions are registered using Genson.Builder.
 * <pre>
 * Genson genson = new Genson.Builder().with(new SuperCoolExtension()).create();
 * </pre>
 * 
 * GensonExtension gives you the ability to define new behaviors for the low level parts of Genson.
 * This part of the API is more complex and likely to change.
 * 
 * @author eugen
 * 
 */
public abstract class GensonExtension {
	public void registerBeanMutatorAccessorResolvers(List<BeanMutatorAccessorResolver> resolvers) {
	}

	public void registerPropertyNameResolvers(List<PropertyNameResolver> resolvers) {
	}

	public void registerConverters(List<Converter<?>> converters) {
	}

	public void registerConverterFactories(List<Factory<? extends Converter<?>>> factories) {
	}

	/**
	 * Allows you to register new BeanPropertyFactory responsible of creating BeanProperty accessors, mutators and BeanCreators.
	 */
	public void registerBeanPropertyFactories(List<BeanPropertyFactory> factories) {
	}

	/**
	 * ContextualFactory is actually in a beta status (thus in internal package), it will not be removed, but for sure it will
	 * move in another package and might be refactored.
	 */
	public void registerContextualFactories(List<ContextualFactory<?>> factories) {
	}
}
