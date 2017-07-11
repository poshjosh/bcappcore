/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.appcore.exceptions;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 29, 2017 5:30:02 PM
 */
public class ObjectFactoryException extends RuntimeException {

    /**
     * Creates a new instance of <code>ObjectFactoryException</code> without detail message.
     */
    public ObjectFactoryException() { }

    /**
     * Constructs an instance of <code>ObjectFactoryException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ObjectFactoryException(String msg) {
        super(msg);
    }

    public ObjectFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectFactoryException(Throwable cause) {
        super(cause);
    }

    public ObjectFactoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
