package com.owlike.genson.ext.scalaExt

import com.owlike.genson.{Context, Genson, Converter, Factory}

import java.lang.reflect.Type
import com.owlike.genson.reflect.TypeUtil._
import com.owlike.genson.convert.DefaultConverters.{MapConverterFactory => JavaMapFactory, KeyAdapter}
import com.owlike.genson.stream.{ObjectReader, ObjectWriter}
import scala.collection.generic.CanBuildFrom

import scala.collection.immutable.{:: => colon, _}

import scala.collection.mutable.{
  Map => MMap,
  ListMap  => MListMap,
  HashMap => MHashMap,
  Set => MSet,
  HashSet => MHashSet,
  ListBuffer,
  Queue => MQueue,
  Buffer
}

import scala.collection.Map
import scala.collection.immutable.{Map => IMap}
import scala.collection.immutable.HashSet.HashTrieSet
import scala.collection.immutable.Set.{Set1, Set2, Set3, Set4}
import scala.Vector
import scala.Seq
import scala.Traversable
import scala.List
import scala.collection.immutable.Map.{Map4, Map3, Map2, Map1}

class MapConverterFactory extends Factory[Converter[_ <: Any]]() {
  val cbfByType = List[(Class[_], CanBuildFrom[_ <: Traversable[_], _, _ <: Traversable[_]])](
    // mappings for immutable collections
    classOf[Map1[_, _]] -> Map.canBuildFrom,
    classOf[Map2[_, _]] -> Map.canBuildFrom,
    classOf[Map3[_, _]] -> Map.canBuildFrom,
    classOf[Map4[_, _]] -> Map.canBuildFrom,

    classOf[MListMap[_, _]] -> MListMap.canBuildFrom,
    classOf[MHashMap[_, _]] -> MHashMap.canBuildFrom,

    classOf[HashMap[_, _]] -> HashMap.canBuildFrom,
    classOf[ListMap[_, _]] -> ListMap.canBuildFrom,

    classOf[MMap[_, _]] -> MMap.canBuildFrom,

    classOf[Map[_, _]] -> Map.canBuildFrom
  )

  def create(genType: Type, genson: Genson): Converter[_ <: Any] = {
    val rawClass = getRawClass(genType)

    cbfByType.filter { case (clazz, cbf) =>
      clazz.isAssignableFrom(rawClass)
    }.headOption.map { case (clazz, cbf) =>
      val castCBF = cbf.asInstanceOf[CanBuildFrom[Map[Any, Any], Any, Map[Any, Any]]]

      val expandedType = expandType(lookupGenericType(classOf[Map[_, _]], getRawClass(genType)), genType)
      val keyType: Type = typeOf(0, expandedType)
      val valueType: Type = typeOf(1, expandedType)
      val elemConverter: Converter[Any] = genson.provideConverter(valueType)
      val keyAdapter = JavaMapFactory.keyAdapter(getRawClass(keyType)).asInstanceOf[KeyAdapter[Any]]
      new MapConverter[Any, Any, Map[Any, Any]](keyAdapter, elemConverter)(castCBF)
    }.getOrElse(null)
  }
}

class TraversableConverterFactory extends Factory[Converter[_ <: Traversable[Any]]]() {
  val cbfByType = List[(Class[_], CanBuildFrom[_ <: Traversable[_], _, _ <: Traversable[_]])](
    // mappings for immutable collections
    classOf[colon[_]] -> List.canBuildFrom,
    classOf[HashTrieSet[_]] -> HashSet.canBuildFrom,
    classOf[HashSet[_]] -> HashSet.canBuildFrom,
    classOf[ListSet[_]] -> ListSet.canBuildFrom,
    classOf[Set1[_]] -> Set.canBuildFrom,
    classOf[Set2[_]] -> Set.canBuildFrom,
    classOf[Set3[_]] -> Set.canBuildFrom,
    classOf[Set4[_]] -> Set.canBuildFrom,
    classOf[Set[_]] -> Set.canBuildFrom,
    classOf[Queue[_]] -> Queue.canBuildFrom,


    classOf[MQueue[_]] -> MQueue.canBuildFrom,
    classOf[ListBuffer[_]] -> ListBuffer.canBuildFrom,
    classOf[Buffer[_]] -> Buffer.canBuildFrom,

    classOf[Vector[_]] -> Vector.canBuildFrom,
    classOf[List[_]] -> List.canBuildFrom,


    classOf[MHashSet[_]] -> MHashSet.canBuildFrom,
    classOf[MSet[_]] -> MSet.canBuildFrom,

    classOf[Seq[_]] -> Seq.canBuildFrom

  )

  def create(genType: Type, genson: Genson): Converter[_ <: Traversable[Any]] = {
    val rawClass = getRawClass(genType)

    cbfByType.filter { case (clazz, cbf) =>
      clazz.isAssignableFrom(rawClass)
    }.headOption.map { case (_, cbf) =>
      val castCBF = cbf.asInstanceOf[CanBuildFrom[Traversable[Any], Any, Traversable[Any]]]
      val elemConverter: Converter[Any] = genson.provideConverter(ScalaBundle.getTraversableType(genType))
      new TraversableConverter[Any, Traversable[Any]](elemConverter)(castCBF)
    }.getOrElse(null)
  }
}

class MapConverter[K, V, C <: Map[K, V]]
(keyAdapter: KeyAdapter[K], elemConverter: Converter[V])(implicit cbf: CanBuildFrom[C, (K, V), C])
  extends Converter[C] {

  def serialize(value: C, writer: ObjectWriter, ctx: Context): Unit = {
    writer.beginObject()
    value.foreach { t =>
      writer.writeName(keyAdapter.adapt(t._1))
      elemConverter.serialize(t._2, writer, ctx)
    }
    writer.endObject()
  }

  def deserialize(reader: ObjectReader, ctx: Context): C = {
    val builder = cbf()
    reader.beginObject()
    while (reader.hasNext) {
      reader.next()
      builder += (keyAdapter.adapt(reader.name()) -> elemConverter.deserialize(reader, ctx))
    }
    reader.endObject()
    builder.result()
  }
}

class TraversableConverter[T, C <: Traversable[T]](elemConverter: Converter[T])(implicit cbf: CanBuildFrom[C, T, C])
  extends Converter[C] {

  def serialize(value: C, writer: ObjectWriter, ctx: Context): Unit = {
    writer.beginArray()
    value.foreach { t =>
      elemConverter.serialize(t, writer, ctx)
    }
    writer.endArray()
  }

  def deserialize(reader: ObjectReader, ctx: Context): C = {
    val builder = cbf()
    reader.beginArray()
    while (reader.hasNext) {
      reader.next()
      builder += elemConverter.deserialize(reader, ctx)
    }
    reader.endArray()
    builder.result()
  }
}