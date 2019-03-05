---
title: Configuration
layout: default
jumbotron: true
quick-overview: Overview of Genson annotations and configuration through GensonBuilder.
---

## Annotations

Some aspects of Genson can be configured via annotations used on your classes. This is nice since you have your configuration that
leaves near the code that uses it.

<div class="table-responsive">
<table class="table table-striped table-bordered">
<tr><th>Annotation Name</th><th>Usage</th></tr>
<tr>
  <td>JsonConverter</td>
  <td>Used on fields, methods and constructor parameters of a Pojo, to enable this specific Converter.
  This is useful when you don't want to use the same Converter for all instances of the same type,
  but only based on some contextual criteria, ie. if it is contained in some class.</td>
</tr>
<tr>
  <td>JsonDateFormat</td>
  <td>Used on fields, methods and constructor parameters of a Pojo, to specify how dates should be formatted.
  Any pattern valid for SimpleDateFormat can be used as value.
  The pattern and asTimeInMillis options are exclusive, you can either ser/de this property as a long or using some pattern.
  If no lang is defined, the default Locale.getDefault will be used.
  </td>
</tr>
<tr>
  <td>JsonIgnore</td>
  <td>Allows to exclude properties from serialization and deserialization. By default it excludes properties from both, but you can
  choose whether you some property to be serialized but not deserialized.
  </td>
</tr>
<tr>
  <td>JsonProperty</td>
  <td>It is the opposite of JsonIgnore. This annotation allows to include properties in ser/de. By default, the property will be included in both,
  but you can include only in serialization or deserialization. This annotation can also be used to change the name of the property.
  </td>
</tr>
<tr>
  <td>JsonCreator</td>
  <td>If you have multiple constructors in a Pojo, Genson might not use  the one you would want.
  To tell Genson to use a specific Constructor or Factory Method, put @JsonCreator annotation on it.
  You can have at most, one item annotated with it per class.</td>
</tr>
<tr>
  <td>WithBeanView</td>
  <td>Used on methods of JAX-RS services or Controller methods in Spring MVC. This will tell Genson
  to enable these BeanViews when serializing the resutling object from the call to the method and vice versa.

  The BeanView feature will change in the future.</td>
</tr>
<tr>
  <td>HandleNull</td>
  <td>By default Converters serialize and deserialize method won't be called when the object is null. This is handled by Genson.
  If you annotate a Converter with @HandleNull, then it will be called for null values too. This is useful when you want to handle null values
  in some specific way.</td>
</tr>
<tr>
  <td>HandleClassMetadata</td>
  <td>When class metadata serialization is enabled, annotating a Converter with HandleClassMetadata, will tell Genson to not
  serialize the metadata for the object it is handling.</td>
</tr>
<tr>
  <td>HandleBeanView</td>
  <td>Used on Converters. Will disable the BeanView mechanism for the type handled by this Converter.</td>
</tr>
</table>
</div>


But in some cases you might not be able to modify the source code or just don't want to, in that case you can't use annotations.


## GensonBuilder

GensonBuilder is the main class allowing to configure and create new instances of Genson.
It tries to be easy to use through a fluent builder API.
Most of those options are documented in the [Javadoc]({{site.baseurl}}/Documentation/Javadoc).
GensonBuilder provides several methods to register components such as Converters, Factories, etc, but it is not documented here.

<div class="table-responsive">
<table class="table table-striped table-bordered">
<tr><th>Method Name</th><th>Default</th><th>Description</th></tr>
<tr>
  <td>setSkipNull</td>
  <td>False</td>
  <td>If true, null values will not be serialized.</td>
</tr>
<tr>
  <td>setHtmlSafe</td>
  <td>False</td>
  <td>\,<,>,&,= characters will be replaced by \u0027, \u003c, \u003e, \u0026, \u003d</td>
</tr>
<tr>
  <td>useClassMetadata</td>
  <td>False</td>
  <td>When true, the class of values is being serialized and then used during deserialization to know the concrete type.
  It is useful when you are working with polymorphic types that you want to deserialize back to the concrete implementation.</td>
</tr>
<tr>
  <td>useDateFormat</td>
  <td>The default date formatting style for the default locale. 
  By default dates are being ser. as a timestamp in milliseconds.</td>
  <td>A DateFormat to be used when converting Dates, Calendars...</td>
</tr>
<tr>
  <td>useDateAsTimestamp</td>
  <td>True</td>
  <td>When enabled, will ser/de dates as long representing the time in milliseconds.</td>
</tr>
<tr>
  <td>useRuntimeType</td>
  <td>False</td>
  <td>Will serialize values based on their real type and not compile type.</td>
</tr>
<tr>
  <td>useConstructorWithArguments</td>
  <td>False</td>
  <td>When true, will enable deserialization to constructor with arguments.</td>
</tr>
<tr>
  <td>useStrictDoubleParse</td>
  <td>False</td>
  <td>By default, it uses Genson optimizations to parse double types.
  There is a small accuracy loss compared to Double.parse() for very large numbers, but it is much faster.
  In most cases this optimization is fine, but if you need the exact same precision as
  Double.parse then you can enable strict double parsing.</td>
</tr>
<tr>
  <td>acceptSingleValueAsList</td>
  <td>False</td>
  <td>Wrap a single value into a list when a list is expected. Useful when dealing with APIs that unwrap 
  arrays containing a single value.</td>
</tr>
<tr>
  <td>useIndentation</td>
  <td>False</td>
  <td>Enable it if you want to output nicely formatted Json.</td>
</tr>
<tr>
  <td>useByteAsInt</td>
  <td>False</td>
  <td>By default byte arrays are ser/de as base64 encoded strings.
   When this feature is enabled, each byte will be ser/de as its numeric value.</td>
</tr>
<tr>
  <td>useClassMetadataWithStaticType</td>
  <td>True</td>
  <td>If set to false, during serialization class metadata will be serialized only for types where
  the runtime type differs from the static one.</td>
</tr>
<tr>
  <td>failOnMissingProperty</td>
  <td>False</td>
  <td>If set to true, Genson will throw a JsonBindingException when it encounters a property in the incoming json that does not match
   a property in the class. By default it skips this value silently.</td>
</tr>
<tr>
  <td>wrapRootValues(inputKey, outputKey)</td>
  <td>Disabled</td>
  <td>Allows to wrap all the root objects in another object using outputKey during serialization and unwrap during 
  deserialization the value under inputKey. 
  Use this only if you need to communicate with some 3rd party library that needs this kind of json.
  A more flexible strategy to wrap root values is to use @XmlRootElement with JaxbBundle, see Extensions section.</td>
</tr>
</table>
</div>