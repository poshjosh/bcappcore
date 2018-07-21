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

package com.bc.appcore.properties;

import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 2, 2017 10:17:33 AM
 */
public class AuthSvcNameToLabel implements AuthSvcProperties, Function<String, String> {

    @Override
    public String apply(String name) {
        switch(name) {
            case SVC_ENDPOINT: return "Service end point";
            case APP_NAME: return "App name";
            case APP_EMAIL: return "App email";
            case APP_PASS: return "App password";
            case APP_TOKEN_FILENAME: return "App token file name";
            case APP_DETAILS_FILENAME: return "App details file name";
            default: throw new IllegalArgumentException(name);
        }
    }
}
