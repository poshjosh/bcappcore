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
import com.bc.appcore.jpa.predicates.MasterPersistenceUnitTest;
import com.bc.appcore.jpa.predicates.SlavePersistenceUnitTest;
import com.bc.appcore.util.ExpirableCache;
import com.bc.config.Config;
import com.bc.jpa.JpaContext;
import com.bc.jpa.sync.JpaSync;
import java.util.Properties;
import java.util.function.Predicate;
import com.bc.jpa.sync.PendingUpdatesManager;
import com.bc.appcore.properties.PropertiesContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2017 5:48:58 PM
 */
public interface AppContext {
    
    public static AppContextBuilder builder() {
        return new AppContextBuilder();
    }
    
    default Predicate<String> getMasterPersistenceUnitTest() {
        return new MasterPersistenceUnitTest();
    }
    
    default Predicate<String> getSlavePersistenceUnitTest() {
        return new SlavePersistenceUnitTest();
    }
    
    ClassLoader getClassLoader();
    
    AppAuthenticationSession getAuthenticationSession();
    
    default String getWorkingDir() {
        return this.getPropertiesPaths().getWorkingDirPath();
    }
    
    PropertiesContext getPropertiesPaths();
    
    Config getConfig();
    
    default boolean saveConfig() {
        final String typeName = PropertiesContext.TypeName.APP;
        try{
            final String charsetName = this.getConfig().getString("charserName", "utf-8");
            final OutputStream out = this.getPropertiesPaths().getOutputStream(typeName, false);
            try(Writer writer  = new OutputStreamWriter(out, charsetName)) {
                final String comment = "Stored by: "+System.getProperty("user.name")+" on "+new Date();
                this.getConfig().getProperties().store(writer, comment);
            }
            return true;
        }catch(IOException e) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING, "For: "+this.getPropertiesPaths().get(typeName), e);
            return false;
        }
    }
    
    Properties getSettingsConfig();
    
    JpaContext getJpaContext();
    
    PendingUpdatesManager getPendingMasterUpdatesManager();
    
    PendingUpdatesManager getPendingSlaveUpdatesManager();
    
    JpaSync getJpaSync();
    
    ExpirableCache<Object> getExpirableAttributes();
}    
