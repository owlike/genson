package com.owlike.genson;

/**
 * @deprecated will be removed in release 1.0, catch instead JsonBindingException.
 */
public final class TransformationRuntimeException extends JsonBindingException {
	private static final long serialVersionUID = 3298864591780352514L;

	public TransformationRuntimeException(String message) {
		super(message);
	}
	
	public TransformationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
