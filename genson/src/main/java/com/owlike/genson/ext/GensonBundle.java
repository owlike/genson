package com.owlike.genson.ext;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualConverterFactory;
import com.owlike.genson.reflect.BeanDescriptorProvider;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanPropertyFactory;
import com.owlike.genson.reflect.PropertyNameResolver;

/**
 * Bundles allow to package all kind of Genson customizations into a single module and register
 * them all together. Extensions are registered using Genson.Builder.
 * <p/>
 * <pre>
 * Genson genson = new GensonBuilder().with(new SuperCoolExtension()).create();
 * </pre>
 * <p/>
 * Extension configuration is mixed with user custom configuration (no way to distinguish them),
 * however user custom config. has preference over bundle configuration. This means that you can
 * override bundle configuration with custom one.
 * <p/>
 *
 * <b>Important note, bundles must be registered first before any other configuration.</b>
 *
 * This part of the API is still in beta, it could change in the future in order to make it more
 * powerful.
 *
 * @author eugen
 */
public abstract class GensonBundle {
  /**
   * This method does not provide any guarantee to when it is called: before user config, during,
   * or after. Thus it should not rely on accessor methods from GensonBuilder they might not reflect
   * the final configuration. Use the builder to register your components.
   */
  public abstract void configure(GensonBuilder builder);

  public BeanDescriptorProvider createBeanDescriptorProvider(ContextualConverterFactory contextualConverterFactory,
                                                             BeanPropertyFactory propertyFactory,
                                                             BeanMutatorAccessorResolver propertyResolver,
                                                             PropertyNameResolver nameResolver,
                                                             GensonBuilder builder) {
    return null;
  }
}
