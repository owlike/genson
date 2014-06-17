---
title: Scala User Guide
layout: default
menu: true
jumbotron: true
quick-overview: Genson provides an out of the box integration with Scala for the high level databinding API.
---

##Overview

Genson provides an easy to use boiler free integration with Scala.
It supports common Scala mutable and immutable collections, Option, tuples, case classes and even functions
(when [class metadata ser/de is enabled]({{base.url}}/Documentation/UserGuide/#polymorphic-types))!

Standard classes will be ser/de with the classic Genson behaviour for Java.
Genson classic features and annotations are supported (JsonIgnore, JsonProperty, JsonConverter, etc),
bringing the power of Genson to Scala.


To get you started with Genson in Scala you only have to import com.owlike.genson.ext.scala.defaultGenson_
and you are done!

{% highlight scala %}
import com.owlike.genson.ext.scala.defaultGenson_

val jsonString = toJson(someValue)
val actualValue = fromJson[SomeType](json)
{% endhighlight %}

Of course if the default configuration does not fit your needs you can make a custom Genson instance and then use it.
For example lets say we want to enable indentation, serialize objects based on their runtime type and want to ser/de
dates as timestamps and all fields from case classes (not only the ones present in the constructor or the vars).

{% highlight scala %}
import com.owlike.genson.ext.scala._

object CustomGenson {
  val customGenson = new ScalaGenson(
    new GensonBuilder()
      .useIndentation(true)
      .useRuntimeType(true)
      .useTimeInMillis(true)
      .withBundle(ScalaBundle().useOnlyConstructorFields(false))
      .create()
  )
}

// then just import it in the places you want to use this instance instead of the default one
import CustomGenson.customGenson._

// and use it!
toJson(...)
fromJson[SomeType](json)
{% endhighlight %}



##Collections & Tuples

Genson provides a set of defaults converters for most scala collections (mutable and immutable).
Scala provides some optimized types for maps and collections of few elements, Genson will respect it
and deserialize to the optimized type.

{% highlight scala %}
import com.owlike.genson.ext.scala.defaultGenson_

// [1,2,3]
val jsonArray = toJson(Seq(1,2,3))
val seqOfInt = fromJson[Seq](jsonArray)

// {"name":"foo bar"}
val jsonMap = toJson(Map("name" -> "foo bar"))
val mapOfString = fromJson[Map[String,String]](jsonMap)
{% endhighlight %}

Genson will serialize scala Tuples1 to 22, as json arrays.
There is no restriction on the complexity of the tuples (may contain options, case classes, etc).

{% highlight scala %}
// ["foo","bar"]
val json = toJson(("foo", "bar"))
val tuple = fromJson[(String, String)](json)
{% endhighlight %}

##Deserilizing Unknown Types

When you don't specify the type to use, Genson will deserialize:

 * Integer json numbers to Long
 * Double json numbers to Double
 * Json objects to scala immutable Map
 * Json arrays to scala immutable List
 * null to null

{% highlight scala %}
fromJson[Any]("""{"name":"foo bar", "age": 28, "address":{}}""") match {
 case map: Map[String, _] => println(map)
 case list: List[_] => list.foreach(println)
 case _ => throw new IllegalStateException()
}

// or you can wrap the values in options
val mapWithOptionalValues = fromJson[Map[String, Option[_]]](
  """{"name":"foo bar", "age": 28, "address":null}"""
)
{% endhighlight %}


##Case classes

Genson has also out of the box support for case classes. By default Case classes ser/de will follow these rules:

 * Serialize only attributes defined in the main constructor and vars defined in the class,
 ex: if you define a val in the class but not in the constructor, then it won't be serialized.
 * If multiple constructors are defined, only the default one will be used
 (that has a default apply method generated in its companion object)
 * Set/Get methods are not used as it does not make sense with case classes.
 If you want to use java beans then use plain old java classes.

{% highlight scala %}
case class Address(street: String, building: Int)
case class Person(name: String, age: Int, address: Option[Address])

// {"name":"foo bar","age":28,"address":{"street":"rue des lapins",building:1337}}
val json = toJson(Person("foo bar", 28, Some(Address("rue des lapins", 1337)))

// {"name":"foo bar","age":28,"address":null}
val jsonWithNoAddress = toJson(Person("foo bar", 28, None)

val person = fromJson[Person](json)
{% endhighlight %}

Genson will serialize None as null and null is deserialized as None.

