### Genson Java Date Time Extension
This module provides a genson extension to allow support for classes in the Java Date & Time API (JSR 310)

#### Installation
Simply add the JavaDateTimeBundle with the desired options when creating your Genson instance
```java
Genson genson = new GensonBuilder().withBundle(new JavaDateTimeBundle()).create();
```

The format in which the Date & Time types are serialized depends on whether `useDateAsTimestamp` is enabled.  If disabled,
values will be serialized/deserialized using DateTimeFormatter. If enabled, values will be serialized/deserialized using a
timestamp (see below for more details)

#### ZoneId
Some types require a ZoneId for things like calculating the number of epoch seconds or parsing formats that omit the zoneId.
By default, the bundle will use the system default zoneId.  To change this, use `setZoneId()`

#### Configuring DateTimeFormatters
If serializing/deserializing using DateTimeFormatters, the bundle will default to the relevant ISO DateTimeFormatter
for parsing/formatting (e.g ZonedDateTime uses ISO_ZONED_DATE_TIME). This can be changed by using the `setFormatter` 
method when creating the bundle to set a desired DateTimeFormatter for a specific TemporalAccessor type.  
In the example below, the format for LocalDate is changed to be MM/dd/uuuu

```java
Genson genson = new GensonBuilder().withBundle(
  new JavaDateTimeBundle()
  .setFormatter(LocalDate.class, DateTimeFormatter.ofPattern('MM/dd/uuuu'))
).create();
```
**Note**: For Period and Duration types, values are formatted using  their `toString()` and parsed using their `parse()` function


##### Parse Defaulting
Using DateTimeFormatters to parse values is tricky. By default, if the formatter does not specify all the fields needed by the
desired type, Java will throw an exception.  For example, attempting to parse '2018-01-01' to a ZonedDateTime will throw an 
exception because the time portion cannot be determined. To avoid this, the bundle applies some parse defaulting for most of the
common fields and zoneId.  This allows '2018-01-01' to become '2018-01-01 00:00:00' at the bundle configured zoneId. This should
work in most common cases but using formatters that utilize less common TemporalField types may cause problems and it is suggested
you file an issue

#### Configuring Timestamps
The following four timestamp formats are available:
  - MILLIS - a numeric timestamp which uses milliseconds wherever possible
  - NANOS - a numeric timestamp which uses nanoseconds wherever possible
  - ARRAY - an array containing all the relevant TemporalField values
  - OBJECT - an object containing all the relevant TemporalField values

The default formats are shown in the table below. Most types support all four formats.  The default format can be configured per
type by using `setTemporalAccessorTimestampFormat` and `setTemporalAmountTimestampFormat` (the latter to be used for Period and
Duration types).  In the example below, the format for ZonedDateTime is set to use nanoseconds:

```java
Genson genson = new GensonBuilder().withBundle(
  new JavaDateTimeBundle()
  .setTemporalAmountTimestampFormat(ZonedDateTime.class, TimestampFormat.NANOS)
).create();
```

##### Timestamp Formats Per Type
Note that ARRAY and OBJECT format tend to use the same values. In the table below, the values are shown in array format.
The labels used match the property names used if serialized in json format. For example, LocalDate is serialized in 
array as `[year, month, day]` and in object format as `{"year": year, "month": month, "day": day }`

|  Type          |  Default |   MILLIS            |      NANOS          |               ARRAY/OBJECT                                            |  Notes | 
|----------------|----------|---------------------|---------------------|-----------------------------------------------------------------------|--------|
| Instant        |  MILLIS  | Epoch milliseconds  |  Epoch Nano seconds | [second, nano]                                        | Second resolves to INSTANT_SECONDS which is number of epoch seconds |
| ZonedDateTime  |  MILLIS  | Epoch milliseconds  |  Epoch Nano seconds | [year, month, day, hour, minute, second, nano, zoneId]        | |
| OffsetDateTime |  MILLIS  | Epoch milliseconds  |  Epoch Nano seconds | [year, month, day, hour, minute, second, nano, offsetSeconds]  | |
| LocaDateTime   |  MILLIS  | Epoch milliseconds  |  Epoch Nano seconds | [year, month, day, hour, minute, second, nano]                |  Milliseconds and nanos are obtained by converting LocalDateTime to ZonedDateTime using configured ZoneId |
| LocalDate      |  MILLIS  | Epoch day           |  Epoch Day          | [year, month, day]                                                    | Millis and nanos both resolve to epoch day |
| LocalTime      |  MILLIS  | Milliseconds of day | Nanoseconds of day  | [hour, minute, second, nano]                                                    |  |
| Year           |  ARRAY   | Year                |  Year               | [year]                                                    |  |
| YearMonth      |  ARRAY   | Epoch month         |  Epoch month        | [year, month]                                                    |  Epoch month is number of months from January 1970 |
| MonthDay       |  ARRAY   | Not Supported       |  Not supported      | [month, day]                                               | Numeric timestamp not supported. Cannot use day of year due to possible leap year |
| OffsetTime     |  ARRAY   | Not supported       |  Not supported      | [hour, minute, second, nano, offsetSeconds]                                                    |  Numeric timestamp not supported |
| Period         |  ARRAY   | Not supported       |  Not supported      | [years, months, days]                                                    |  Numeric timestamp not supported |
| Duration       |  ARRAY   | Not supported       |  Not supported      | [seconds, nanos]                                                    |  Numeric timestamp not supported |

#### Annotations
##### @JsonDateFormat
The bundle is configured to work with the existing `@JsonDateFormat` annotation. The value of the annotation will be used as
the pattern to construct a DateTimeFormatter. 

The `asTimeInMillis` property is supported but does not function exactly the same.  The value only 
determines whether the bundle will serialize/deserialize in the assigned TimestampFormat. It does not guarantee that the timestamp
format is actually milliseconds.  Use `@JsonTimestampFormat` to control the desired format

##### @JsonTimestampFormat
Specify the timestamp format to use when serializing/deserializing the property. Only applicable if `Genson#isDateAsTimestamp`
is set to true or if @JsonDateFormat on the property has `asTimeInMillis` set to true

##### @JsonZoneId
Specify the ZoneId to use when serializing/deserializing the property (if applicable to the type).  Can be used to override
the default ZoneId configured in the module
