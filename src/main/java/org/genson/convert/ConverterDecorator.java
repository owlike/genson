package org.genson.convert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.genson.Converter;
import org.genson.Operations;


/**
 * ConverterDecorator class must be extended by decorated converters that wrap other converters. This allows to
 * access merged class information of wrapped converter and the converter itself. So instead of
 * doing myObject.getClass().isAnnotationPresent(..) you will do myObject.isAnnotationPresent(..),
 * where myObject is an instance of ConverterDecorator. For example to check if a converter (or any another
 * encapsulated converter and so on) has annotation @HandleNull you will do it that way:
 * 
 * <pre>
 * ConverterDecorator.toAnnotatedElement(converter).isAnnotationPresent(HandleNull.class);
 * </pre>
 * 
 * In the future there may be other methods to access other kind of class information.
 * 
 * @author eugen
 */
public abstract class ConverterDecorator<T> implements Converter<T>, AnnotatedElement {
	private AnnotatedElement wrappedElement;
	protected Converter<T> wrapped;

	ConverterDecorator() {
	}

	protected ConverterDecorator(Converter<T> wrappedObject) {
		if (wrappedObject == null)
			throw new IllegalArgumentException("Null not allowed!");
		decorate(wrappedObject);
	}

	@Override
	public Annotation[] getAnnotations() {
		return Operations.union(Annotation[].class, wrappedElement.getAnnotations(), getClass()
				.getAnnotations());
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> aClass) {
		A ann = wrappedElement.getAnnotation(aClass);
		return ann == null ? getClass().getAnnotation(aClass) : ann;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return Operations.union(Annotation[].class, wrappedElement.getDeclaredAnnotations(),
				getClass().getDeclaredAnnotations());
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return wrappedElement.isAnnotationPresent(annotationClass)
				|| getClass().isAnnotationPresent(annotationClass);
	}

	// package visibility as a convenience for CircularClassReferenceConverter
	void decorate(Converter<T> object) {
		if (wrappedElement != null)
			throw new IllegalStateException("An object is already wrapped!");
		if (object instanceof AnnotatedElement)
			this.wrappedElement = (AnnotatedElement) object;
		else
			this.wrappedElement = object.getClass();
		this.wrapped = object;
	}

	public Converter<T> unwrap() {
		return wrapped;
	}

	/**
	 * This method acts as an adapter to AnnotatedElement, use it when you need to work on a
	 * converter annotations. In fact "object" argument will usually be of type converter. If this
	 * class is a wrapper than it will cast it to annotatedElement (as ConverterDecorator implements
	 * AnnotatedElement). Otherwise we will return the class of this object.
	 * 
	 * @param object may be an instance of converter for example
	 * @return an annotatedElement that allows us to get annotations from this object and it's
	 *         wrapped classes if it is a ConverterDecorator.
	 */
	public static AnnotatedElement toAnnotatedElement(Object object) {
		if (object == null)
			return null;
		if (isWrapped(object))
			return (AnnotatedElement) object;
		else
			return object.getClass();
	}

	public static boolean isWrapped(Object object) {
		return object instanceof ConverterDecorator;
	}
}
