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

package com.bc.appcore;

import com.authsvc.client.AppAuthenticationSession;
import com.authsvc.client.ResponseIsErrorTestImpl;
import com.authsvc.client.parameters.Createuser;
import java.io.IOException;
import java.text.ParseException;
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
public class UserImpl extends AbstractUser {

    private transient static final Logger logger = Logger.getLogger(UserImpl.class.getName());

    private final AppAuthenticationSession authSession;
    
    private final Function<String, String> nameToEmail;
    
    public UserImpl(AppAuthenticationSession authSession, Function<String, String> nameToEmail) {
        this.authSession = Objects.requireNonNull(authSession);
        this.nameToEmail = Objects.requireNonNull(nameToEmail);
    }
    
    @Override
    public boolean create(Map params) throws LoginException {
        return this.fetchUser(params, true);
    }

    @Override
    public boolean login(Map params) throws LoginException {    
    
        return this.fetchUser(params, false);
    }
        
    public boolean fetchUser(Map params, boolean create) 
            throws LoginException {    
        
        if(this.isLoggedIn()) {
            logger.warning(() -> "User is already logged in as: " + getName());
            return false;
        }
        if (!authSession.isServiceAvailable()) {
            throw new LoginException("Login Service Unavailable");
        }
        
        try{
            
            if(create) {
                params = new LinkedHashMap(params);
                params.put(Createuser.ParamName.sendregistrationmail.name(), false);
                params.put(Createuser.ParamName.activateuser.name(), true);
            }
            
            final String usernameKey = com.authsvc.client.parameters.Getuser.ParamName.username.name();
            
            final Object userOval = params.getOrDefault(usernameKey, null);
            if(userOval == null) {
                throw new LoginException("Please provide a " + usernameKey);
            } 
            
            final Object emailOval = params.getOrDefault(com.authsvc.client.parameters.Getuser.ParamName.emailaddress.name(), null);
            if(emailOval == null) {
                params = new LinkedHashMap(params);
                final String email = nameToEmail.apply(userOval.toString());
                logger.log(Level.FINE, () -> "Name: "+userOval+" converted to email: "+email);
                params.put(com.authsvc.client.parameters.Getuser.ParamName.emailaddress.name(), 
                        email);
            }
            
            final String passwordKey = com.authsvc.client.parameters.Getuser.ParamName.password.name();
            final Object passOval = params.getOrDefault(passwordKey, null);
            if(passOval == null) {
                throw new LoginException("Please provide a " + passwordKey);
            } 
            
            if(passOval instanceof char[]) {
                final String pass = new String((char[])passOval);
                params = new LinkedHashMap(params);
                params.put(com.authsvc.client.parameters.Getuser.ParamName.password.name(), pass);
            }
            logger.log(Level.FINE, "Request params: {0}", params.keySet());
            
            final Map response = create ? authSession.createUser(params) : authSession.loginUser(params);
            logger.log(Level.FINE, "Response: {0}", response);
            
            if(new ResponseIsErrorTestImpl().accept(response)) {
                throw new LoginException(String.valueOf(response.values().iterator().next()));
            }
            
            final String username = (String)params.get(com.authsvc.client.parameters.Getuser.ParamName.username.name());
            logger.log(Level.FINE, "Logged in user: {0}", username);
            
            this.setName(username);
            
            this.setLoggedIn(true);
            
            final boolean isLoggedIn = this.isLoggedIn();
            logger.log(Level.FINER, "Is logged in: {0}", isLoggedIn);
            return isLoggedIn;
            
        }catch(IOException e) {
            
            final String msg = "Error accessing Login Service";
            
            logger.log(Level.WARNING, msg, e);
            
            throw new LoginException(msg);
            
        }catch(ParseException e) {
            
            final String msg = "Invalid response from Login Service";
            
            logger.log(Level.WARNING, msg, e);
            
            throw new LoginException(msg);
        }
    }
}
