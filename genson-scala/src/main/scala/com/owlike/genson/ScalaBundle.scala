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
      .useDefaultValue(None, classOf[Option[_]])
      .withConverterFactory(new TraversableConverterFactory())
      .withConverterFactory(new MapConverterFactory())
      .withConverterFactory(ScalaUntypedConverterFactory)
      .withConverterFactory(new TupleConverterFactory())
      .withConverterFactory(new OptionConverterFactory())
      .withBeanPropertyFactory(new ScalaBeanPropertyFactory(builder.getClassLoader))
      .`with`(new StandardMutaAccessorResolver(VisibilityFilter.PRIVATE, VisibilityFilter.NONE, VisibilityFilter.PRIVATE))
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
    new CaseClassDescriptorProvider(contextualConverterFactory,
      beanPropertyFactory,
      propertyResolver,
      propertyNameResolver,
      useOnlyConstructorFields)
  }
}

object ScalaBundle {
  def apply() = new ScalaBundle()
}
