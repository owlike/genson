package com.owlike

import scala.language.implicitConversions

package object genson {

  lazy val defaultGenson = new ScalaGenson(new GensonBuilder().withBundle(ScalaBundle()).create())

  implicit def toScalaGenson(genson: Genson): ScalaGenson = new ScalaGenson(genson)
}
