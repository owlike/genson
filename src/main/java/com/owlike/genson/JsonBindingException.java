package com.owlike.genson;

public class JsonBindingException extends RuntimeException {

    public JsonBindingException(String message) {
        super(message);
    }

    public JsonBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
