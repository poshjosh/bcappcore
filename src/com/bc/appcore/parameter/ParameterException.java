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

package com.bc.appcore.parameter;

import com.bc.appcore.exceptions.UserException;

/**
 * @author Chinomso Bassey Ikwuagwu on Jan 31, 2017 11:08:56 PM
 */
public class ParameterException extends Exception implements UserException {

    /**
     * Creates a new instance of <code>ParameterException</code> without detail message.
     */
    public ParameterException() {
    }


    /**
     * Constructs an instance of <code>ParameterException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParameterException(String msg) {
        super(msg);
    }

    public ParameterException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    @Override
    public String getUserMessage() {
        return this.getLocalizedMessage();
    }
}
