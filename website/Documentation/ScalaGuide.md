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

To get you started with Genson in Scala you only have to import com.owlike.genson.defaultGenson_
and you are done!

{% highlight scala %}
import com.owlike.genson.defaultGenson_

val jsonString = toJson(someValue)
val actualValue = fromJson[SomeType](json)
{% endhighlight %}

###Scala version

Genson has been tested against scala 2.11. In theory you can use it with all releases from 2.11 and up.
It also works with scala 2.10, but at the moment we don't provide an artifact for scala 2.10. If you want
to use Genson against 2.10 you will have to build the source code your self. If you are using/want to use Genson with scala 2.10,
please drop an email on the user list so we can find a better way to provide a distribution for different scala versions.


##Download

In order to keep the main Genson jar small we chose to provide the Scala extension as a separate jar.
The other reason is that some tools might not handle well optional dependencies, in result someone that does not use
the Scala extension could end up with the scala language library downloaded.

###SBT

{% highlight scala %}
libraryDependencies += "com.owlike" % "genson-scala" % "{{site.latest_version}}"
{% endhighlight %}

###Maven

{% highlight xml %}
<dependency>
	<groupId>com.owlike</groupId>
	<artifactId>genson-scala</artifactId>
	<version>{{site.latest_version}}</version>
</dependency>
{% endhighlight %}

###Manual Download

If you don't use a dependency management tool then you can still use it, but you will have to download the Scala extension and
the core Genson jars by hand.

 * [Genson](http://repo1.maven.org/maven2/com/owlike/genson/{{site.latest_version}}/genson-{{site.latest_version}}.jar)
 * [Genson-scala](http://repo1.maven.org/maven2/com/owlike/genson-scala/{{site.latest_version}}/genson-scala-{{site.latest_version}}.jar)


##Collections & Tuples

Genson provides a set of default converters for most scala collections (mutable and immutable).
Scala provides some optimized types for maps and collections of few elements, Genson will respect it
and deserialize to the optimized type.

{% highlight scala %}
import com.owlike.genson.defaultGenson_

// [1,2,3]
val jsonArray = toJson(Seq(1,2,3))
val seqOfInt = fromJson[Seq[Int]](jsonArray)

// {"name":"foo bar"}
val jsonMap = toJson(Map("name" -> "foo bar"))
val mapOfString = fromJson[Map[String,String]](jsonMap)
{% endhighlight %}

Genson will serialize scala Tuple1 to 22, as json arrays.
There is no restriction on the complexity of the tuples (may contain options, case classes, etc).

{% highlight scala %}
// ["foo","bar"]
val json = toJson(("foo", "bar"))
val tuple = fromJson[(String, String)](json)
{% endhighlight %}

##Deserialize Unknown Types

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
 If you want to work with java bean conventions then use plain old java classes.

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

##Customizing

Of course if the default configuration does not fit your needs you can make a custom Genson instance and then use it.
For example lets say we want to enable indentation, serialize objects based on their runtime type and want to ser/de
dates as timestamps and all fields from case classes (not only the ones present in the constructor or the vars).

{% highlight scala %}
import com.owlike.genson._

object CustomGenson {
  val customGenson = new ScalaGenson(
    new GensonBuilder()
      .useIndentation(true)
      .useRuntimeType(true)
      .useDateAsTimestamp(true)
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

For a more in depth overview of Genson features and customization have a look at the User Guide and configuration sections.


##AST support via json4s

Instead of creating again another DOM structure like all the existing ones, Genson provides it by supporting json4s.
Json4s defines an AST for JSON and utilities to work with it.


{% highlight scala %}
import com.owlike.genson._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST._

object CustomGenson {
  val genson = new ScalaGenson(
    new GensonBuilder()
      .withBundle(ScalaBundle(), Json4SBundle())
    .create()
  )
}

// then just import it in the places you want to use this instance instead of the default one
import CustomGenson.genson._

// and use it!
val json = fromJson[JObject]("""{"name":"foo","someDouble":28.1,"male":true,"someArray":[1,2,3],"null":null}""")

{% endhighlight %}


In order to use json4s features with Genson, you need to add a dependency to it.

###SBT

{% highlight scala %}
libraryDependencies += "org.json4s" % "json4s-ast_${scala.version}" % "3.2.10"
{% endhighlight %}

###Maven

{% highlight xml %}
<dependency>
	<groupId>org.json4s</groupId>
	<artifactId>json4s-ast_${scala.version}</artifactId>
	<version>${json4s.version}</version>
</dependency>
{% endhighlight %}
