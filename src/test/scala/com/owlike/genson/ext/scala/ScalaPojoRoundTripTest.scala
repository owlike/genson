package com.owlike.genson.ext.scala

import com.owlike.genson.ext.scala.defaultGenson._
import org.scalatest.{Matchers, FunSuite}

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
}
