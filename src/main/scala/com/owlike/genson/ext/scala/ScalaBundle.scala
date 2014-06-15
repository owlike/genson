package com.owlike.genson.ext.scala

import java.lang.reflect.{Type => JType, Method, Modifier, ParameterizedType}
import java.io.{OutputStream, Writer, InputStream, StringReader, Reader => JReader}
import java.net.URL
import java.util.{List => JList, Map => JMap}

import com.owlike.genson.{Context, GenericType}

import com.owlike.genson.reflect._

import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.stream.{ObjectWriter, ObjectReader}
import com.owlike.genson.annotation.{JsonProperty, JsonCreator}
import com.owlike.genson.ext.GensonBundle
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualConverterFactory
import com.owlike.genson.reflect.BeanMutatorAccessorResolver.{StandardMutaAccessorResolver, CompositeResolver}
import java.util

class ScalaBundle extends GensonBundle {
  private var useOnlyConstructorFields: Boolean = true

  def configure(builder: GensonBuilder) {
    builder.useConstructorWithArguments(true)
      .withConverterFactory(new TraversableConverterFactory())
      .withConverterFactory(new MapConverterFactory())
      .withConverterFactory(ScalaUntypedConverterFactory)
      .withConverterFactory(new TupleConverterFactory())
      .withConverterFactory(new OptionConverterFactory())
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

  protected[scala] def getTraversableType(genType: JType): JType = {

    if (genType.isInstanceOf[Class[_]]) {
      val clazz: Class[_] = genType.asInstanceOf[Class[_]]
      if (clazz.isArray) return clazz.getComponentType
      else if (classOf[Traversable[_]].isAssignableFrom(clazz)) {
        val expandedType = expandType(lookupGenericType(classOf[Traversable[_]], clazz), clazz)
        return typeOf(0, expandedType)
      }
    } else if (genType.isInstanceOf[ParameterizedType] && classOf[Traversable[_]].isAssignableFrom(getRawClass(genType))) {
      return typeOf(0, genType)
    }

    throw new IllegalArgumentException("Could not extract parametrized type, are you sure it is a Traversable or an Array?")
  }
}

class ScalaGenson(val genson: Genson) extends AnyVal {

  def toJson[T: Manifest](value: T): String = genson.serialize(value, GenericType.of(toJavaType))

  def toJsonBytes[T: Manifest](value: T): Array[Byte] = genson.serializeBytes(value, GenericType.of(toJavaType))

  def toJson[T: Manifest](value: T, writer: Writer): Unit = toJson(value, genson.createWriter(writer))

  def toJson[T: Manifest](value: T, os: OutputStream): Unit = toJson(value, genson.createWriter(os))

  def toJson[T: Manifest](value: T, writer: ObjectWriter): Unit = genson.serialize(value, toJavaType, writer, new Context(genson))

  def fromJson[T: Manifest](json: String): T = fromJson(genson.createReader(new StringReader(json)))

  def fromJson[T: Manifest](jsonUrl: URL): T = fromJson(genson.createReader(jsonUrl.openStream()))

  def fromJson[T: Manifest](json: JReader): T = fromJson(genson.createReader(json))

  def fromJson[T: Manifest](json: InputStream): T = fromJson(genson.createReader(json))

  def fromJson[T: Manifest](json: Array[Byte]): T = fromJson(genson.createReader(json))

  def fromJson[T: Manifest](reader: ObjectReader): T = {
    genson.deserialize(GenericType.of(toJavaType), reader, new Context(genson)).asInstanceOf[T]
  }

  private def toJavaType(implicit m: Manifest[_]): JType = {
    if (m.typeArguments.nonEmpty) {
      new ScalaParameterizedType(None, m.runtimeClass, m.typeArguments.map(m => toJavaType(m)).toArray)
    } else {
      if (m.runtimeClass.isPrimitive) wrap(m.runtimeClass)
      else m.runtimeClass
    }
  }
}

class CaseClassDescriptorProvider(ctxConverterFactory: AbstractBeanDescriptorProvider.ContextualConverterFactory,
                                  propertyFactory: BeanPropertyFactory,
                                  mutatorAccessorResolver: BeanMutatorAccessorResolver,
                                  nameResolver: PropertyNameResolver,
                                  useOnlyConstructorFields: Boolean)
  extends BaseBeanDescriptorProvider(ctxConverterFactory, propertyFactory, mutatorAccessorResolver, nameResolver, false, true, true) {


  override def provide[T](ofClass: Class[T], ofType: JType, genson: Genson): BeanDescriptor[T] = {
    if (classOf[Product].isAssignableFrom(ofClass)) super.provide(ofClass, ofType, genson)
    else null
  }

  protected override def checkAndMerge(ofType: JType, creators: JList[BeanCreator]): BeanCreator = {
    defaultApply(getRawClass(ofType)).map {
      applyMethod =>
        scala.collection.JavaConversions.collectionAsScalaIterable(creators).find {
          ctr =>
            ctr.isAnnotationPresent(classOf[JsonCreator]) || isDefaultCreator(applyMethod, ctr)
        }
    }.flatten.getOrElse(super.checkAndMerge(ofType, creators))
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

private class ScalaParameterizedType(val ownerType: Option[JType], val rawType: JType, val typeArgs: Array[JType])
  extends ParameterizedType {

  def getOwnerType: JType = ownerType.getOrElse(null)

  def getRawType: JType = rawType

  def getActualTypeArguments: Array[JType] = typeArgs

  def canEqual(other: Any): Boolean = other.isInstanceOf[ScalaParameterizedType]

  override def equals(other: Any): Boolean = other match {
    case that: ScalaParameterizedType =>
      (that canEqual this) &&
        ownerType == that.ownerType &&
        rawType == that.rawType &&
        typeArgs == that.typeArgs
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(ownerType, rawType, typeArgs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}