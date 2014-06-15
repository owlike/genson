package com.owlike.genson.ext.scala

import org.scalatest.FunSuite
import org.scalatest.Matchers

import scala.collection.immutable.{
HashSet,
ListSet,
ListMap,
HashMap,
Map,
Queue
}

import scala.collection.mutable.{
Map => MMap,
ListMap => MListMap,
HashMap => MHashMap,
Set => MSet,
HashSet => MHashSet,
ListBuffer,
Queue => MQueue,
Buffer
}

import org.scalatest.prop.PropertyChecks
import com.owlike.genson.GenericType

class TraversableSerDeTest extends FunSuite with Matchers with PropertyChecks {
  lazy val genson = new GensonBuilder().withBundle(new ScalaBundle()).create()

  val tupleTests = Table(
    "expected",
    Tuple1(1),
    (1, 2),
    (1, "a"),
    (true, "hey", List(1, 2, 3), Map("key" -> "bar"), 2, 2.3)
  )

  val mapTests = Table(
    ("expected"),
    Map("1" -> "aa"),
    Map("1" -> "aa", "2" -> true),
    Map("1" -> "aa", "2" -> true, "3" -> 3.3),
    Map("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar"),
    Map("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> Map("key" -> "foo bar")),
    HashMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> Map("key" -> "foo bar")),
    ListMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> Map("key" -> "foo bar")),
    MMap("1" -> "a"),
    Map("1" -> "aa", "2" -> true),
    MMap("1" -> "aa", "2" -> true, "3" -> 3.3),
    MMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar"),
    MMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> MMap("key" -> "foo bar")),
    MListMap("1" -> "aa", "2" -> true, "3" -> 3.3),
    MListMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> Map("key" -> "foo bar")),
    MHashMap("1" -> "aa", "2" -> true),
    MHashMap("1" -> "aa", "2" -> true, "3" -> 3.3, "4" -> "bar", "5" -> Map("key" -> "foo bar"))
  )

  val traversableTests = Table(
    "expected",
    Seq(1),
    Seq(1, 2),
    Seq(1, 2, 3),
    List(1, 2, 3, 4),
    HashSet("a", "b", "c"),
    Queue(1, 2, 3),
    Set("ab", "c"),
    Set(1),
    Set(1, 2, 3),
    Set(1, 2, 3, 4),
    Set(1, 2, 3, 4, 5, 6),
    Vector(1, 2, 3),
    ListSet(1, 2),

    MSet("ab", "c"),
    MSet(1),
    MSet(1, 2, 3),
    MSet(1, 2, 3, 4),
    MSet(1, 2, 3, 4, 5, 6),
    MHashSet("a", "b", "c"),
    MHashSet(1, 2, 3, 4, 5, 6),
    ListBuffer(1),
    ListBuffer(1, 2, 3, 4, 5),
    MQueue(1, 2, 3),
    Buffer(1, 2)
  )

  test("tuple round trip") {
    forAll(tupleTests)(checkRoundTrip)
  }

  test("map like traversable round trip") {
    forAll(mapTests)(checkRoundTrip)
  }

  test("iterable like traversable round trip") {
    forAll(traversableTests)(checkRoundTrip)
  }

  def checkRoundTrip(expected: Any) {
    val json = genson.serialize(expected, GenericType.of(expected.getClass))
    val actual = genson.deserialize(json, GenericType.of(expected.getClass))

    actual.getClass shouldEqual expected.getClass
    actual shouldEqual expected
  }
}

