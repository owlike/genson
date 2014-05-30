package com.owlike.genson.ext

import _root_.scala.language.implicitConversions

package object scala {
  type Genson = com.owlike.genson.Genson
  type GensonBuilder = com.owlike.genson.GensonBuilder

  lazy val defaultGenson = new ScalaGenson(new GensonBuilder().withBundle(ScalaBundle()).create())

  implicit def toScalaGenson(genson: Genson): ScalaGenson = new ScalaGenson(genson)
}
