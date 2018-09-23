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
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.jpa.SearchContextImpl;
import com.bc.appcore.util.ExpirableCache;
import java.util.Properties;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.config.Config;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.sync.MasterSlaveSwitch;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2017 5:48:58 PM
 */
public interface AppContext {
    
    boolean isSyncEnabled();
    
    ClassLoader getClassLoader();
    
    Optional<AppAuthenticationSession> getAuthenticationSession();
    
    default String getWorkingDir() {
        return this.getPropertiesContext().getWorkingDirPath();
    }
    
    PropertiesContext getPropertiesContext();
    
    Config getConfig();
    
    default boolean saveConfig() {
        final String typeName = PropertiesContext.TypeName.APP;
        try{
            final String charsetName = this.getConfig().getString("charserName", "utf-8");
            final OutputStream out = this.getPropertiesContext().getOutputStream(typeName, false);
            try(Writer writer  = new OutputStreamWriter(out, charsetName)) {
                final String comment = "Stored by: "+System.getProperty("user.name")+" on " + ZonedDateTime.now();
                ((Properties)this.getConfig().getSourceData()).store(writer, comment);
            }
            return true;
        }catch(IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "For: "+this.getPropertiesContext().get(typeName), e);
            return false;
        }
    }
    
    Properties getSettingsConfig();
    
    PersistenceContext getPersistenceContext();
    
    default Dao getDao() {
        return this.getActivePersistenceUnitContext().getDao();
    }
    
    default PersistenceUnitContext getActivePersistenceUnitContext() {
        return this.getPersistenceContextSwitch().getActive();
    }
    
    MasterSlaveSwitch<PersistenceUnitContext> getPersistenceContextSwitch();
    
    default <T> SearchContext<T> getSearchContext(Class<T> entityType) {
        return new SearchContextImpl<>(this, Objects.requireNonNull(entityType), 20, true);
    }

    ExpirableCache<Object> getExpirableCache();
}    
