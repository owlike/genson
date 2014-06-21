package com.owlike.genson

case class BasicPoso(val aString: String, val aInt: Int, val aBoolean: Boolean) {
  val shouldBeIgnored: Int = 10
  var other: String = _

  def getAnotherIgnored(): Int = 15
}

case class PosoWithOption(val optPoso: Option[BasicPoso], val optInt: Option[Int])

case class PosoWithMultipleCtrs(val aString: String, val aInt: Int) {
  def this(aInt2: Int) = this("", aInt2)
}
