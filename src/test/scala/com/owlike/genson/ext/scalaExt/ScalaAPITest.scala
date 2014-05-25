package com.owlike.genson.ext.scalaExt

import org.scalatest.FunSuite
import com.owlike.genson.GensonBuilder
import org.scalatest.Matchers
import java.net.URL
import java.util.Date
import java.text.SimpleDateFormat

class ScalaAPITest extends FunSuite with Matchers {
  val df = new SimpleDateFormat("yyyy-MM-dd")
  val genson = new GensonBuilder().`with`(ScalaBundle()).useDateFormat(df).create()

  test("type inference with Tuples") {
    val (date, url) = genson.fromJson[(Date, URL)]("[\"2014-12-01\", \"http://www.google.com\"]")

    date shouldEqual df.parse("2014-12-01")
    url.toString shouldEqual "http://www.google.com"
  }

  test("type inference with List") {
    val List(url) = genson.fromJson[List[URL]]("[\"http://www.google.com\"]")

    url.toString shouldEqual "http://www.google.com"
  }

  test("type inference with Map") {
    val map = genson.fromJson[Map[String, Set[URL]]]("{\"k1\": [\"http://www.google.com\"]}")

    map("k1").head shouldEqual new URL("http://www.google.com")
  }

  test("type inference of Map with Int key") {
    val map = genson.fromJson[Map[Int, Int]]("{\"1\": 2}")
    map(1) shouldEqual 2
  }

  test("type inference of tuple of Map with Int key") {
    val (map, intValue) = genson.fromJson[(Map[Int, Int], Int)]("[{\"1\": 2}, 3]")
    map(1) shouldEqual 2
    intValue shouldEqual 3
  }

  test("round trip Map with Optional values") {
    val map = genson.fromJson[Map[Int, Option[String]]]("{\"1\": null, \"2\": \"foo bar\"}")
    map(1) shouldEqual None
    map(2) shouldEqual Some("foo bar")
  }
}
