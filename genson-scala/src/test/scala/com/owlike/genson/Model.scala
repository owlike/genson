package com.owlike.genson

import scala.beans.BeanProperty
import scala.language.existentials

case class BasicPoso(val aString: String, val aInt: Int, val aBoolean: Boolean) {
  val shouldBeIgnored: Int = 10
  var other: String = _

  def getAnotherIgnored(): Int = 15
}

case class PosoWithOption(val optPoso: Option[BasicPoso], val optInt: Option[Int])

case class PosoWithMultipleCtrs(val aString: String, val aInt: Int) {
  def this(aInt2: Int) = this("", aInt2)
}

case class PosoWithArrayOfPrimitives(val array: Array[Int])

case class PosoWithNestedGenerics(val genValue: GenericHolder[GenericHolder[Int]])

case class GenericHolder[T](val v: T)

case class PosoWithBoundedGenericHolder(val arrayHolder: GenericHolder[_ <: BaseClass])

case class BaseClass(val aInt: Int)

class ClassWithGetterAndFiels(@BeanProperty val aStr: String, @BeanProperty val anArray: Array[Option[Int]]) {
  var aIntWithPrivateName: Option[Int] = None

  def getAInt() = aIntWithPrivateName
  def setAInt(aInt: Option[Int]) = this.aIntWithPrivateName = aInt
}

case class CaseClassWithAny(any: Any, anyRef: AnyRef, anyVal: AnyVal)

case class PosoWithPrimitives(aShort: Short, aFloat: Float, aByte: Byte, aChar: Char)