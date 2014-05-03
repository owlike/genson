---
title: Getting Started
layout: default
menu: true
jumbotron: true
quick-overview: The getting started guide gives an overview of Genson and some base features that can be enable via configuration. After reading it you should be able to do address most of your use cases.
---


##Overview

Genson is the main class of the library.
It provides all the required methods to do java to json serialization and json to java deserialization.
If the default configuration fits your needs you can use its no arg constructor otherwise you can use Genson.Builder
 inner class to create custom instances of Genson.

##Read/Write JSON

{% highlight java %}
Genson genson = new Genson();
String json = genson.serialize(777.777); // the output will be 777.777
genson.serialize(true); // output is true (without quotes)

genson.deserialize("777", int.class); // deserializes it into 777
genson.deserialize("777.777", Object.class); // will return 777.777 (a double)
genson.deserialize("null", Object.class); // will return null;
{% endhighlight %}

You can also work with arrays:

{% highlight java %}
json = genson.serialize(new Object[]{1, 2.2, null, false, "hey!"}); // the result is [1,2.2,null,false,"hey!"]

// and now let's do some magic an deserialize it back without knowing the types:
genson.deserialize(json, Object[].class); // and we got back our initial array!
{% endhighlight %}

Genson can serialize generic maps and deserialize back to it, it also supports serialization of objects and deserialization into Maps, but we will see it later.
{% highlight java %}
Map<String, Object> map = new HashMap<String, Object>();
map.put("aString", "hey");
map.put("aInt", 1);
map.put("anArray", new int[]{1, 2});
Map<String, Object> anotherMap = new HashMap<String, Object>();
anotherMap.put("aBool", false);
map.put("theOtherMap", anotherMap);

json = genson.serialize(map); // we obtain {"aString":"hey","aInt":1,"anArray":[1,2],"theOtherMap":{"aBool":false}}
genson.deserialize(json, Map.class); // we got the initial map back
{% endhighlight %}

You can also work with multidimensional arrays without problems. Note that if you want to deserialize to a unknown type the result will be and array of objects, so you won't be able to cast it to an array of int.

{% highlight java %}
{% raw %}
// result is [[[1,2],[5],[4]],[[6]]]
json = genson.serialize(new int[][][]{{{1, 2},{5},{4}},{{6}}});
int[][][] threeDimensionArray = genson.deserialize(json, int[][][].class);

// works also but you will have to cast each value separately as it is an array of objects.
Object object = genson.deserialize(json, Object.class);
{% endraw %}
{% endhighlight %}



##Binding to POJOs
Genson provides full support for object databinding by following standard JavaBean conventions. The basic rules for databinding are:

 * All public or package visibility fields, getters and setters will be used, including the inherited ones.
 * If a getter/setter exists for a field then it will be used instead of the field.
 * Transient and synthetic fields are not serialized nor used during deserialization.
 * If a field does not exist in the json stream then its field/setter will not be used.
 * If a value is null then you can choose whether you want it to be serialized as null or just skipped, by default null values are serialized.

Lets have a closer look to an example.

{% highlight java %}
class Person {
 String fullName;
 // will be converted even if it is private
 @JsonProperty private Date birthDate;
 Adress adress;
 @JsonIgnore public int ignoredField;
 private int privateNotDetected;
 private Person() {}

 @Creator public static Person create() {
   return new Person();
 }

 public String getFullName(){
  // will be used instead of direct field access
 }
}

class Adress {
  final int building;
  final String street;
  // only a constructor with arguments genson will use it during deserialization
  public Adress(@JsonProperty("building") int building, @JsonProperty("street")  String street) {
  }
}

Person someone = new Person("eugen", new GregorianCalendar(1986, 1, 16).getTime(), new Adress(157, "paris"));
// we obtain the following json string
//{"adress":{"building":157,"street":"paris"},"birthDate":"16 févr. 1986","happy":true,"fullName":"eugen"}
String json = genson.serialize(someone);

// now we deserialize it back
someone = genson.deserialize(json, Person.class);
{% endhighlight %}

Note the use of {% highlight java nowrap %}@JsonIgnore{% endhighlight %} and {% highlight java nowrap %}@JsonProperty{% endhighlight %} annotations.
The first one allows to mark a field/method as ignored for serialization/deserialization,
the second one allows you to specify a name for your property and to include fields/methods.
In Genson we choose to give priority to explicit information (ex: annotations) over implicit information (ex: naming convetions, visibility, etc).
Another important thing to notice is that Adress provides a single constructor that takes arguments.
Genson will invoke it with the corresponding values from the json string. You may also use static factory methods as
the "create" method of Person class, to be detected they need to be marked with {% highlight java nowrap %}@Creator{% endhighlight %} annotation and be static.

**Remarks**

 * If there is a no arg constructor Genson will use it instead of others constructors, except if there is a constructor or
 method annotated with {% highlight java nowrap %}@Creator{% endhighlight %}, then this one will be used.

 * In some cases you may have a single creator or a constructor annotated with {% highlight java nowrap %}@Creator{% endhighlight %} but without Genson using it,
 this means that your creator has arguments but there is no way to resolve their names. Annotate each argument with @JsonProperty
  or enable automatic argument name resolution with {% highlight java nowrap %}Genson.Builder.setWithDebugInfoPropertyNameResolver(true){% endhighlight %}.

 * You can have only a Constructor/Method annotated with @Creator per class.

 * Actually inner and anonymous classes serialization is supported but not deserialization, except if it is a static inner class.

 * By default Genson will serialize objects using their compile type and not runtime type
 (List*<*Number*>* content will be serialized as Numbers no matter their runtime type) if you want to use runtime
 type configure your genson instance with {% highlight java nowrap %}setUseRuntimeTypeForSerialization(true){% endhighlight %}.


##Nice Features



##Generics support

###Deserialize to Generic Lists###

You may ask yourself "okay this is cool but how can I work with generic types?"
Indeed it is a good question. Due to type erasure you can not do things like List*<*String*>*.class, the solution
is to use what is called TypeTokens, see [http://gafter.blogspot.fr/2006/12/super-type-tokens.html](this blog) for an explanation. Gensons implementation of TypeToken is [http://genson.googlecode.com/git/javadoc/com/owlike/genson/GenericType.html GenericType]

{% highlight java %}
json = genson.serialize(Arrays.asList(1, 2));
List<Integer> listOfInt = genson.deserialize(json, new GenericType<List<Integer>>(){});
{% endhighlight %}

Genson will respect the type information.
{% highlight java %}
Type type = new GenericType<List<Number>>(){}.getType();
// this will work, Genson will convert 2.2 to a double, but if the parameterized type was String, 2.2
// would not be converted to an int

genson.deserialize("[1, \"2.2\", null]", type);

// but if you want to deserialize a content that can not be converted to the parameterized type it will fail!
// this will throw an exception of the form "org.genson.TransformationRuntimeException:
// Could not convert input value GG of type class java.lang.String to a Number type."

genson.deserialize("[1, \"GG\"]", type);
{% endhighlight %}

###Generic Objects###
Genson has full support of java generics. It supports generic types specialization in subclasses, wildcards, type variables and so long.
{% highlight java %}
 public class P {}

 public class GenericList<E extends P> {
  List<E> list;
 }

 // list elements will be deserialized to type P
 GenericList genericContainer = genson.deserialize("{\"list\":[{}, {}]}", GenericList.class);
{% endhighlight %}


##Polymorphic types

Another nice feature of Genson is its ability to deserialize an object serialized with Genson back into its concrete type.
This feature is disabled by default, to enable it use {% highlight java nowrap %}Genson.Builder.setWithClassMetadata(true){% endhighlight %}.
This will enable serialization of class names and use it during deserialization to detect the concrete type.
You should use Genson alias mechanism to associate an alias with the concerned class.
Instead of serializing the class name the alias will be used.
It is a good practice to do so as it gives you the ability to rename your class or package without any impact
on the json (especially useful if you store json into a database) and it is also safer from a security point of view.


{% highlight java %}
// suppose we have the following interface and that the Person class from previous example implements it.
interface Entity {}

Genson genson = new Genson.Builder().setWithClassMetadata(true).create();
// json value will be {"@class":"mypackage.Person", ... other properties ...}
String json = genson.serialize(new Person());
// the value of @class property will be used to detect that the concrete type is Person
Entity entity = genson.deserialize(json, Entity.class);

// a better way to achieve the same thing would be to use an alias
// no need to use setWithClassMetadata(true) as when you add an alias Genson
// will automatically enable the class metadata mechanism
genson = new Genson.Builder().addAlias("person", Person.class).create();

// output is {"@class":"person", ... other properties ...}
genson.serialize(new Person());
{% endhighlight %}

Metadata will be discussed more in depth in the advanced user guide.

**Remarks**

 * Class metadata and the overall metadata mechanism is available only for object types and not for arrays or litteral values.
 Metadata must always be the first name/value pairs in json objects.

 * Genson does not serialize, nor use class metadata for standard types (lists, primitives, etc).

 * If you define a custom Converter and do not want Genson to use class metadata for types supported by your Converter
 use @HandleClassMetadata annotation.

