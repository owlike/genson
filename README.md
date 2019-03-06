[![Build Status](https://travis-ci.org/owlike/genson.svg?branch=master)](https://travis-ci.org/owlike/genson)

# Genson

[![Join the chat at https://gitter.im/owlike/genson](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/owlike/genson?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Genson is a complete json <-> java conversion library, providing full databinding, streaming and much more.

Gensons main strengths?

 - Easy to use and just works!
 - Its modular and configurable architecture.
 - Speed and controlled small memory foot print making it scale.

## Online Documentation

Checkout our new website - <http://genson.io>.


The old website at <http://code.google.com/p/genson/>, hosts the documentation and javadoc until release 0.99 inclusive.
But starting with 1.0 everything has been moved to github and the new website.

## Motivation

Why create another Json databinding library for Java?
Many libraries lack either important features or the flexibility to add new ones.
As Genson is being developed with such issues in mind, it is easily customizable with support for user extentions, and it includes many useful features out-of-the-box.


## Features you will like

  - Easy to use, fast, highly configurable, lightweight and all that into a single small jar!
  - Full databinding and streaming support for efficient read/write
  - Support for polymorphic types (able to deserialize to an unknown type)
  - Does not require a default no arg constructor and really passes the values not just null, encouraging immutability. It can even be used with factory methods instead of constructors!
  - Full support for generic types
  - Easy to filter/include properties without requiring the use of annotations or mixins
  - Genson provides a complete implementation of JSR 353
  - Starting with Genson 0.95 JAXB annotations and types are supported!
  - Automatic support for JSON in JAX-RS implementations
  - Serialization and Deserialization of maps with complex keys

## Goals

 - Be as much extensible as possible by allowing users to add new features in a clean and easy way. Genson applies the philosophy that *"We can not think of every use case, so give to users the ability to do it by them self in a easy way*".
 - Provide an easy to use API.
 - Try to be as fast and scalable or even faster than the most performant librairies.
 - Full support of Java generics.
 - Provide the ability to work with classes of which you don't have the source code.
 - Provide an efficient streaming API.

## Download

Genson is provided as an all-in-one solution but also natively supports a few extensions and integrations with other libraries such as JAX-RS implementations, Spring, Joda time, and Scala.
These libraries are not included in Genson and won't be pulled transitively if you are using maven 
(they are marked as optional).

You can download Genson manually from [maven central](http://repo1.maven.org/maven2/com/owlike/genson/)
or add the dependency to your pom if you use Maven.

```xml
<dependency>
  <groupId>com.owlike</groupId>
  <artifactId>genson</artifactId>
  <version>{{latest_version}}</version>
</dependency>
```

You can also build it from the sources using Maven.

## POJO databinding

The main entry point in Genson library is the Genson class.
It provides methods to serialize Java objects to JSON  and deserialize JSON streams to Java objects.
Instances of Genson are immutable and thread safe, you should reuse them. In general the recommended way is to have a single instance
per configuration type.

The common way to use Genson is to read JSON and map it to some POJO and vice versa, read the POJO and write JSON.

```java
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
```


## Java collections

But you can also work with standard Java collections such as Map and Lists. If you don't tell Genson what type to use,
it will deserialize JSON Arrays to Java List and JSON Objects to Map, numbers to Long and Double.

Using Java standard types instead of POJO can be a easy way to start learning JSON. In that case you will deal only with
List, Map, Long, Double, String, Boolean and null.

```java
// will be deserialized to a list of maps
List<Object> persons = genson.deserialize("[{\"age\":28,\"name\":\"Foo\"}]", List.class);
// will produce same result as
Object persons = genson.deserialize("[{\"age\":28,\"name\":\"Foo\"}]", Object.class);
```


Instead of using the previous Person class we can use a Map. By default if you don't specify the type of the keys,
Genson will deserialize to String and serialize using toString method of the key.

```java
Map<String, Object> person = new HashMap<String, Object>() {{
  put("name", "Foo");
  put("age", 28);
}};

// {"age":28,"name":"Foo"}
String singlePersonJson = genson.serialize(person);
// will contain a long for the age and a String for the name
Map<String, Object> map = genson.deserialize(singlePersonJson, Map.class);
```

## Deserialize generic types

You can also deserialize to generic types such as a list of Pojos.

```java
String json = "[{\"age\":28,\"name\":\"Foo\"}]";

List<Person> persons = genson.deserialize(json, new GenericType<List<Person>>(){});

// or lets say we want to use something else than String as the keys of our Map.
Map<Integer, Object> map = genson.deserialize(
  "{\"1\":28, \"2\":\"Foo\"}",
  new GenericType<Map<Integer, Object>>(){}
);
```

Note in the previous example we defined the keys (1, 2) as json strings. JSON specification
allows only strings as object property names, but Genson allows to map this keys to some - limited - other types.

## Customizing Genson

If the default configuration of Genson does not fit your needs you can customize it via the GensonBuilder.
For example to enable indentation of the output, serialize all objects using their runtime type and
deserialize to classes that don't provide a default no argument constructor can be achieved with following configuration.

```java
Genson genson = new GensonBuilder()
  .useIndentation(true)
  .useRuntimeType(true)
  .useConstructorWithArguments(true)
  .create();
```


You are ready to rock the JSON! :)


## Copyright and license

Copyright 2011-2014 Genson - Cepoi Eugen

Licensed under the **[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)** (the "License");
you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
