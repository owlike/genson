package com.owlike.genson.ext.scala

import com.owlike.genson.ext.GensonBundle
import com.owlike.genson._
import com.owlike.genson.reflect._
import java.lang.reflect.{Modifier, Type}
import java.util
import com.owlike.genson.annotation.{JsonProperty, JsonCreator}

import scala.collection.JavaConversions._

class ScalaBundle extends GensonBundle {

    def configure(builder: GensonBuilder) {
    }

}

class CaseClassDescriptorProvider(ctxConverterFactory: AbstractBeanDescriptorProvider.ContextualConverterFactory,
                                  propertyFactory: BeanPropertyFactory,
                                  mutatorAccessorResolver: BeanMutatorAccessorResolver,
                                  nameResolver: PropertyNameResolver,
                                  useOnlyConstructorFields: Boolean)
    extends BaseBeanDescriptorProvider(ctxConverterFactory, propertyFactory, mutatorAccessorResolver, nameResolver, false, true, true)
    with Factory[Converter[Product]] {

    def create(`type`: Type, genson: Genson): Converter[Product] = provide(`type`, genson).asInstanceOf[Converter[Product]]

    protected override def checkAndMerge(ofType: Type, creators: util.List[BeanCreator]): BeanCreator = {
        val ctr = super.checkAndMerge(ofType, creators)
        if (creators.size() > 1 && !ctr.isAnnotationPresent(classOf[JsonCreator]))
            throw new JsonBindingException("Case classes with multiple constructor must indicate what constructor to use with @JsonCreator annotation.")
        ctr
    }

    protected override def mergeAccessorsWithCreatorProperties(ofType: Type, accessors: util.Map[String, PropertyAccessor], creator: BeanCreator) {
        super.mergeAccessorsWithCreatorProperties(ofType, accessors, creator)

        if (useOnlyConstructorFields) {
            val ctrProps = creator.getProperties

            // don't serialize properties that are not used in a constructor and are final and note annotated with JsonProperty
            for (
                (name, prop) <- accessors.toMap
                if !ctrProps.containsKey(name) && isFinal(prop) && prop.getAnnotation(classOf[JsonProperty]) == null
            ) accessors.remove(name)
        }
    }

    private def isFinal(prop: PropertyAccessor) = (prop.getModifiers & Modifier.FINAL) != 0
}
