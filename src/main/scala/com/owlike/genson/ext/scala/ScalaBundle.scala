package com.owlike.genson.ext.scala

import java.lang.reflect.{Type => JType, Modifier, ParameterizedType}
import java.io.{OutputStream, Writer, InputStream, StringReader, Reader => JReader}
import java.net.URL
import java.util.{List => JList, Map => JMap}

import scala.collection.JavaConversions._

import com.owlike.genson.{Context, GenericType, Factory, Converter}

import com.owlike.genson.reflect.{
    BaseBeanDescriptorProvider,
    AbstractBeanDescriptorProvider,
    BeanPropertyFactory,
    BeanMutatorAccessorResolver,
    PropertyNameResolver,
    BeanCreator,
    PropertyAccessor
}

import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.stream.{ObjectWriter, ObjectReader}
import com.owlike.genson.annotation.{JsonProperty, JsonCreator}
import com.owlike.genson.ext.GensonBundle


class ScalaBundle extends GensonBundle {

  def configure(builder: GensonBuilder) {
    builder.useConstructorWithArguments(true)
      .withConverterFactory(new TraversableConverterFactory())
      .withConverterFactory(new MapConverterFactory())
      .withConverterFactory(ScalaUntypedConverterFactory)
      .withConverterFactory(new TupleConverterFactory())
      .withConverterFactory(new OptionConverterFactory())
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
    extends BaseBeanDescriptorProvider(ctxConverterFactory, propertyFactory, mutatorAccessorResolver, nameResolver, false, true, true)
    with Factory[Converter[Product]] {

    def create(`type`: JType, genson: Genson): Converter[Product] = provide(`type`, genson).asInstanceOf[Converter[Product]]

    protected override def checkAndMerge(ofType: JType, creators: JList[BeanCreator]): BeanCreator = {
        val ctr = super.checkAndMerge(ofType, creators)
        if (creators.size() > 1 && !ctr.isAnnotationPresent(classOf[JsonCreator]))
            throw new JsonBindingException("Case classes with multiple constructor must indicate what constructor to use with @JsonCreator annotation.")
        ctr
    }

    protected override def mergeAccessorsWithCreatorProperties(ofType: JType, accessors: JMap[String, PropertyAccessor], creator: BeanCreator) {
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