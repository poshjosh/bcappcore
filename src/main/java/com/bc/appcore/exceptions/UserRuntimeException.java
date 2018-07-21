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
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2017 1:25:21 PM
 */
public class UserRuntimeException extends RuntimeException implements HasUserMessage {

    /**
     * Creates a new instance of <code>UserRuntimeException</code> without detail message.
     */
    public UserRuntimeException() {
    }


    /**
     * Constructs an instance of <code>UserRuntimeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UserRuntimeException(String msg) {
        super(msg);
    }

    public UserRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserRuntimeException(Throwable cause) {
        super(cause);
    }

    public UserRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getUserMessage() {
        return this.getLocalizedMessage();
    }
}
