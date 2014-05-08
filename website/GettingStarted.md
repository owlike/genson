---
title: Getting Started
layout: default
menu: true
jumbotron: true
quick-overview: The getting started guide gives an overview of Genson and some base features that can be enable via configuration. After reading it you should be able to do address most of your use cases.
---


##Overview

The main entry point in Genson library is the Genson class.
It provides methods to serialize Java objects to JSON  and deserialize JSON streams to Java objects.
Instances of Genson are immutable and thread safe, you should reuse them. In general the recommended way is to have a single instance
per configuration type. Genson provides a default constructor with no arguments that uses the default configuration,
to customize Genson have a look at the GensonBuilder.


##Read/Write Json

Genson by default supports main Java classes such as primitives, Lists, Maps, Dates, etc. by providing Converters for them,
if there is no such converter Genson will consider it is a Java Bean and will try to ser/de based on its Getters,
Setters, Fields and Constructors.

Once you have a Genson instance, you can use it to serialize/deserialize JSON strings.

{% highlight java %}
int[] arrayOfInts = new int[]{1, 2, 3};
// json = [1,2,3]
String json = genson.serialize(arrayOfInts);
genson.deserialize(json, int[].class);
{% endhighlight %}

###Supported IO classes
You can also use Java OutputStream/InputStream, Writer/Reader and byte arrays instead of Strings.
In most cases you will want to use them instead of Strings for better performances.

{% highlight java %}
byte[] byteArray = genson.serializeBytes(arrayOfInts);
genson.serialize(arrayOfInts, outputStream);
genson.serialize(arrayOfInts, writer);

genson.deserialize(byteArray, int[].class);
genson.deserialize(inputStream, int[].class);
genson.deserialize(reader, int[].class);
{% endhighlight %}


###Untyped Java structures
During serialization if there is no type information available (meaning it is Object),
Genson will use the runtime type of the object to decide how to serialize it.
If it has type information then it will use it instead of the runtime type.


In the example below, Genson will use the runtime type of the values in the map.

{% highlight java %}
{% raw %}
Map<String, Object> person = new HashMap<String, Object>() {{
    put("name", "Foo");
    put("age", 28);
}};

// {"age":28,"name":"Foo"}
String json = genson.serialize(person);
{% endraw %}
{% endhighlight %}


Deserialization however is more tricky as the type must be detected based on the content of the JSON stream.
By default, if no type information is available during deserialization, Genson will provide a base mapping between JSON types and Java types.
For example, JSON object will be mapped to a Map with Strings as keys, JSON arrays to Java arrays, etc.
There is another mechanism allowing to [deserialize to polymorphic or unknown types](#polymorphic-types), but we will discuss it latter.


Here in both cases it will be deserialized to a map with the correct types (string and int) as they are standard Json types.
{% highlight java %}
Map<String, Object> map = genson.deserialize(json, Map.class);
Object map2 = genson.deserialize(json, Object.class);
{% endhighlight %}


**Note**: Sometime you might want to always use the runtime type, this can be enabled with
{% highlight java  %}new GensonBuilder().useRuntimeType(true).create(){% endhighlight %}


##POJO Databinding
Of course working only with Maps, Lists and primitive types is not so convenient.
Usually your goal is to map the JSON to some structure you have defined and vice versa.

Genson provides full support for object databinding by following standard JavaBean conventions. The basic rules for databinding are:

 * All public or package visibility fields, getters and setters will be used, including the inherited ones.
 * If a getter/setter exists for a field then it will be used instead of the field.
 * Transient and static fields are not serialized nor used during deserialization.
 * A default no argument constructor is required (can be configured to use constructors with arguments and support immutable types)
 * If a field does not exist in the json stream then its field/setter will not be used.
 * If a value is null then you can choose whether you want it to be serialized as null or just skipped, by default null values are serialized.

Lets have a closer look to an example.

{% highlight java %}
Person someone = new Person("Eugen", 28, new Address(157, "Paris"));

// {"address":{"building":157,"city":"Paris"},"age":28,"name":"Eugen"}
String json = genson.serialize(someone);

// deserialize it back
Person person = genson.deserialize(json, Person.class);

public class Person {
    private String name;
    private int age;
    private Address address;

    public Person() {}

    public Person(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    // getters & setters
}

public class Address {
    public int building;
    public String city;

    public Address() {}

    public Address(int building, String city) {
        this.building = building;
        this.city = city;
    }
}
{% endhighlight %}



**Remark** Actually inner and anonymous classes serialization is supported but not deserialization, except if it is a static inner class.


##Filter/Rename properties##

Genson provides two major ways of including/excluding and naming properties, via annotations and via configuration through GensonBuilder (not exclusive).

###Via annotations###

Excluding a property can be achieved by putting a {% highlight java nowrap %}@JsonIgnore{% endhighlight %} annotation on the fields or getter/setter. The same goes for
including a property (for example a private field that you want to be serialized) but using @JsonProperty annotation.
JsonProperty can also be used to define a different name for a property than the default one {% highlight java nowrap %}@JsonProperty(name="newName"){% endhighlight %}.

###Via GensonBuilder###

The other way is using the GensonBuilder exclude/include/rename methods that support a wider range of exclusion/inclusion/naming patterns.
This feature gets handy if you can't modify the source code of your classes or just don't want to pollute your code with annotations.

For example suppose you have an attribute named password in some of your classes and want to always exclude it
or that you want to always exclude properties of some type, etc.
The code below demonstrates some of those strategies (but more are available - see the API).

{% highlight java %}
new GensonBuilder()
    .exclude("password")
    .exclude(Class.class)
    .include("name", FromSome.class)
    .rename("type", "_type")
.create();
{% endhighlight %}

###Genson wide strategies###
You can also define some strategies for property resolution that will be used for a Genson instance. You can choose whether
to use Getter&Setter or Fields (or both - by default) and their visibility (public, protected, private...).

{% highlight java %}
new GensonBuilder()
    .useFields(true, VisibilityFilter.PRIVATE)
    .useMethods(false)
.create();
{% endhighlight %}


If you want to have a more general property resolution or naming strategy, you can respectively,
extend PropertyBaseResolver or implement your own PropertyNameResolver and register it.


##Object instantiation##
By default Genson requires you to provide a default no argument constructor. But Genson also works with constructors that take arguments,
encouraging people to work with immutable objects.

- *Why by default doesn't it support constructors with arguments?* <br/>
Java reflection API does not provide access to parameter names of methods and constructors, Genson as most libs that deal with databinding uses it.


To bypass this problem Genson provides two solutions, annotations and automatic detection via byte code analysis.

###Annotations###

Annotate each parameter of the constructor with **@JsonProperty**, this would tell to Genson that this is the name that must be used.

{% highlight java %}
public class Address {
    final int building;
    final String city;

    // we could also put @Creator annotation, if we had multiple
    // constructors and wanted Genson to use this one.
    public Address(@JsonProperty("building") int building, @JsonProperty("city") String city) {
        this.building = building;
        this.city = city;
    }
}
{% endhighlight %}


###Automatic resolution###

You can also use constructors with arguments without using annotations.
This mechanism is disabled by default as it uses byte code parsing to extract names from code that has been compiled with debug symbols
(the default for most build systems and libs). This option does not work on Android.

Enable automatic name resolution for constructors and methods via the GensonBuilder.

{% highlight java %}
new GensonBuilder().useConstructorWithArguments(true).create();
{% endhighlight %}

###Factory methods###
Genson has also support for methods that act as factories. Those methods must be static, defined inside the class you want to
deserialize (or a parent) and annotated with @Creator annotation. Creator annotation can also be used on Constructors when you have multiple ones
and want to tell Genson which one to use.


In the previous example we could have a factory method that builds an Address with some default values,
that would then be overridden by Genson based on the JSON content.

{% highlight java %}
public class Address {
    final int building;
    final String city;

    @Creator public static Address default() {
        return new Address(0, "UNKNOWN");
    }

    public Address(int building, String city) {
        this.building = building;
        this.city = city;
    }
}
{% endhighlight %}



##Exceptions##

Since version 0.99 there are no checked exceptions anymore.
The main reason is to avoid ugly boilerplate code for people who use Genson. Of course even if Genson makes use of unchecked exceptions you
can still catch them if you want to handle them. Genson can throw two kind of exceptions:

 * JsonStreamException, thrown by the streaming API when parsing for example invalid JSON.
 * JsonBindingException, everywhere else, especially if something goes wrong at the databinding level, ex: can not instantiate some type.



##Generic types##

Due to type erasure, in Java, we can not do things like List<Address>.class. Meaning that we can't tell to Genson to deserialize to a List of Addresses but only to a list of unknown objects.
Those unknown objects being deserialized as Maps. The solution is to use what is called TypeToken in [this blog](http://gafter.blogspot.fr/2006/12/super-type-tokens.html).
Gensons implementation of TypeToken is named GenericType. Why not TypeToken?
Because this name is used in Guava library so choosing another name allows to avoid confusion and mistakes.

{% highlight java %}
List<Integer> listOfInt = genson.deserialize("[1, 2]", new GenericType<List<Integer>>(){});
{% endhighlight %}


Genson has full support of Javas generics, allowing you to hava quite complex structures and still be able to ser/de it.

{% highlight java %}
// enable constructor parameter names resolution through byte code analysis
Genson genson = new GensonBuilder().useConstructorWithArguments(true).create();

AddressGroup addressGroup = new AddressGroup(new Container<EuropeanAddress>(
            Arrays.asList(new EuropeanAddress("Champs Elysees", 1, "Paris")))
        )

// {"addressGroup":{"values":[{"building":1,"city":"Paris","street":"Champs Elysees"}]}}
String json = genson.serialize(addressGroup);

public class EuropeanAddress extends Address {
    public final String street;
    public EuropeanAddress(String street, int building, String city) {
        super(buildingn city);
        this.street = street;
    }
}

public class AddressGroup {
    public final Container<EuropeanAddress> addressGroup;
    public AddressGroup(Container<EuropeanAddress> addressGroup) {
        this.addressGroup = addressGroup;
    }
}

public class Container<E extends Address> {
    public final List<E> values;
    public Container(List<E> values) {
        this.values = values;
    }
}
{% endhighlight %}


**Remark** that if in AddressGroup we were using Address type instead of the concrete type, the serialized json wouldn't contain the street property.
Indeed it would have been ser/de using Address type, using the runtime type can be enable via {% highlight java nowrap %}builder.useRuntimeType(true){% endhighlight %}

##Polymorphic types

Another nice feature of Genson is its ability to deserialize an object serialized with Genson back into its concrete type.
Lets now change a bit the previous example. AddressGroup will use Address type instead of the concrete type, the serialized json would not contain the street property anymore.
Indeed it would serialize the content of Container using Address type and not EuropeanAddress, so lets enable runtime type usage.

{% highlight java %}
// lets also enable runtime type usage
Genson genson = new GensonBuilder().useRuntimeType(true).create();

AddressGroup result = genson.deserialize(json, AddressGroup.class);

public class AddressGroup {
    public final Container<? extends Address> addressGroup;
    public AddressGroup(Container<? extends Address> addressGroup) {
        this.addressGroup = addressGroup;
    }
}
{% endhighlight %}

The deserialized result will contain addresses and not european addresses. Why? Because Genson does not have more information about the type except that it is an Address.
There are two kind of solutions to this problem: add information in the class with an annotation for example that would tell what the concrete type is or store in the serialized json
the information about the concrete type. In Genson the second approach is implemented via a metadata mechanism that will serialize class name + package during serialization and use
it when deserializing back.


{% highlight java %}
// lets enable class metadata ser/de, allowing to deserialize back polymorphic types
Genson genson = new GensonBuilder()
        .useClassMetadata(true)
        .useRuntimeType(true)
    .create();

// {"addressGroup":{"values":[{"@class":"my.package.EuropeanAddress","building":1,"city":"Paris","street":"Champs Elysees"}]}}
AddressGroup result = genson.serialize(addressGroup);
{% endhighlight %}


Genson provides an alias mechanism for classes, allowing to use the alias instead of the full class name.
It is a good practice to do so as it gives you the ability to rename your class or package without any impact
on the json (especially useful if you store json in a persistent storage) and it is also safer from a security point of view.

{% highlight java %}
// addAlias creates an alias between a logic name and a full class name, this also enables class metadata mechanism
Genson genson = new GensonBuilder()
        .addAlias("europeanAddress", EuropeanAddress.class)
        .useRuntimeType(true)
    .create();

// {"addressGroup":{"values":[{"@class":"europeanAddress","building":1,"city":"Paris","street":"Champs Elysees"}]}}
AddressGroup result = genson.serialize(addressGroup);
{% endhighlight %}


**Remarks**

 * Class metadata and the overall metadata mechanism is available only for object types and not for arrays or litteral values.
 Metadata must always be the first name/value pairs in json objects.

 * Genson does not serialize, nor use class metadata for standard types (lists, primitives, etc).

 * If you define a custom Converter and do not want Genson to use class metadata for types supported by your Converter
 use @HandleClassMetadata annotation.


##Streaming API

##Custom Converters







