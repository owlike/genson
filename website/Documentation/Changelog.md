---
title: Changelog
layout: default
---

##Release 1.2 - 26 December 2014
 
 **Genson**
 
  * Issue #24 - Automatic discovery of Genson with Jersey v2
  * Issue #26 - Iterate over sequential root json values not enclosed in an array, enabled via usePermissiveParsing
  * Issue #28 - Avoiding NPE with Resteasy when an exception is being thrown
  * Issue #30 - Handle primitive short and float
  * Issue #31 - Handle primitive and boxed single byte and char
  
 **Genson Scala**
 
  * Handling erased primitive types in generics, requires scala 2.11
 

##Release 1.1 - 22 September 2014

"Medium" release providing new extensions, configuration and bug fixes. The API remains backwards compatible.

 **Genson core**

 * Issue #4 Ads deserializeInto method in Genson, allowing to deserialize in an exisintg Pojo
 * Issue #13 Ads failOnMissingProperty option allowing to throw an exception when no property
 * Issue #17 Ads useClassMetadataWithStaticType option allowing to ser or not object type when it is the same the static type.
 in the class matches with the on in the json.
 * Issue #12 Avoiding to pick converters from cache when a contextual converter is available
 * Issue #11 Accessors/Mutators/Creator Properties annotations are merged together

 **ScalaBundle**:

 * Adding support for json4s AST via the new bundle Json4SBundle
 * Fixes a bug in the ScalaBundle when serializing to a byte array
 * Issue #19 Handling correctly root arrays in ScalaBundle
 * Issue #10 ScalaBundle: Ser/de of None without parameter type was failing


##Release 1.0 - 27 June 2014

Major release and migration to GitHub with a complete new website.

 * Fresh new website
 * Scala extension & Bundle
 * Jodatime Bundle
 * Guava Bundle (only Optional for now)
 * Converters for TreeSet, TreeMap, LinkedList, LinkedHashSet, LinkedHashMap, ArrayDeque, PriorityQueue, boxed float and short
 * Moving Genson.Builder to a new class GensonBuilder in its own file. Renaming some configuration methods
 * New writeString(name, value), writeNumber(name, value)... methods in ObjectWriter
 * Deser. to java.util.List instead of array when no type information is available
 * Minor bug fixes


##Release 0.99 - 13 April 2014

This release introduces some new features, the main being JSR 353 and some simplifications of code related to exceptions. Indeed all checked exceptions have been replaced by unchecked ones.

 * Complete implementation of JSR 353
 * Removing TransformationRuntimeException and TransformationException, removing also IOException from signatures. Replaced previous exceptions with the new JsonBindingException. Gensons uses now only unchecked exceptions
 * Guava Bundle, at the moment only Optional is supported
 * Adding ability to ser/deser byte array as array of ints instead of base 64 encoded strings
 * Supporting line & block comments in json
 * Bug fix: #issue 6, fixing memory leak with tomcat (and probably websphere & cie too)
 * Bug fix: serialization of byte array from Pojos, key was not being serialized.
 * Bug fix: using class classloader in ASM resolver instead of Gensons classloader


##Release 0.98 - 7 August 2013

 * JSR 353 types support via a new bundle JSR353Bundle
 * Bugfix in JsonReader skipValue method
 * Bugfix when using BeanViews, introduced after some refactoring in version 0.95

##Release 0.97 - 11 June 2013

 * Bugfix: Jersey messagebodywriter does not close outputstream anymore. Previously they were being closed by inadvertance, streams should be closed by the code that is managing them, not the code that reads/writes.

##Release 0.96 - 2 June 2013

 * Important [http://code.google.com/p/genson/issues/detail?id=4 bug fix] related to generics getting mixed up in internal cache, you are highly encouraged to upgrade to 0.96.

##Release 0.95 - 21 May 2013

 * JAXB annotations and types support, enabled by default for JAX-RS implementations.
 * Added Resteay integration (works as for Jersey, just drop the jar :)).
 * Addition of a bundle system, now users can group a set of custom features or configuration into a bundle and register all at once.
 * Support for serialization and deserialization of byte arrays (encoded by genson to base64).
 * Addition of contextual factory mechanism, allowing to define specific behaviour based on property info (name, annotations, etc).
 * New annotations: @JsonDateFormat and @JsonConverter, can be used on Pojo properties to define a specific date format and converter to use (while still using the default ones for the rest of the properties).
 * Improved map ser/deser to handle all sort of keys. Map with complex keys can now be serialized and deserialized back (however they are not represented as a json object but an array of objects).
 * Bug fix: Generic self referencing types.

##Release 0.94 - 5 February 2013

 * corrected bug related to generics and default generic converter
 * added pretty printing support (enable it via the Genson.Builder)
 * replaced IllegalStateExceptions from JsonReader with JsonStreamException (having location information row/col, package visibility only for this version)
 * added UUID, Calendar, Set converters, ability to use dates/calendars as timeinmillis, unit testing, TypeUtil clean code remove unused method and ParameterizedTypeImpl, we already have ExtendedParameterizedType
 * tests for JsonReader column/row location in Exceptions, added type safe methods to Context and depracted old ones + test
 * improved spring mvc integration
 * ValueType.OBJECT type has been replaced with Map (as unknown object types are deserialized to maps)
 * ValueType.INTEGER type has been replaced with long. INTEGER meaning here a numeric value with no decimals.
 * More unit testing, code enhancements and internal stuff.

##Release 0.93 - 1 October 2012

 * Added new methods to Genson.Builder allowing to filter and rename properties of classes without using annotations and requiring only small code
 * Corrected bugs related to generics
 * Replaced double parsing algorithm with a more accurate one
 * Added the possibility to choose whether to use custom double parsing algorithm (very fast) or standard Double.parse (exact)
 * Metadata bug corrected for attributes defined as Object.

##Release 0.92 - 27 August 2012

 * Jersey integration with automatic detection (and working with DI frameworks)
 * Spring web integration
 * Enhanced filtering support and provided better user customization
 * Default converters for URI, BigInteger, BigDecimal, Timestamp.

##Release 0.91 - 20 August 2012

 * Addition of factory methods in Genson class allowing to ease the creation of ObjectReader/Writer.

##Release 0.9 - 12 August 2012

 * First stable release.