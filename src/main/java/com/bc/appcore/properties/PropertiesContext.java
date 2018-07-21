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

import com.bc.appcore.PathContext;
import java.nio.file.Path;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 20, 2017 9:53:42 AM
 */
public interface PropertiesContext extends PathContext {
    
    interface TypeName{
        String APP = "app";
        String LOGGING = "logging";
        String SETTINGS = "settings";
        String AUTHSVC = "authsvc";
        String USER_AUTH = "user_auth";
        String JDBC_JPA = "jdbc_jpa";
    }
    
    static PropertiesContextBuilder builder() {
        return new PropertiesContextBuilder();
    }

    String getWorkingDirPath();
    
    String getDirPath();
    
    default Path getApp() {
        return get(TypeName.APP);
    }
    default Path getLogging() {
        return get(TypeName.LOGGING);
    }
    default Path getSettings() {
        return get(TypeName.SETTINGS);
    }
    default Path getAuthsvc() {
        return get(TypeName.AUTHSVC);
    }
    default Path getUserAuth() {
        return get(TypeName.USER_AUTH);
    }
    default Path getJpaJdbc() {
        return get(TypeName.JDBC_JPA);
    }
}
