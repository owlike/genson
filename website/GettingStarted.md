---
title: Getting Started
layout: default
menu: true
jumbotron: true
quick-overview: An overview of Genson, how to download, configure and use it in your projects.
---

## Download

Genson is provided as a all in one solution containing all the features. It provides also a couple of extensions and
integrations with different other libraries such JAX-RS implementations, Spring, Joda time, Scala available out of the box.
These libraries are of course not included in Genson and if you are using maven won't be pulled transitively
(they are marked as optional).

To get you running you can download it manually from [maven central](http://repo1.maven.org/maven2/com/owlike/genson/)
or add the dependency to your pom if you use Maven.

{% highlight xml %}
<dependency>
  <groupId>com.owlike</groupId>
  <artifactId>genson</artifactId>
  <version>{{site.latest_version}}</version>
</dependency>
{% endhighlight %}

You can also build it from the sources using Maven.

## POJO databinding

The main entry point in Genson library is the Genson class.
It provides methods to serialize Java objects to JSON  and deserialize JSON streams to Java objects.
Instances of Genson are immutable and thread safe, you should reuse them. In general the recommended way is to have a single instance
per configuration type.

The common way to use Genson is to read JSON and map it to some POJO and vice versa, read the POJO and write JSON.

{% highlight java %}
Genson genson = new Genson();

// read from a String, byte array, input stream or reader
Person person = genson.deserialize("{\"age\":28,\"name\":\"Foo\"}", Person.class);

String json = genson.serialize(person);
// or produce a byte array
byte[] jsonBytes = genson.serializeBytes(person);
// or serialize to a output stream or writer
genson.serialize(person, outputStream);

public class Person {
  public String name;
  public int age;
}
{% endhighlight %}


## Java collections

But you can also work with standard Java collections such as Map and Lists. If you don't tell Genson what type to use,
it will deserialize JSON Arrays to Java List and JSON Objects to Map, numbers to Long and Double.

Using Java standard types instead of POJO can be a easy way to start learning JSON. In that case you will deal only with
List, Map, Long, Double, String, Boolean and null.

{% highlight java %}
// will be deserialized to a list of maps
List<Object> persons = genson.deserialize("[{\"age\":28,\"name\":\"Foo\"}]", List.class);
// will produce same result as
Object persons = genson.deserialize("[{\"age\":28,\"name\":\"Foo\"}]", Object.class);
{% endhighlight %}


Instead of using the previous Person class we can use a Map. By default if you don't specify the type of the keys,
Genson will deserialize to String and serialize using toString method of the key.

{% highlight java %}
{% raw %}
Map<String, Object> person = new HashMap<String, Object>() {{
  put("name", "Foo");
  put("age", 28);
}};

// {"age":28,"name":"Foo"}
String singlePersonJson = genson.serialize(person);
// will contain a long for the age and a String for the name
Map<String, Object> map = genson.deserialize(singlePersonJson, Map.class);
{% endraw %}
{% endhighlight %}

## Deserialize generic types

You can also deserialize to generic types such as a list of Pojos.

{% highlight java %}
String json = "[{\"age\":28,\"name\":\"Foo\"}]";

List<Person> persons = genson.deserialize(json, new GenericType<List<Person>>(){});

// or lets say we want to use something else than String as the keys of our Map.
Map<Integer, Object> map = genson.deserialize(
  "{\"1\":28, \"2\":\"Foo\"}",
  new GenericType<Map<Integer, Object>>(){}
);
{% endhighlight %}

Note in the previous example we defined the keys (1, 2) as json strings. JSON specification
allows only strings as object property names, but Genson allows to map this keys to some - limited - other types.

## Customizing Genson

If the default configuration of Genson does not fit your needs you can customize it via the GensonBuilder.
For example to enable indentation of the output, serialize all objects using their runtime type and
deserialize to classes that don't provide a default no argument constructor can be achieved with following configuration.

{% highlight java %}
Genson genson = new GensonBuilder()
  .useIndentation(true)
  .useRuntimeType(true)
  .useConstructorWithArguments(true)
  .create();
{% endhighlight %}


You are ready to rock the JSON! :)
