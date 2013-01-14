package com.owlike.genson;

import java.lang.reflect.Array;

public final class Operations {
	public static <T> T[] union(Class<T[]> tClass, T[]... values) {
		int size = 0;
		for (T[] value : values)
			size += value.length;
		T[] arr = tClass.cast(Array.newInstance(tClass.getComponentType(), size));
		for (int i = 0, len = 0; i < values.length; len += values[i].length, i++)
			System.arraycopy(values[i], 0, arr, len, values[i].length);
		return arr;
	}

	public static void checkNotNull(Object... values) {
		for (Object value : values)
			if (value == null) throw new IllegalArgumentException("Null not allowed!");
	}
}
