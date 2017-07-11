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
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 6:01:49 PM
 */
public class TargetNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>TargetNotFoundException</code> without detail message.
     */
    public TargetNotFoundException() {
    }


    /**
     * Constructs an instance of <code>TargetNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TargetNotFoundException(String msg) {
        super(msg);
    }
}
