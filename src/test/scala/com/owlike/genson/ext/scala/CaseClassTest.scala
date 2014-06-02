package com.owlike.genson.ext.scala

import org.scalatest.{Matchers, FunSuite}
import com.owlike.genson.GensonBuilder
import java.io.StringWriter
import com.owlike.genson.reflect.VisibilityFilter

case class BasicPoso(val aString: String, val aInt: Int, val aBoolean: Boolean, other: String = "hey")

class CaseClassTest extends FunSuite with Matchers {
    val genson = new GensonBuilder().useFields(true, VisibilityFilter.PRIVATE).useConstructorWithArguments(true).create()

    test("round trip case class with public primitive attributes") {
        val sw = new StringWriter()

        classOf[BasicPoso].getDeclaredFields.foreach(println)
        genson.serialize(BasicPoso("a", 2, true), sw)

        classOf[BasicPoso].getInterfaces.foreach(println)

        classOf[BasicPoso].getDeclaredConstructors.foreach(println)
        val p = genson.getBeanDescriptorFactory.provide(classOf[BasicPoso], genson)

        sw.flush()
        val json = sw.toString

        println(json)

        val poso = genson.deserialize("{\"aBoolean\":true,\"aInt\":2,\"aString\":\"a\"}", classOf[BasicPoso])
        println(poso)
    }
}
