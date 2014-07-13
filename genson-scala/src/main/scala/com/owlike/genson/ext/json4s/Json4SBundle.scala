package com.owlike.genson.ext.json4s

import com.owlike.genson.ext.GensonBundle
import com.owlike.genson._
import org.json4s.JsonAST._
import com.owlike.genson.stream.{ValueType, ObjectWriter, ObjectReader}
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JDouble
import org.json4s.JsonAST.JInt
import org.json4s.JsonAST.JDecimal
import org.json4s.JsonAST.JBool
import org.json4s.JsonAST.JArray
import java.lang.reflect.Type
import org.json4s.JsonAST.JDouble
import org.json4s.JsonAST.JBool
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JInt
import org.json4s.JsonAST.JDecimal
import org.json4s.JsonAST.JArray
import scala.collection.mutable.ArrayBuffer

object Json4SBundle extends GensonBundle {

  def configure(builder: GensonBuilder) {
    builder.withConverterFactory(new Factory[Converter[JValue]]() {
      def create(`type`: Type, genson: Genson): Converter[JValue] = JValueConverter
    })
  }
}

object JValueConverter extends Converter[JValue] {

  def serialize(value: JValue, writer: ObjectWriter, ctx: Context) = {
    if (value == null) writer.writeNull()
    else {
      value match {
        case JString(v) => writer.writeString(v)
        case JDouble(v) => writer.writeValue(v)
        case JDecimal(v) => writer.writeValue(v)
        case JInt(v) => writer.writeValue(v)
        case JNull => writer.writeNull()
        case JObject(fields) => {
          writer.beginObject()
          fields.foreach(field => serialize(field._2, writer.writeName(field._1), ctx))
          writer.endObject()
        }
        case JArray(values) => {
          writer.beginArray()
          values.foreach(serialize(_, writer, ctx))
          writer.endArray()
        }
        case JBool(v) => writer.writeBoolean(v)
        case JNothing => {}
      }
    }
  }

  def deserialize(reader: ObjectReader, ctx: Context): JValue = {
    reader.getValueType match {
      case ValueType.STRING => JString(reader.valueAsString())
      case ValueType.DOUBLE => JDouble(reader.valueAsDouble())
      case ValueType.INTEGER => JInt(reader.valueAsLong())
      case ValueType.NULL => JNull
      case ValueType.OBJECT => {
        val buffer = ArrayBuffer[JField]()
        reader.beginObject()
        while(reader.hasNext) {
          reader.next()
          buffer += JField(reader.name(), deserialize(reader, ctx))
        }
        reader.endObject()
        JObject(buffer.toList)
      }
      case ValueType.ARRAY => {
        val buffer = ArrayBuffer[JValue]()
        reader.beginArray()
        while(reader.hasNext) {
          reader.next()
          buffer += deserialize(reader, ctx)
        }
        reader.endArray()
        JArray(buffer.toList)
      }
      case ValueType.BOOLEAN => JBool(reader.valueAsBoolean())
      case _ => JNothing
    }
  }
}