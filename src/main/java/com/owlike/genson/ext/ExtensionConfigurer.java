package com.owlike.genson.ext;

import java.util.List;

import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanPropertyFactory;
import com.owlike.genson.reflect.PropertyNameResolver;

public abstract class ExtensionConfigurer {
	public void registerBeanMutatorAccessorResolvers(List<BeanMutatorAccessorResolver> resolvers) {
	}

	public void registerPropertyNameResolvers(List<PropertyNameResolver> resolvers) {
	}

	public void registerConverters(List<Converter<?>> converters) {
	}

	public void registerConverterFactories(List<Factory<? extends Converter<?>>> factories) {
	}

	public void registerBeanPropertyFactories(List<BeanPropertyFactory> factories) {
	}
}
