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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.security.auth.login.LoginException;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 9, 2017 2:25:08 AM
 */
public abstract class AbstractUser implements User, Serializable {

    private String name = "";
    
    private boolean loggedIn = false;
    
    public AbstractUser() { }
    
    @Override
    public boolean isAnonymous() {
        return "".equals(this.name);
    }
    
    @Override
    public boolean login(String email, String name, char[] pass) throws LoginException {
     
        final Map params = new HashMap(8, 0.75f);
        params.put(com.authsvc.client.parameters.Getuser.ParamName.emailaddress.name(), email);
        params.put(com.authsvc.client.parameters.Getuser.ParamName.username.name(), name);
        params.put(com.authsvc.client.parameters.Getuser.ParamName.password.name(), pass);
        
        return this.login(params);
    }

    @Override
    public boolean logout() {
        this.setName("");
        this.setLoggedIn(false);
        return !this.loggedIn;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    protected void setLoggedIn(boolean b) {
        this.loggedIn = b;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractUser other = (AbstractUser) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+'{' + ", name=" + name + ", loggedIn=" + loggedIn + '}';
    }
}
