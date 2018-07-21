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
import java.util.Objects;
import java.util.Properties;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.appcore.util.ExpirableCacheImpl;
import com.bc.config.Config;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.sync.MasterSlaveSwitch;
import com.bc.jpa.sync.impl.MasterSlaveSwitchImpl;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2017 6:15:37 PM
 */
public class AppContextBuilder implements AppContext {

    private static final Logger logger = Logger.getLogger(AppContextBuilder.class.getName());
    
    private ClassLoader classLoader;
    private PropertiesContext propertiesPaths;
    private Config config;
    private Properties settingsConfig;
    private PersistenceContext persistenceContext;
    private Predicate<String> masterPersistenceUnitTest;
    private Predicate<String> slavePersistenceUnitTest;
    private MasterSlaveSwitch<PersistenceUnitContext> persistenceContextSwitch;
    private boolean syncEnabled;
    private ExpirableCache<Object> expirableCache;
    private AppAuthenticationSession authenticationSession;
    
    private boolean buildAttempted;

    public AppContextBuilder() { }
    
    public AppContext build() {

        this.requireBuildNotYetAttempted("build() method may only be called once");

        this.buildAttempted = true;
        
        if(expirableCache == null) {
            expirableCache = new ExpirableCacheImpl(30, TimeUnit.MINUTES);
        }
        
        if(this.masterPersistenceUnitTest == null) {
            this.masterPersistenceUnitTest = (name) -> true;
        }
        
        if(this.slavePersistenceUnitTest == null) {
            this.slavePersistenceUnitTest = this.masterPersistenceUnitTest.negate();
        }
        
        if(this.persistenceContextSwitch == null) {
            
            final Function<String, PersistenceUnitContext> nameToPuContext = 
                    (puName) -> this.persistenceContext.getContext(puName);
            
            final Set<PersistenceUnitContext> puContextSet = this.persistenceContext
                    .getMetaData(false).getPersistenceUnitNames().stream()
                    .map(nameToPuContext).collect(Collectors.toSet());
            
            this.persistenceContextSwitch = new MasterSlaveSwitchImpl<>(
                    puContextSet, 
                    (ctx) -> this.masterPersistenceUnitTest.test(ctx.getName()), 
                    (ctx) -> this.slavePersistenceUnitTest.test(ctx.getName())
            );
        }
        
        logger.finer(() -> "Persistence units. Master:: test: "+this.masterPersistenceUnitTest+
                ", name: " + this.persistenceContextSwitch.getMaster().getName()+
                "\nSlave:: test: " + this.slavePersistenceUnitTest + 
                ", name: " + this.persistenceContextSwitch.getSlaveOptional().map((ctx) -> ctx.getName()).orElse(null));
        
        this.persistenceContextSwitch.switchToMaster();
        
        return this;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Optional<AppAuthenticationSession> getAuthenticationSession() {
        return Optional.ofNullable(authenticationSession);
    }
    
    @Override
    public PropertiesContext getPropertiesContext() {
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
    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    @Override
    public MasterSlaveSwitch<PersistenceUnitContext> getPersistenceContextSwitch() {
        return this.persistenceContextSwitch;
    }

    @Override
    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    @Override
    public ExpirableCache<Object> getExpirableCache() {
        return expirableCache;
    }

    public boolean isBuildAttempted() {
        return buildAttempted;
    }

    public AppContextBuilder classLoader(ClassLoader classLoader) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.classLoader = classLoader;
        return this;
    }

    public AppContextBuilder masterPersistenceUnitTest(Predicate<String> masterPersistenceUnitTest) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.masterPersistenceUnitTest = masterPersistenceUnitTest;
        return this;
    }

    public AppContextBuilder slavePersistenceUnitTest(Predicate<String> slavePersistenceUnitTest) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.slavePersistenceUnitTest = slavePersistenceUnitTest;
        return this;
    }
    
    public AppContextBuilder authenticationSession(AppAuthenticationSession authenticationSession) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.authenticationSession = authenticationSession;
        return this;
    }
    
    public AppContextBuilder propertiesContext(PropertiesContext arg) {
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

    public AppContextBuilder persistenceContext(PersistenceContext arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.persistenceContext = Objects.requireNonNull(arg);
        return this;
    }

    public AppContextBuilder syncEnabled(boolean arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.syncEnabled = arg;
        return this;
    }

    public AppContextBuilder expirableCache(ExpirableCache arg) {
        this.requireBuildNotYetAttemptedBeforeFieldUpdate();
        this.expirableCache = Objects.requireNonNull(arg);
        return this;
    }

    public void requireBuildNotYetAttemptedBeforeFieldUpdate() {
        this.requireBuildNotYetAttempted("This method or any update methods may not be called after build() method is called");
    }
    
    public void requireBuildNotYetAttempted(String msg) {
        if(buildAttempted) {
            throw new IllegalStateException(msg);
        }
    }
}
