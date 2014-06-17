---
title: Getting Started
layout: default
menu: true
jumbotron: true
quick-overview: An overview of Genson, how to download, configure and use it in your projects.
---

## Installing

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

## Basic usage

The main entry point in Genson library is the Genson class.
It provides methods to serialize Java objects to JSON  and deserialize JSON streams to Java objects.
Instances of Genson are immutable and thread safe, you should reuse them. In general the recommended way is to have a single instance
per configuration type.

{% highlight java %}
Genson genson = new Genson();

String json = genson.serialize(myObject);

MyObject result = genson.deserialize(json, MyObject.class);
{% endhighlight %}

## Customizing Genson

If the default configuration of Genson does not fit your needs you can customize it via the GensonBuilder.
For example to enable indentation of the output and deserialize to classes that don't provide a default no argument constructor.

{% highlight java %}
Genson genson = new GensonBuilder()
  .useIndentation(true)
  .useConstructorWithArguments(true)
  .create();
{% endhighlight %}


You are ready to rock the JSON! :)
