package com.owlike.genson

import com.owlike.genson.reflect._
import java.lang.reflect.{Constructor, Method, Type, Field}
import com.owlike.genson.reflect.PropertyAccessor.{MethodAccessor, FieldAccessor}
import com.owlike.genson.reflect.BeanCreator.{MethodBeanCreator, ConstructorBeanCreator}
import com.owlike.genson.reflect.PropertyMutator.{MethodMutator, FieldMutator}
import scala.reflect.runtime.{universe => u}

private object ScalaReflectionApiLock

// its a bit ugly to do all this only to handle erased primitive types in scala, but it works to some degree...
private[genson] class ScalaBeanPropertyFactory(classloader: ClassLoader) extends BeanPropertyFactory {

  def this() = this(classOf[ScalaBundle].getClassLoader)

  val mirror = ScalaReflectionApiLock.synchronized {
    u.runtimeMirror(classloader)
  }

  def createAccessor(name: String, field: Field, ofType: Type, genson: Genson): PropertyAccessor = {
    fieldType(field, ofType)
      .map(new FieldAccessor(name, field, _, field.getDeclaringClass))
      .getOrElse(null)
  }

  def createAccessor(name: String, method: Method, ofType: Type, genson: Genson): PropertyAccessor = {
    ScalaReflectionApiLock.synchronized {
      val sType = expandToJavaType(matchingMethod(method).returnType, ofType)
      sType.map(new MethodAccessor(name, method, _, method.getDeclaringClass))
        .getOrElse(null)
    }
  }

  def createCreator(ofType: Type, ctr: Constructor[_], resolvedNames: Array[String], genson: Genson): BeanCreator = {
    ScalaReflectionApiLock.synchronized {
      val matchingCtrs = mirror.classSymbol(ctr.getDeclaringClass)
        .selfType
        .declaration(u.nme.CONSTRUCTOR)
        .asTerm
        .alternatives
        .filter(p => p.asMethod.paramss.flatten.length == ctr.getParameterTypes.length)

      if (matchingCtrs.length != 1)
        throw new UnsupportedOperationException(
          "Failed to match single constructor, please report this issue."
        )

      val sCtr = matchingCtrs.head.asMethod
      val parameterTypes = sCtr.paramss
        .flatten
        .flatMap(p => expandToJavaType(p.typeSignature, ofType))
        .toArray

      if (parameterTypes.length == ctr.getParameterTypes.length)
        new ConstructorBeanCreator(TypeUtil.getRawClass(ofType), ctr, resolvedNames, parameterTypes)
      else null
    }
  }

  def createCreator(ofType: Type, method: Method, resolvedNames: Array[String], genson: Genson): BeanCreator = {
    ScalaReflectionApiLock.synchronized {
      val parameterTypes = matchingMethod(method).paramss
        .flatten
        .flatMap(p => expandToJavaType(p.typeSignature, ofType))
        .toArray
      if (parameterTypes.length == method.getParameterTypes.length)
        new MethodBeanCreator(method, resolvedNames, parameterTypes, TypeUtil.getRawClass(ofType))
      else null
    }
  }

  def createMutator(name: String, field: Field, ofType: Type, genson: Genson): PropertyMutator = {
    fieldType(field, ofType).map(new FieldMutator(name, field, _, field.getDeclaringClass)).getOrElse(null)
  }

  def createMutator(name: String, method: Method, ofType: Type, genson: Genson): PropertyMutator = {
    ScalaReflectionApiLock.synchronized {
      val sType = expandToJavaType(matchingMethod(method).paramss.flatten.head, ofType)
      sType.map(new MethodMutator(name, method, _, method.getDeclaringClass)).getOrElse(null)
    }
  }

  private def matchingMethod(method: Method): u.MethodSymbol = {
    ScalaReflectionApiLock.synchronized {
      val matchingMethods = mirror.classSymbol(method.getDeclaringClass)
        .selfType
        .declaration(u.newTermName(method.getName))
        .asTerm
        .alternatives
        .filter(p => p.asMethod.paramss.flatten.length == method.getParameterTypes.length)

      if (matchingMethods.length != 1)
        throw new UnsupportedOperationException("Failed to match single accessor, please report this issue.")

      matchingMethods.head.asMethod
    }
  }

  private def fieldType(field: Field, ofType: Type): Option[Type] = {
    ScalaReflectionApiLock.synchronized {
      val term = u.newTermName(field.getName)
      val typeSig = mirror.classSymbol(field.getDeclaringClass).selfType.member(term).typeSignature
      expandToJavaType(typeSig, ofType)
    }
  }

  private def expandToJavaType(scalaType: Any, rootType: Type): Option[Type] = {
    if (scalaType.isInstanceOf[u.NullaryMethodTypeApi]) {
      expandToJavaType(scalaType.asInstanceOf[u.NullaryMethodTypeApi].resultType, rootType)
    } else if (scalaType.isInstanceOf[u.TypeRefApi]) {
      expandTypeRef(scalaType.asInstanceOf[u.TypeRefApi], rootType)
    } else if (scalaType.isInstanceOf[u.ClassSymbolApi]) {
      expandClassSymbol(scalaType.asInstanceOf[u.ClassSymbol], rootType)
    } else {
      None
    }
  }

  private def expandClassSymbol(t: u.ClassSymbol, rootType: Type): Option[Type] = {
    if (t.isAliasType)
      expandToJavaType(t.toType.asInstanceOf[scala.reflect.internal.Types#Type].dealias, rootType)
    else if (t.isAbstractType) {
      // TODO handle bounded types or fallback to type resolution implemented in TypeUtil
      val stv = new ScalaTypeVariable(t.name.decoded, Array(classOf[Object]), TypeUtil.getRawClass(rootType))
      Option(TypeUtil.expandType(stv, rootType))
    } else {
      val name = t.fullName
      if (name != "scala.Any" && name != "scala.AnyRef" && name != "scala.AnyVal") {
        val clazz = mirror.runtimeClass(t)

        if (t.isDerivedValueClass) {
          val fields = clazz.getDeclaredFields
          if (fields.length != 1) {
            println(clazz.getSuperclass)
            clazz.getInterfaces.foreach(println)
            throw new UnsupportedOperationException(
              s"Please report this issue. Encountered Value Class with the following" +
                s"fields ${fields.mkString("[", ", ", "]")} while only one was expected."
            )
          }
          Option(fields.head.getGenericType)
        } else Option(clazz)
      } else Option(classOf[Object])
    }
  }

  private def expandTypeRef(t: u.TypeRefApi, rootType: Type): Option[Type] = {
    val isArray = t.sym.fullName == "scala.Array"

    if (t.args.nonEmpty) {
      val args = t.args.flatMap(p => expandToJavaType(p, rootType)).toArray

      if (args.length != t.args.length) {
        None
      } else {
        val expandedArgs = args.map { expandedType =>
          val rawType = TypeUtil.getRawClass(expandedType)
          if (!isArray && rawType.isPrimitive) TypeUtil.wrap(rawType)
          else expandedType
        }

        if (t.sym.fullName == "scala.Array") {
          if (args.length > 1) {
            // should not happen
            throw new UnsupportedOperationException()
          }

          Option(new ScalaGenericArrayType(args.head))
        } else expandToJavaType(t.sym, rootType).map(new ScalaParameterizedType(None, _, expandedArgs))
      }
    } else expandToJavaType(t.sym, rootType)
  }
}
