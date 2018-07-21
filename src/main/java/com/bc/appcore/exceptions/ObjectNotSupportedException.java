package com.bc.appcore.exceptions;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2017 5:01:21 PM
 */
public class ObjectNotSupportedException extends ObjectFactoryException {

    /**
     * Creates a new instance of <code>ObjectNotRegisteredException</code> without detail message.
     */
    public ObjectNotSupportedException() { }

    /**
     * Constructs an instance of <code>ObjectNotRegisteredException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ObjectNotSupportedException(String msg) {
        super(msg);
    }
}
