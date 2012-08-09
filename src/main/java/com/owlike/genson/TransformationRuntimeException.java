package com.owlike.genson;

/**
 * This exception is thrown when there went something wrong and you can not recover from that.
 * You should not try to catch this exception.
 * 
 * @author eugen
 *
 */
public class TransformationRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 3298864591780352514L;

	public TransformationRuntimeException(String message) {
		super(message);
	}
	
	public TransformationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
