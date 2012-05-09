package org.likeit.transformation;

import java.lang.reflect.Type;

public interface Factory<T> {
	public T create(Type type);
}
