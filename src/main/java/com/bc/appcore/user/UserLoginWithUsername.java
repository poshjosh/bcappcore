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

package com.bc.appcore.user;

import com.authsvc.client.AppAuthenticationSession;
import com.authsvc.client.JsonResponseIsErrorTest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 29, 2017 9:20:59 PM
 */
public class UserLoginWithUsername extends UserImpl {

    private transient static final Logger logger = Logger.getLogger(UserLoginWithUsername.class.getName());

    private final Function<String, String> nameToEmail;
    
    public UserLoginWithUsername(
            AppAuthenticationSession authSession, 
            JsonResponseIsErrorTest jsonResponseIsErrorTest,
            Function<String, String> nameToEmail) {
        super(authSession, jsonResponseIsErrorTest);
        this.nameToEmail = Objects.requireNonNull(nameToEmail);
    }

    @Override
    public Map format(Map params, boolean create) throws LoginException {
        final Map.Entry userEntry = this.requireNonNull(params, 
                com.authsvc.client.parameters.Getuser.ParamName.username.name());
        final Object emailOval = params.getOrDefault(com.authsvc.client.parameters.Getuser.ParamName.emailaddress.name(), null);
        if(emailOval == null) {
            final Object userOval = userEntry.getValue();
            params = new LinkedHashMap(params);
            final String email = nameToEmail.apply(userOval.toString());
            logger.log(Level.FINE, () -> "Name: "+userOval+" converted to email: "+email);
            params.put(com.authsvc.client.parameters.Getuser.ParamName.emailaddress.name(), 
                    email);
        }
        return super.format(params, create);
    }
}
