package com.owlike.genson.convert

import java.lang.reflect.Type
import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.stream.{ObjectReader, ObjectWriter, ValueType}
import com.owlike.genson.annotation.{HandleClassMetadata, HandleNull}
import com.owlike.genson.{Genson, Factory, Context, Converter}

class OptionConverterFactory extends Factory[Converter[Option[AnyRef]]] {

  def create(genType: Type, genson: Genson): Converter[Option[AnyRef]] = {
    if (None.getClass.equals(getRawClass(genType))) NoneConverter
    else {
      val typeOfValue: Type = typeOf(0, genType)
      new OptionConverter[AnyRef](genson.provideConverter(typeOfValue))
    }
  }
}

@HandleNull
@HandleClassMetadata
object NoneConverter extends Converter[Option[AnyRef]] {
  def serialize(value: Option[AnyRef], writer: ObjectWriter, ctx: Context) {
    if (value != None) throw new IllegalStateException()
    else writer.writeNull()
  }

  def deserialize(reader: ObjectReader, ctx: Context): Option[AnyRef] = {
    if (reader.getValueType != ValueType.NULL) throw new IllegalStateException()
    else None
  }
}

@HandleNull
@HandleClassMetadata
class OptionConverter[T](valueConverter: Converter[T]) extends Converter[Option[T]] {

  def serialize(value: Option[T], writer: ObjectWriter, ctx: Context) {
    if (value != null || value.isDefined) {
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
