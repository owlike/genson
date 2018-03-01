package com.owlike.genson.ext.javadatetime;

/**
 * The different formats that can be used when serializing/deserializing a {@link java.time.temporal.TemporalAccessor}
 * or {@link java.time.temporal.TemporalAmount}
 */
public enum TimestampFormat {
	/**
	 * Values will be read/written as numbers, using milliseconds wherever applicable
	 */
	MILLIS,

	/**
	 * Values will be read/written as numbers, using nanoseconds wherever applicable
	 */
	NANOS,

	/**
	 * Values will be read/written as arrays whose elements consist of the fields contained within the object
	 */
	ARRAY,

	/**
	 * Values will be read/written as objects whose attributes consist of the fields contained within the object
	 */
	OBJECT;
}