package com.owlike.genson

import org.scalatest.{Matchers, FunSuite}
import defaultGenson._

class ScalaPojoRoundTripTest extends FunSuite with Matchers {

  test("case class with primitive val attributes") {
    val expected = BasicPoso("foo", 3, true)
    expected.other = "bar"

    toJson(expected) shouldEqual "{\"aBoolean\":true,\"aInt\":3,\"aString\":\"foo\",\"other\":\"bar\"}"
    fromJson[BasicPoso](toJson(expected)) shouldEqual expected
  }

  test("case class with Option present") {
    val expected = PosoWithOption(Some(BasicPoso("foo", 3, true)), Some(2))

    fromJson[PosoWithOption](toJson(expected)) shouldEqual expected
  }

  test("case class with Option absent") {
    val expected = PosoWithOption(None, None)

    fromJson[PosoWithOption](toJson(expected)) shouldEqual expected
  }

  test("Seq containing optional case classes") {
    val expected = Seq(None, Some((BasicPoso("foo", 3, true))), None)

    toJson(expected) shouldEqual "[null,{\"aBoolean\":true,\"aInt\":3,\"aString\":\"foo\",\"other\":null},null]"
    fromJson[Seq[Option[BasicPoso]]](toJson(expected)) shouldEqual expected
  }

  test("Case class with multiple constructors should use default constructor if none annotated with @JsonCreator") {
    val expected = new PosoWithMultipleCtrs("foo bar", 2)
    val json = toJson(expected)
    json should be ("""{"aInt":2,"aString":"foo bar"}""")

    val actual = fromJson[PosoWithMultipleCtrs](json)
    actual should be (expected)
  }

  test("Ser/de root array") {
    val expected = Array(1, 2, 3)
    fromJson[Array[Int]](toJson(expected)) should be (expected)
  }

  test("Ser/de root complex array") {
    val expected = Array(Array(Seq(BasicPoso("foo", 2, true))))
    fromJson[Array[Array[Seq[BasicPoso]]]](toJson(expected)) should be (expected)
  }
}
