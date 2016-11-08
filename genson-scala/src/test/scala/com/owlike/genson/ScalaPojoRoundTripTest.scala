package com.owlike.genson

import org.scalatest.{FunSuite, Matchers}
import defaultGenson._

class ScalaPojoRoundTripTest extends FunSuite with Matchers {

  test("poso with scala primitives should be same after round trip") {
    val expected = PosoWithPrimitives(1, 2.3f, Byte.MinValue, 'o')
    fromJson[PosoWithPrimitives](toJson(expected)) should be (expected)
  }

  test("should serialize correctly case class containing a property of type Any") {
    val expected = "{\"any\":1,\"anyRef\":{\"aInt\":2},\"anyVal\":3}"
    toJson(CaseClassWithAny(1, BaseClass(2), 3)) should be (expected)
  }

  test("deser of tuple with Optional primitive") {
    val expected = (1, Option(2), "foo")
    fromJson[(Int, Option[Int], String)](toJson(expected)) should be (expected)
  }

  test("round trip of ClassWithGetterAndFiels should work with generics of primitive using get/set") {
    val expected = new ClassWithGetterAndFiels("foo", Array(Option(1), None, Option(2)))
    expected.setAInt(Option(2))

    val actual = fromJson[ClassWithGetterAndFiels](toJson(expected))
    actual.aStr should be (expected.aStr)
    actual.anArray.toSeq should be (expected.anArray.toSeq)
    actual.getAInt() should be (expected.getAInt())
  }

  test("deser poso using upper type bound") {
    val json = toJson(PosoWithBoundedGenericHolder(GenericHolder(BaseClass(1))))
    fromJson[PosoWithBoundedGenericHolder](json).arrayHolder.v.aInt == 1 should be (true)
  }

  test("deser poso with nested generics") {
    val expected = PosoWithNestedGenerics(GenericHolder(GenericHolder(1)))
    expected.genValue.v.v.getClass should be (classOf[Int])
    fromJson[PosoWithNestedGenerics](toJson(expected)) should be (expected)
  }

  test("deser poso with array of primitive int") {
    val expected = PosoWithArrayOfPrimitives(Array(1, 2, 3))
    fromJson[PosoWithArrayOfPrimitives](toJson(expected)).array.toSeq should be (expected.array.toSeq)
  }

  test("deser generic primitive scala type") {
    val expected = PosoWithOption(Some(BasicPoso("foo", 3, true)), Some(2))

    val v = fromJson[PosoWithOption](toJson(expected)).optInt.get
    v should be (2)
  }

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

  test("Exclude property from json when excluded using GensonBuilder") {
    val genson = new GensonBuilder()
      .withBundle(ScalaBundle())
      .exclude("aInt")
      .create()

    genson.toJson(BaseClass(3)) should be ("{}")
  }

  ignore("Rename property from json with GensonBuilder") {
    val genson = new GensonBuilder()
      .withBundle(ScalaBundle())
      .rename("aInt", "renamedInt")
      .create()


    genson.toJson(BaseClass(3)) should be ("""{"renamedInt":3}""")
  }

  test("class with value class in it should be properly serialized and then deserialized back") {
    val ent = MyEntity(MyAmount(1L))
    val json = toJson(ent)
    json shouldEqual """{"amount":1}"""
    fromJson[MyEntity](json) shouldEqual ent
  }
}
