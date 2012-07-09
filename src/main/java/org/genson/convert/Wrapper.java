package org.genson.convert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.genson.Operations;

public abstract class Wrapper<T> implements AnnotatedElement {
	private AnnotatedElement wrappedElement;
	
	protected Wrapper() {}
	
	protected Wrapper(T wrappedObject) {
		if (wrappedObject == null) throw new IllegalArgumentException("Null not allowed!");
		wrap(wrappedObject);
	}
	
	public Annotation[] getAnnotations() {
		return Operations.union(Annotation[].class, wrappedElement.getAnnotations(), getClass().getAnnotations());
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> aClass) {
		A ann = wrappedElement.getAnnotation(aClass);
		return ann == null ? getClass().getAnnotation(aClass) : ann;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return Operations.union(Annotation[].class, wrappedElement.getDeclaredAnnotations(), getClass().getDeclaredAnnotations());
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return wrappedElement.isAnnotationPresent(annotationClass) || getClass().isAnnotationPresent(annotationClass);
	}
	
	// package visibility as a convenience for CircularClassReferenceConverter
	void wrap(T object) {
		if (wrappedElement != null) throw new IllegalStateException("An object is already wrapped!");
		if (object instanceof AnnotatedElement)
			this.wrappedElement = (AnnotatedElement) object;
		else this.wrappedElement = object.getClass();
	}
	
	public static AnnotatedElement toAnnotatedElement(Object object) {
		if (object  == null) return null;
		if (isWrapped(object)) return (AnnotatedElement) object;
		else return object.getClass();
	}
	
	public static boolean isWrapped(Object object) {
		return object instanceof Wrapper;
	}
}
