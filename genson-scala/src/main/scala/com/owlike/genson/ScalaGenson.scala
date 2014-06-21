package com.owlike.genson

import java.lang.reflect.{Type => JType, ParameterizedType}
import java.io.{OutputStream, Writer, InputStream, StringReader, Reader => JReader}
import java.net.URL

import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.stream.{ObjectWriter, ObjectReader}

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
