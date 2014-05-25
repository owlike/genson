package com.owlike.genson.ext

import com.owlike.genson.Genson
import scala.language.implicitConversions

package object scalaExt {
  implicit def toScalaGenson(genson: Genson): ScalaGenson = new ScalaGenson(genson)
}
