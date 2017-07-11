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

import com.bc.appcore.exceptions.ObjectFactoryException;
import com.bc.jpa.sync.SlaveUpdates;
import com.bc.config.Config;
import com.bc.config.ConfigService;
import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.DaoImpl;
import com.bc.appcore.jpa.SearchContextImpl;
import com.bc.jpa.sync.JpaSync;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.actions.Action;
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.actions.ActionCommandsBase;
import com.bc.appcore.actions.TaskExecutionException;
import com.bc.appcore.exceptions.TargetNotFoundException;
import com.bc.appcore.jpa.model.ResultModel;
import com.bc.appcore.parameter.ParameterException;
import com.bc.appcore.predicates.AcceptAll;
import com.bc.appcore.util.Expirable;
import com.bc.appcore.util.ExpirableCache;
import com.bc.appcore.util.Settings;
import com.bc.util.JsonFormat;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:26:00 PM
 */
public abstract class AbstractAppCore implements AppCore {
    
    private transient static final Logger logger = Logger.getLogger(AbstractAppCore.class.getName());
    
    private final Filenames filenames;
    
    private final JpaContext jpaContext;
    
    private final ConfigService configService;
    
    private final Config config;
    
    private final Properties settingsConfig;
    
    private final Map<String, Object> attributes;
    
    private final SlaveUpdates slaveUpdates;
    
    private final JpaSync jpaSync;
    
    private final ExpirableCache<Object> expirableCache;
    
    private ObjectFactory objectFactory;
    
    private volatile boolean shutdown;
    
    public AbstractAppCore(
            Filenames filenames, ConfigService configService, 
            Config config, Properties settingsConfig, JpaContext jpaContext,
            SlaveUpdates slaveUpdates, JpaSync jpaSync, ExpirableCache expirableCache) {
        
        this.filenames = Objects.requireNonNull(filenames);
        
        this.deleteTempFiles(Paths.get(this.filenames.getWorkingDir()).toFile());
        
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.configService = Objects.requireNonNull(configService);
        this.config = Objects.requireNonNull(config);
        this.settingsConfig = Objects.requireNonNull(settingsConfig);
        this.slaveUpdates = Objects.requireNonNull(slaveUpdates);
        this.jpaSync = Objects.requireNonNull(jpaSync);
        
        this.attributes = new HashMap<>(); 
        
        this.expirableCache = Objects.requireNonNull(expirableCache);
    }
    
    @Override
    public void init() {
        this.objectFactory = this.createObjectFactory();
    }
    
    protected ObjectFactory createObjectFactory() {
        return new ObjectFactoryImpl(this);
    }

    @Override
    public String getName() {
        return this.config.getProperty("application.name", this.getClass().getSimpleName());
    }

    @Override
    public <T> T getOrDefault(Class<T> type, T outputIfNone) {
        return this.objectFactory.getOrDefault(type, outputIfNone);
    }

    @Override
    public <T> T getOrException(Class<T> type) throws ObjectFactoryException {
        return this.objectFactory.getOrException(type);
    }

    @Override
    public <T> void registerDefault(Class<T> type, Supplier<T> typeSupplier) {
        this.objectFactory.registerDefault(type, typeSupplier);
    }

    @Override
    public void deregisterDefault(Class type) {
        this.objectFactory.deregisterDefault(type);
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public void shutdown() {
        
        this.shutdown = true;
        
        this.attributes.clear();
        
        if(!this.slaveUpdates.isStopRequested()) {
            this.slaveUpdates.requestStop();
        }
        
        if(this.jpaContext.isOpen()) {
            this.jpaContext.close();
        }
        
        try{
            expirableCache.close();
        }catch(Exception e) {
            logger.log(Level.WARNING, "Error closing: "+expirableCache.getClass().getName(), e);
        }
        
        this.deleteTempFiles(Paths.get(this.filenames.getWorkingDir()).toFile());
    }
    
    private void deleteTempFiles(File dir) {
        try{
            Map<String, Object> params = Collections.singletonMap(File.class.getName(), dir);
            this.getAction(ActionCommandsBase.DELETE_TEMP_FILES_IN_DIR).execute(this, params);
        }catch(ParameterException | TaskExecutionException e) {
            logger.log(Level.WARNING, "Unexpected error", e);
        }
    }

    @Override
    public JpaSync getJpaSync() {
        return this.jpaSync;
    }
    
    @Override
    public SlaveUpdates getSlaveUpdates() {
        return this.slaveUpdates;
    }
    
    @Override
    public Filenames getFilenames() {
        return filenames;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public ExpirableCache<Object> getExpirableAttributes() {
        return this.expirableCache;
    }

    @Override
    public <T> T removeExpirable(Class<T> type, Object key) throws TargetNotFoundException {
        return this.fetchExpirable(type, key, false);
    }

    @Override
    public <T> T getExpirable(Class<T> type, Object key) throws TargetNotFoundException {
        return this.fetchExpirable(type, key, true);
    }
        
    public <T> T fetchExpirable(Class<T> type, Object key, boolean getNotRemove) throws TargetNotFoundException {
    
        final Expirable<T> expirable = getNotRemove ? this.expirableCache.get(key) : this.expirableCache.remove(key);

        if(expirable == null) {
            throw new TargetNotFoundException("Session has expired. Begin process afresh");
        }

        final Optional<T> optional = expirable.get();
        
        if(!optional.isPresent()) {
            throw new NullPointerException();
        }
        
        final T output = optional.get();
        
        return output;
    }

    @Override
    public UserBase getUser() {
        return new UserBaseImpl("guest", false);
    }

    @Override
    public <T> SearchContext<T> getSearchContext(Class<T> entityType) {
        ResultModel<T> resultModel = this.getResultModel(entityType, null);
        return new SearchContextImpl<>(this, Objects.requireNonNull(resultModel), 20, true);
    }

    @Override
    public Action getAction(String actionCommand) {
        try{
            final Class aClass = Class.forName(actionCommand);
            final Action action = (Action)aClass.getConstructor().newInstance();
            logger.log(Level.FINE, "Created action: {0}", action);
            return action;
        }catch(ClassNotFoundException | NoSuchMethodException | SecurityException | 
                InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Calendar getCalendar() {
        return Calendar.getInstance(this.getTimeZone(), this.getLocale());
    }

    @Override
    public Dao getDao(Class entityType) {
        return new DaoImpl(this.getEntityManager(entityType));
    }

    @Override
    public JpaContext getJpaContext() {
        return this.jpaContext;
    }

    /**
     * This returns the actual persistence unit names used by the application. And it is
     * typically a subset of those returned by {@link #getJpaContext()#getPersistenceUnitNames()}.
     * @return The names of the persistence units used by the application
     * @see #getJpaContext() 
     */
    @Override
    public Set<String> getPersistenceUnitNames() {
        final Predicate<String> puNameTest = this.getPersistenceUnitNameTest();
        final String [] puNames = jpaContext.getMetaData().getPersistenceUnitNames();
        final Set<String> accepted = new HashSet();
        for(String puName : puNames) {
            if(!puNameTest.test(puName)) {
                continue;
            }
            accepted.add(puName);
        }
        return accepted;
    }
    
    public Predicate<String> getPersistenceUnitNameTest() {
        return new AcceptAll();
    }

    @Override
    public ConfigService getConfigService() {
        return this.configService;
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    @Override
    public Properties getSettingsConfig() {
        return this.settingsConfig;
    }

    @Override
    public Settings getSettings() {
        return this.getOrException(Settings.class);
    }
    
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public JsonFormat getJsonFormat() {
        return new JsonFormat(true, true, "  ");
    }
}
