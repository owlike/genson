package com.owlike.genson.ext.scala

import java.lang.reflect.Type
import com.owlike.genson.reflect.TypeUtil
import com.owlike.genson.stream.{ObjectReader, ObjectWriter, ValueType}
import com.owlike.genson.annotation.{HandleClassMetadata, HandleNull}
import com.owlike.genson.{Factory, Context, Converter}

class OptionConverterFactory extends Factory[Converter[Option[AnyRef]]] {

  def create(`type`: Type, genson: Genson): Converter[Option[AnyRef]] = {
    val typeOfValue: Type = TypeUtil.typeOf(0, `type`)
    return new OptionConverter[AnyRef](genson.provideConverter(typeOfValue))
  }
}

@HandleNull
@HandleClassMetadata
class OptionConverter[T](valueConverter: Converter[T]) extends Converter[Option[T]] {

  def serialize(value: Option[T], writer: ObjectWriter, ctx: Context) {
    if (value == null || value.isDefined) {
      valueConverter.serialize(value.get, writer, ctx)
    } else writer.writeNull
  }

  def deserialize(reader: ObjectReader, ctx: Context): Option[T] = {
    if (ValueType.NULL == reader.getValueType) {
      return None
    }
    return Some(valueConverter.deserialize(reader, ctx))
  }
}
