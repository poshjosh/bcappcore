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
import com.bc.appcore.util.ExpirableCache;
import com.bc.config.Config;
import com.bc.jpa.JpaContext;
import com.bc.jpa.sync.JpaSync;
import java.util.Objects;
import java.util.Properties;
import com.bc.jpa.sync.PendingUpdatesManager;
import com.bc.appcore.properties.PropertiesContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2017 6:15:37 PM
 */
public class AppContextBuilder implements AppContext {
    
    private ClassLoader classLoader;
    private PropertiesContext propertiesPaths;
    private Config config;
    private Properties settingsConfig;
    private JpaContext jpaContext;
    private PendingUpdatesManager pendingMasterUpdatesManager;
    private PendingUpdatesManager pendingSlaveUpdatesManager;
    private JpaSync jpaSync;
    private ExpirableCache<Object> expirableAttributes;
    private AppAuthenticationSession authenticationSession;

    private boolean buildAttempted;

    public AppContextBuilder() { }
    
    public AppContext build() {
        
        this.requireBuildNotYetAttempted("build() method has ");

        this.buildAttempted = true;
        
        return this;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    @Override
    public AppAuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }
    
    @Override
    public PropertiesContext getPropertiesPaths() {
        return propertiesPaths;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public Properties getSettingsConfig() {
        return settingsConfig;
    }

    @Override
    public JpaContext getJpaContext() {
        return jpaContext;
    }

    @Override
    public PendingUpdatesManager getPendingMasterUpdatesManager() {
        return pendingMasterUpdatesManager;
    }

    @Override
    public PendingUpdatesManager getPendingSlaveUpdatesManager() {
        return pendingSlaveUpdatesManager;
    }

    @Override
    public JpaSync getJpaSync() {
        return jpaSync;
    }

    @Override
    public ExpirableCache<Object> getExpirableAttributes() {
        return expirableAttributes;
    }

    public boolean isBuildAttempted() {
        return buildAttempted;
    }

    public AppContextBuilder classLoader(ClassLoader classLoader) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.classLoader = classLoader;
        return this;
    }
    
    public AppContextBuilder authenticationSession(AppAuthenticationSession authenticationSession) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.authenticationSession = authenticationSession;
        return this;
    }
    
    public AppContextBuilder propertiesPaths(PropertiesContext arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.propertiesPaths = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder config(Config arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.config = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder settingsConfig(Properties arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.settingsConfig = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder jpaContext(JpaContext arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.jpaContext = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder pendingMasterUpdatesManager(PendingUpdatesManager arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.pendingMasterUpdatesManager = Objects.requireNonNull(arg);
        return this;
    }
    
    public AppContextBuilder pendingSlaveUpdatesManager(PendingUpdatesManager arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.pendingSlaveUpdatesManager = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder jpaSync(JpaSync arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.jpaSync = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder expirableCache(ExpirableCache arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.expirableAttributes = Objects.requireNonNull(arg);
        return this;
    }

    public void requireBuildNotYetAttemptedBeforeFieldUpdate() {
        if(buildAttempted) {
            throw new IllegalStateException("This method or any update methods may not be called after build() method is called");
        }
    }

    
    public void requireBuildNotYetAttempted(String msg) {
        if(buildAttempted) {
            throw new IllegalStateException(msg);
        }
    }
}
