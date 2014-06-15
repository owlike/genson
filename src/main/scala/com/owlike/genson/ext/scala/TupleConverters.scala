package com.owlike.genson.ext.scala

import com.owlike.genson.{Factory, Converter, Context}
import java.lang.reflect.{Constructor, ParameterizedType, Type}
import com.owlike.genson.reflect.TypeUtil
import com.owlike.genson.stream.{ObjectWriter, ObjectReader}
import com.owlike.genson.annotation.HandleClassMetadata

class TupleConverterFactory extends Factory[Converter[_ <: Any]] {

  def create(genType: Type, genson: Genson): Converter[_ <: Any] = {
    val rawClass = TypeUtil.getRawClass(genType)
    if (rawClass.getName.startsWith("scala.Tuple")) {
      val ctr = rawClass.getDeclaredConstructors.head

      val converters = genType match {
        case parametrizedType: ParameterizedType => parametrizedType.getActualTypeArguments.map(genson.provideConverter[AnyRef])
        case _ => ctr.getGenericParameterTypes.map(genson.provideConverter[AnyRef])
      }
      new TupleNConverter(ctr, converters)
    } else null
  }
}

@HandleClassMetadata
class TupleNConverter(val ctr: Constructor[_], val valuesConverters: Array[Converter[AnyRef]]) extends Converter[Product] {

  assert(ctr.getGenericParameterTypes.length == valuesConverters.length)

  def serialize(value: Product, writer: ObjectWriter, ctx: Context): Unit = {
    var i = 0
    writer.beginArray()
    value.productIterator.foreach {
      elem =>
        valuesConverters(i).serialize(elem.asInstanceOf[AnyRef], writer, ctx)
        i += 1
    }
    writer.endArray()
  }

  def deserialize(reader: ObjectReader, ctx: Context): Product = {
    var i = 0
    val values = Array.ofDim[AnyRef](valuesConverters.length)
    reader.beginArray()
    while (reader.hasNext && i < valuesConverters.length) {
      reader.next()
      values(i) = valuesConverters(i).deserialize(reader, ctx)
      i += 1
    }
    reader.endArray()

    if (i < valuesConverters.length) {
      throw new JsonBindingException("Can't bind to Tuple" + valuesConverters.length + " the json contained only " + i + " values")
    } else if (reader.hasNext && i == valuesConverters.length) {
      throw new JsonBindingException("Can't bind to Tuple" + valuesConverters.length + " the json contains more values")
    }

    ctr.newInstance(values: _*).asInstanceOf[Product]
  }
}
