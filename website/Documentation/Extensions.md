---
title: Extensions
layout: default
menu: true
jumbotron: true
quick-overview: Genson provides integrations with some common frameworks and is packaged with Bundles to support types from commonly used libraries.
---

##Extension types##

At the moment you can find two kind of extensions in Genson,

 - Integrations of Genson in common frameworks to handle the JSON ser/deser.
 - Integration in Genson of types defined in widely used libraries, ie. support JSR 353 JsonObject, JsonArray, etc types.
 This is done through the GensonBundle system.

##GensonBundle##

A GensonBundle is a way to group a set of features in a single registrable component. Bundles by default, are not registered, you must do it
explicitly. This is used internally to support types and annotations defined in other libraries and is intended to allow users to group their customizations
behind a logical abstraction - the bundle.

Bundles take precedence over user defined customization at the builder level.
This means that if a bundle says you won't serialize null values and you tell the builder the contrary, the bundle will win.
This has been designed this way because we consider that the configuration supplied by a Bundle is required by it, in order to work.

For example you can implement a Bundle providing some common features for your company.

{% highlight java %}
public class MyBundle extends GensonBundle {
  @Override
  public void configure(GensonBuilder builder) {
    // use the builder to configure & register your components
  }
}

// then register your bundle
Genson genson = new GensonBuilder().withBundle(new MyBundle()).create();
{% endhighlight %}

##JAX-RS: Jersey & cie##

To enable json support in JAX-RS implementations with Genson, just drop the jar into your classpath.
The implementation will detect it and use Genson for json conversions.

Actually it has been tested with Jersey and Resteasy. It works out of the box.

###Customization###
In many cases you might want to customize Genson instance.
To do that use GensonBuilder to create a custom instance and then inject it with ContextResolver.

{% highlight java %}
@Provider
public class GensonCustomResolver implements ContextResolver<Genson> {
  // configure the Genson instance
  private final Genson genson = new GensonBuilder().create();

  @Override
  public Genson getContext(Class<?> type) {
      return genson;
  }
}
{% endhighlight %}


**Note**

By default Genson JAX-RS integration enables JAXB annotations support.


##JSR 353 - Java API for Json Processing##

Genson provides two kind of integrations with the JSR. You can use it as the JSR implementation or
to work with the DOM structures defined in the JSR.


If the JSR API is not included in your Java version, you can still get it with Maven.

{% highlight xml %}
<dependency>
  <groupId>javax.json</groupId>
  <artifactId>javax.json-api</artifactId>
  <version>1.0</version>
</dependency>
{% endhighlight %}

###Using Genson as JSR 353 implementation###

Since version 0.99 Genson provides a complete implementation of JSR 353. To use Genson implementation,
you only need it on your classpath.


###Using JSR 353 types with Genson###

Starting with release 0.98 Genson provides a bundle JSR353Bundle that enables support of JSR 353 types in Genson.
This means that you can ser/deser using those types but also mix them with the databinding mechanism.
{% highlight java %}
Genson genson = new GensonBuilder().with(new JSR353Bundle()).create();
JsonObject object = genson.deserialize(json, JsonObject.class);
{% endhighlight %}

You can also mix with standard Pojos.

{% highlight java %}
class Pojo {
  public String aString;
  public JsonArray someArray;
}

Pojo pojo = genson.deserialize(json, Pojo.class);
{% endhighlight %}

##JAXB Annotations##


Since version 0.95 Genson provides support for JAXB annotations.
All annotations are not supported as some do not make sense in the JSON world.
Genson annotations take precedence over JAXB ones, so they can be used to override JAXB annotations.

In Jersey JAXB bundle is enabled by default. If you are using Genson outside Jersey you have to register JAXB bundle:

{% highlight java %}
Genson genson = new GensonBuilder().with(new JAXBBundle()).create();
{% endhighlight %}

###Supported annotations###

 * **XmlAttribute** can be used to include a property in serialization/deserialization (can be used on fields, getter and setter).
 If a name is defined it will be used instead of the one derived from the method/field.

 * **XmlElement** works as **XmlAttribute**, if type is defined it will be used instead of the actual one.

 * **XmlJavaTypeAdapter** can be used to define a custom XmlAdapter. This adapter must have a no arg constructor.

 * **XmlEnumValue** can be used on enum values to map them to different values (it can be mixed with default behaviour:
 you can use this annotation on some enum values and let the others be ser/deser using the default strategy).

 * **XmlAccessorType** can be used to define how to detect properties (fields, methods, public only etc).

 * **XmlTransient** to exclude a field or get/set from ser/deser process.

###Supported types###

Actual implementation has default converters for **Duration** and **XMLGregorianCalendar**.


###What might come next###

Support for cyclic references using XmlId and XmlIdRef, XmlType.
Maybe XmlRootElement, even if I doubt of its pertinence in JSON...

If there are other jaxb features you would like to be supported just open an issue or drop an email on the google group.

##Joda-Time

Genson provides JodaTimeBundle enabling joda-time types support.

{% highlight java %}
Genson genson = new GensonBuilder().withBundle(new JodaTimeBundle()).create();
{% endhighlight %}

By default dates are being ser/de using {% highlight java nowrap %}ISODateTimeFormat.dateTime(){% endhighlight %} formatter.
You can change it and use another formatter. However take care, that this formatter will be used for serialization and deserialization,
thus it must support the methods **print** and **parse**.

{% highlight java %}
DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd");
Genson genson = new GensonBuilder()
  .withBundle(new JodaTimeBundle().useDateFormatter(formatter))
  .create();
{% endhighlight %}


Or ser/de dates as timestamps, in that case you configure it at the builder level, like for classic Java Dates.

{% highlight java %}
Genson genson = new GensonBuilder()
  .useDateAsTimestamp(true)
  .withBundle(new JodaTimeBundle())
  .create();
{% endhighlight %}

###Supported types###

Main Joda Time types are supported at the moment, if some are missing feel free to open an issue or even better, make a pull request :)

* Implementations of ReadableInstant such as DateTime, Instant and MutableDateTime.
* Interval as an object containing two keys start and end, using the configured DateTimeFormatter or timestamps.
* Period is ser/de using {% highlight java nowrap %}ISOPeriodFormat.standard(){% endhighlight %}.
* Duration is ser/de as a long representing the duration in milliseconds.


##Guava

At the moment the Guava bundle is still under development, it only supports the Optional type. Absent will be serialized as null and null
will be deserialized back to Absent.

{% highlight java %}
Genson genson = new GensonBuilder().withBundle(new GuavaBundle()).create();
{% endhighlight %}



##Spring MVC##

To enable json support in Spring MVC with Genson, you need to register Gensons MessageConverter implementation.

{% highlight xml %}
<mvc:annotation-driven>
  <mvc:message-converters>
    <bean class="com.owlike.genson.ext.spring.GensonMessageConverter"/>
  </mvc:message-converters>
</mvc:annotation-driven>
{% endhighlight %}