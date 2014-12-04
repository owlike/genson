package com.owlike.genson

import java.lang.reflect.{Type => JType, GenericDeclaration, TypeVariable, GenericArrayType, ParameterizedType}
import java.io.{Reader => JReader, _}
import java.net.URL

import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.stream.{ObjectWriter, ObjectReader}

class ScalaGenson(val genson: Genson) extends AnyVal {

  def toJson[T: Manifest](value: T): String = genson.serialize(value, GenericType.of(toJavaType))

  def toJsonBytes[T: Manifest](value: T): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val objectWriter = genson.createWriter(baos)

    genson.serialize(value, toJavaType, objectWriter, new Context(genson))
    baos.toByteArray()
  }

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

  private[genson] def toJavaType(implicit m: Manifest[_]): JType = {
    if (m.runtimeClass.isArray) {
      m.typeArguments
        .headOption
        .map(m => new ScalaGenericArrayType(toJavaType(m)))
        .getOrElse(m.runtimeClass)
    } else if (m.typeArguments.nonEmpty) {
      new ScalaParameterizedType(None, m.runtimeClass, m.typeArguments.map(m => toJavaType(m)).toArray)
    } else {
      if (m.runtimeClass.isPrimitive) wrap(m.runtimeClass)
      else m.runtimeClass
    }
  }
}

private[genson] class ScalaTypeVariable(name: String, bounds: Array[JType], decl: Class[_])
  extends TypeVariable[Class[_]] {

  def getBounds: Array[JType] = bounds

  def getGenericDeclaration: Class[_] = decl

  def getName: String = name
}

private[genson] class ScalaGenericArrayType(componentType: JType) extends GenericArrayType {
  def getGenericComponentType: JType = componentType
}

private[genson] class ScalaParameterizedType(val ownerType: Option[JType], val rawType: JType, val typeArgs: Array[JType])
  extends ParameterizedType {

  def getOwnerType: JType = ownerType.getOrElse(null)

  def getRawType: JType = rawType

  def getActualTypeArguments: Array[JType] = typeArgs

  def canEqual(other: Any): Boolean = other.isInstanceOf[ParameterizedType]

  override def equals(other: Any): Boolean = other match {
    case that: ParameterizedType =>
      (this canEqual that) &&
        ownerType.exists(_== that.getOwnerType) &&
        rawType == that.getRawType &&
        typeArgs == that.getActualTypeArguments
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(ownerType, rawType, typeArgs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
