package com.owlike.genson.ext.scala

import org.scalatest.{Matchers, FunSuite}

case class BasicPoso(val aString: String, val aInt: Int, val aBoolean: Boolean, other: String = "hey")

class CaseClassTest extends FunSuite with Matchers {

    test("round trip case class with public primitive attributes") {

    }
}
