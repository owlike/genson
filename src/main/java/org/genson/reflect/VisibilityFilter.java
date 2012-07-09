package org.genson.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public final class VisibilityFilter {
	public final static VisibilityFilter DEFAULT = new VisibilityFilter(Modifier.TRANSIENT, Modifier.NATIVE);
	public final static VisibilityFilter ALL = new VisibilityFilter();
	public final static VisibilityFilter PROTECTED = new VisibilityFilter(Modifier.TRANSIENT, Modifier.NATIVE, Modifier.PRIVATE);
	public final static VisibilityFilter PACKAGE_PUBLIC = new VisibilityFilter(Modifier.TRANSIENT, Modifier.NATIVE, Modifier.PRIVATE, Modifier.PRIVATE);
	
	private final static int JAVA_MODIFIERS =  Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.ABSTRACT
			| Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE | Modifier.SYNCHRONIZED | Modifier.NATIVE
			| Modifier.STRICT | Modifier.INTERFACE;
	
	private int filter;
	
	public VisibilityFilter(int...modifier) {
		filter = 0;
		for (int m : modifier) {

			if ((m & JAVA_MODIFIERS) == 0) throw new IllegalArgumentException("One of the modifiers is not a standard java modifier.");
			filter = filter | m;
		}
	}
	
	public final boolean isVisible(Member member) {
		return (member.getModifiers() & filter) == 0;
	}
}
