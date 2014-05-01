---
layout: default
---

 * How to exclude a property from serialization/deserialization?
 Annotate your field and getter/setter with @JsonIgnore.


 * *How to use a field or getter/setter even if he is filtered by VisibilityFilter or if its name does not respect JavaBean conventions?*
 Annotate it with [http://genson.googlecode.com/git/javadoc/com/owlike/genson/annotation/JsonProperty.html @JsonProperty]. You can even indicate if you want it to be included only in serialization or deserialization.

 * *How to skip null values from output?*
{% highlight java %}
new Genson.Builder().setSkipNull(true).create();
{% endhighlight %}

 * *How to create a custom converter/serializer/deserializer?*
 See the custom converter section of the user guide [GettingStarted#Custom_Converter here].

 * *How to store state/pass data from one converter to another?*
  * Use [http://genson.googlecode.com/git/javadoc/com/owlike/genson/Context.html Context] class (prefered solution), it is intended to be shared across all converters for a single execution and already provides some usefull methods to store/retrieve data.
  * Use [http://genson.googlecode.com/git/javadoc/com/owlike/genson/ThreadLocalHolder.html ThreadLocalHolder] which internally uses a ThreadLocal map to store data.

 * *How to change the default visibility of serialized/deserialized fields, methods and constructors?*
 Genson uses [http://genson.googlecode.com/git/javadoc/com/owlike/genson/reflect/VisibilityFilter.html VisibilityFilter] for filtering fields, methods and constructors by their [http://docs.oracle.com/javase/6/docs/api/java/lang/reflect/Modifier.html Modifiers].
 For example if you want to accept all fields :
{% highlight java %}
new Genson.Builder().setFieldVisibility(VisibilityFilter.ALL).create();

// you can also define a custom visibility filter :
new Genson.Builder().setFieldVisibility(new VisibilityFilter(Modifier.TRANSIENT, ...other excluded modifiers...)).create();
{% endhighlight %}

 * *How to use only fields or methods?*
 For example if you want to use only fields :
{% highlight java %}
new Genson.Builder().setUseGettersAndSetters(false).setFieldVisibility(VisibilityFilter.DEFAULT).create();
{% endhighlight %}
 You need to change the visibility filter for fields as by default it will only use fields with public/package visibility.

 * *How to deserialize into a interface/abstract class?*
 See the [GettingStarted#Interface/Abstract_classes_support class metadata mechanism] discussed in the User Guide.

 * *How to deserialize into a class without a no argument constructor?*
 The main problem in using constructors with arguments is to resolve parameter names, as they are not available through introspection. Genson provides a couple of solutions:
  * If your constructor parameter names match the properties names then you can enable it with : [http://genson.googlecode.com/git/javadoc/com/owlike/genson/Genson.Builder.html#setWithDebugInfoPropertyNameResolver(boolean) this feature].
  * Annotate each parameter with @JsonProperty(theName) and specify the name of the matched property.
  * Use a custom Converter/Deserializer or a [GettingStarted#Bean_View Bean View].