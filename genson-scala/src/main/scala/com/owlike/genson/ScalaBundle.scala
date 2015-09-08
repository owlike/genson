package com.owlike.genson

import com.owlike.genson.reflect._

import com.owlike.genson.ext.GensonBundle
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualConverterFactory
import com.owlike.genson.reflect.BeanMutatorAccessorResolver.{StandardMutaAccessorResolver, CompositeResolver}
import java.util
import com.owlike.genson.convert._

class ScalaBundle extends GensonBundle {
  private var useOnlyConstructorFields: Boolean = true

  def configure(builder: GensonBuilder) {
    builder.useConstructorWithArguments(true)
      .withConverterFactory(new TraversableConverterFactory())
      .withConverterFactory(new MapConverterFactory())
      .withConverterFactory(ScalaUntypedConverterFactory)
      .withConverterFactory(new TupleConverterFactory())
      .withConverterFactory(new OptionConverterFactory())
      .withBeanPropertyFactory(new ScalaBeanPropertyFactory(builder.getClassLoader))
  }

  def useOnlyConstructorFields(enable: Boolean): ScalaBundle = {
    useOnlyConstructorFields = enable
    this
  }

  override def createBeanDescriptorProvider(contextualConverterFactory: ContextualConverterFactory,
                                            beanPropertyFactory: BeanPropertyFactory,
                                            propertyResolver: BeanMutatorAccessorResolver,
                                            propertyNameResolver: PropertyNameResolver,
                                            builder: GensonBuilder): BeanDescriptorProvider = {
    val caseClassPropertyResolver = new CompositeResolver(util.Arrays.asList(
      new StandardMutaAccessorResolver(VisibilityFilter.PRIVATE, VisibilityFilter.NONE, VisibilityFilter.PRIVATE),
      propertyResolver)
    )

    new CaseClassDescriptorProvider(contextualConverterFactory,
      beanPropertyFactory,
      caseClassPropertyResolver,
      propertyNameResolver,
      useOnlyConstructorFields)
  }
}

object ScalaBundle {
  def apply() = new ScalaBundle()
}
