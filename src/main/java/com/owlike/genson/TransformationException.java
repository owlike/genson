package com.owlike.genson;

/**
 * @deprecated in favor of unchecked exceptions, will be removed in release 1.0.
 * If you want to handle binding exceptions, catch JsonBindingException instead.
 */
public final class TransformationException extends JsonBindingException {
	private static final long serialVersionUID = -1267673244135892897L;

    public TransformationException(String message) {
        super(message);
    }

	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}