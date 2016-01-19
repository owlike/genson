package com.owlike.genson.convert

import java.lang.reflect.{Type => JType, Method, Modifier}
import java.util.{List => JList}

import com.owlike.genson.reflect._
import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.annotation.{JsonProperty, JsonCreator}
import com.owlike.genson.Genson

class CaseClassDescriptorProvider(ctxConverterFactory: AbstractBeanDescriptorProvider.ContextualConverterFactory,
                                  propertyFactory: BeanPropertyFactory,
                                  mutatorAccessorResolver: BeanMutatorAccessorResolver,
                                  nameResolver: PropertyNameResolver,
                                  useOnlyConstructorFields: Boolean)
  extends BaseBeanDescriptorProvider(ctxConverterFactory, propertyFactory, mutatorAccessorResolver, nameResolver,
    false, true, true) {


  override def provide[T](ofClass: Class[T], ofType: JType, genson: Genson): BeanDescriptor[T] = {
    if (classOf[Product].isAssignableFrom(ofClass)) super.provide(ofClass, ofType, genson)
    else null
  }

  protected override def checkAndMerge(ofType: JType, creators: JList[BeanCreator]): BeanCreator = {
    defaultApply(getRawClass(ofType)).flatMap {
      applyMethod =>
        scala.collection.JavaConversions.collectionAsScalaIterable(creators).find { ctr =>
          ctr.isAnnotationPresent(classOf[JsonCreator]) || isDefaultCreator(applyMethod, ctr)
        }
    }.getOrElse(super.checkAndMerge(ofType, creators))
  }

  protected override def mergeAccessorsWithCreatorProperties(ofType: JType,
                                                             accessors: JList[PropertyAccessor],
                                                             creator: BeanCreator) {

    super.mergeAccessorsWithCreatorProperties(ofType, accessors, creator)

    if (useOnlyConstructorFields) {
      val ctrProps = creator.getProperties

      // don't serialize properties that are not used in a constructor and are final and note annotated with JsonProperty
      val it = accessors.iterator()
      while (it.hasNext) {
        val prop = it.next()
        if (!ctrProps.containsKey(prop.getName)
          && isFinal(prop)
          && prop.getAnnotation(classOf[JsonProperty]) == null) it.remove()
      }
    }
  }

  protected def companionOf(clazz: Class[_]): Option[AnyRef] = try {
    val companionClassName = clazz.getName + "$"
    val companionClass = Class.forName(companionClassName)
    val moduleField = companionClass.getField("MODULE$")
    Some(moduleField.get(null))
  } catch {
    case e: Throwable => None
  }

  protected def isDefaultCreator(applyMethod: Method, ctr: BeanCreator): Boolean = {
    val ctrParams = scala.collection.JavaConversions.collectionAsScalaIterable(ctr.getProperties.entrySet()).toList
    applyMethod.getParameterTypes.zipWithIndex.forall {
      case (clazz, idx) =>
        ctrParams(idx).getValue.getType.equals(clazz)
    }
  }

  protected def defaultApply(clazz: Class[_]): Option[Method] = {
    companionOf(clazz).map {
      companion =>
        companion.getClass.getDeclaredMethods.filter(m =>
          m.getName.equals("apply") && m.getReturnType.equals(clazz)
        ).headOption
    }.flatten
  }

  private def isFinal(prop: PropertyAccessor) = (prop.getModifiers & Modifier.FINAL) != 0
}
