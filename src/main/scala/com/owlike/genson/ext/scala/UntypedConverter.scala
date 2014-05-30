package com.owlike.genson.ext.scala

import com.owlike.genson._
import com.owlike.genson.stream.{ValueType, ObjectWriter, ObjectReader}
import java.lang.reflect.Type
import com.owlike.genson.reflect.TypeUtil

object ScalaUntypedConverterFactory extends Factory[Converter[Any]] {

  object ScalaUntypedConverter extends Converter[Any] {

    def deserialize(reader: ObjectReader, ctx: Context): Any = {
      reader.getValueType match {
        case ValueType.OBJECT => ctx.genson.deserialize(GenericType.of(classOf[Map[_, _]]), reader, ctx)
        case ValueType.ARRAY => ctx.genson.deserialize(GenericType.of(classOf[List[_]]), reader, ctx)
        case v => ctx.genson.deserialize(GenericType.of(v.toClass), reader, ctx)
      }

    }

    def serialize(obj: Any, writer: ObjectWriter, ctx: Context) {
      if (classOf[AnyRef] == obj.getClass) throw new UnsupportedOperationException("Serialization of type Object is not supported by default serializers.")
      ctx.genson.serialize(obj, obj.getClass, writer, ctx)
    }
  }

  def create(genType: Type, genson: Genson): Converter[Any] = {
    if (classOf[Object].equals(TypeUtil.getRawClass(genType))) {
      ScalaUntypedConverter
    } else null
  }
}
