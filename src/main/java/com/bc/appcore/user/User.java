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

import java.util.Map;
import javax.security.auth.login.LoginException;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 11, 2017 3:52:50 AM
 */
public interface User {
    
    boolean isAnonymous();
    
    String getName();
    
    boolean create(Map params) throws LoginException;
    
    boolean isLoggedIn();
    
    boolean login(String email, String name, char[] pass) throws LoginException;
    
    boolean login(Map params) throws LoginException;
    
    boolean logout();
}
