package com.owlike.genson.ext.json4s

import org.scalatest.{Matchers, FunSuite}
import com.owlike.genson.{ScalaBundle, GensonBuilder, ScalaGenson}
import org.json4s._
import org.json4s.JsonDSL._

class Json4SRoundTripTest extends FunSuite with Matchers {
  val genson = new ScalaGenson(
    new GensonBuilder()
      .withBundle(ScalaBundle(), Json4SBundle)
      .create()
  )

  test("json4s types round trip") {
    val json = (
        ("name"  -> "foo") ~
        ("someDouble" -> 28.1) ~
        ("male" -> true) ~
        ("someArray" -> Seq(1, 2, 3)) ~
        ("null" -> JNull)
    )

    genson.toJson(json) should equal ("""{"name":"foo","someDouble":28.1,"male":true,"someArray":[1,2,3],"null":null}""")

    genson.fromJson[JValue](genson.toJson(json)) should equal (json)
  }
}
