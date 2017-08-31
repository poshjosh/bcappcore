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

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 1, 2017 9:59:28 PM
 */
public class InvalidParameterException extends ParameterException {

    /**
     * Creates a new instance of <code>InvalidParameterException</code> without detail message.
     */
    public InvalidParameterException() {
    }


    /**
     * Constructs an instance of <code>InvalidParameterException</code> with the specified detail message.
     * @param parameterName The name of the parameter.
     */
    public InvalidParameterException(String parameterName) {
        super(parameterName);
    }

    public InvalidParameterException(String parameterName, Throwable rootCause) {
        super(parameterName, rootCause);
    }

    @Override
    public String getUserMessage() {
        return "Invalid value for parameter: " + this.getLocalizedMessage();
    }
}
