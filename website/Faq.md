---
title: FAQ
layout: default
jumbotron: true
---

###How to exclude a property from ser/de?
Annotate your field and getter/setter with `@JsonIgnore`.


###How to include a field or method from ser/de?
Annotate it with [`@JsonProperty` or use GensonBuilder]({{base.url}}/GettingStarted/#filterrename-properties).
You can even indicate if you want it to be included only in serialization or deserialization.


###How to skip null values from output?

{% highlight java %}
new GensonBuilder().setSkipNull(true).create();
{% endhighlight %}


###How to create a custom Converter?
See the [custom converter section]({{base.url}}/GettingStarted/#custom-serde) of the user guide.


###How to store state/share data from one converter to another?

  * Use Context class (preferred solution), it is intended to be shared across all converters for a
  single execution and already provides some usefull methods to store/retrieve data.
  * Use ThreadLocalHolder which internally uses a ThreadLocal map to store data.


###How to change the default visibility of fields, methods and constructors?

Genson uses **VisibilityFilter** class to filter fields, methods and constructors by their [Modifiers](http://docs.oracle.com/javase/6/docs/api/java/lang/reflect/Modifier.html).
For example if you want to accept public, protected and private fields :
{% highlight java %}
new GensonBuilder().useFields(true, VisibilityFilter.PRIVATE).useMethods(true).create();

// you can also define a custom visibility filter :
new GensonBuilder().useFields(true, new VisibilityFilter(Modifier.TRANSIENT, Modifier.STATIC)).create();
{% endhighlight %}


###How to use only fields or methods?

For example if you want to use only fields :

{% highlight java %}
new GensonBuilder().useMethods(false).useFields(true, VisibilityFilter.PRIVATE).create();
{% endhighlight %}

You need to change the visibility filter for fields as by default it will only use fields with public/package visibility.


###How to deserialize to an interface/abstract class?
See the [class metadata mechanism]({{base.url}}/GettingStarted/#polymorphic-types) discussed in the Getting Started Guide.


###How to deserialize a class without a no argument constructor?
The main problem in using constructors with arguments is to resolve parameter names, as they are not available through introspection.
[See the solutions described]({{base.url}}/Documentation/UserGuide/#object-instantiation) in the Getting Started guide.

###How to use case insensitive enums?
By default enum names must match exactly. To make the matching case insensitive register a customized instance of EnumConverterFactory.

{% highlight java %}
Genson genson = new GensonBuilder().withConverterFactory(new DefaultConverters.EnumConverterFactory(false)).create();
{% endhighlight %}