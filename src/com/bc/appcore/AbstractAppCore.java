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
import com.bc.appcore.exceptions.ObjectFactoryException;
import com.bc.config.Config;
import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.Dao;
import com.bc.appcore.jpa.SearchContextImpl;
import com.bc.jpa.sync.JpaSync;
import java.io.File;
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
import com.bc.appcore.functions.CreateActionFromClassName;
import com.bc.appcore.exceptions.TaskExecutionException;
import com.bc.appcore.exceptions.TargetNotFoundException;
import com.bc.appcore.jpa.SlaveUpdateListenerImpl;
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
import com.bc.jpa.sync.PendingUpdatesManager;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.appcore.actions.ActionCommandsCore;
import com.bc.appcore.actions.ActionQueue;
import com.bc.appcore.actions.ActionQueueImpl;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:26:00 PM
 */
public abstract class AbstractAppCore implements AppCore {
    
    private transient static final Logger logger = Logger.getLogger(AbstractAppCore.class.getName());

    private final AppContext appContext;
    
    private final Map<String, Object> attributes;
    
    private final ActionQueue actionQueue;
    
    private User user;
    
    private ObjectFactory objectFactory;
    
    private volatile boolean shutdown;
    
    public AbstractAppCore(AppContext appContext) {
        
        this.appContext = Objects.requireNonNull(appContext);
        
        this.deleteTempFiles(Paths.get(this.appContext.getWorkingDir()).toFile());
        
        this.attributes = new HashMap<>(); 
        
        this.actionQueue = new ActionQueueImpl();
        
        SlaveUpdateListenerImpl.app = AbstractAppCore.this;
    }
    
    @Override
    public void init() {
        
        this.objectFactory = this.createObjectFactory();
        
        final Settings settingsAreValidatedThus = this.objectFactory.getOrException(Settings.class);
        
        this.user = this.objectFactory.getOrException(User.class);
    }
    
    @Override
    public void shutdown() {
        
        this.shutdown = true;
        
        this.attributes.clear();
        
        if(!this.getPendingMasterUpdatesManager().isStopRequested()) {
            this.getPendingMasterUpdatesManager().requestStop();
        }
        
        if(!this.getPendingSlaveUpdatesManager().isStopRequested()) {
            this.getPendingSlaveUpdatesManager().requestStop();
        }
        
        if(this.getJpaContext().isOpen()) {
            this.getJpaContext().close();
        }
        
        try{
            this.getExpirableAttributes().close();
        }catch(Exception e) {
            logger.log(Level.WARNING, "Error closing: " + this.getExpirableAttributes().getClass().getName(), e);
        }
        
        this.deleteTempFiles(Paths.get(this.getWorkingDir()).toFile());
    }
    
    @Override
    public User getUser() {
        return this.user;
    }
    
    protected ObjectFactory createObjectFactory() {
        return new ObjectFactoryImpl(this);
    }

    @Override
    public String getName() {
        return this.getConfig().getProperty("app.name", this.getClass().getSimpleName());
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

    private void deleteTempFiles(File dir) {
        try{
            Map<String, Object> params = Collections.singletonMap(File.class.getName(), dir);
            this.getAction(ActionCommandsCore.DELETE_TEMP_FILES_IN_DIR).execute(this, params);
        }catch(ParameterException | TaskExecutionException e) {
            logger.log(Level.WARNING, "Unexpected error", e);
        }
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
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
    
        final ExpirableCache<Object> expirableCache = this.getExpirableAttributes();
        
        final Expirable<T> expirable = getNotRemove ? expirableCache.get(key) : expirableCache.remove(key);

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
    public <T> SearchContext<T> getSearchContext(Class<T> entityType) {
        ResultModel<T> resultModel = this.getResultModel(entityType, null);
        return new SearchContextImpl<>(this, Objects.requireNonNull(resultModel), 20, true);
    }

    @Override
    public ActionQueue getActionQueue() {
        return this.actionQueue;
    }

    @Override
    public Action getAction(String actionCommand) {
        return this.getAction(actionCommand, Level.FINE);
    }

    public Action getAction(String actionCommand, Level logLevel) {
        final Action action = new CreateActionFromClassName(logLevel).apply(actionCommand);
        return action;
    }
    
    @Override
    public Calendar getCalendar() {
        return Calendar.getInstance(this.getTimeZone(), this.getLocale());
    }

    @Override
    public Dao getDao(Class entityType) {
        return this.getJpaContext().getDao(entityType);
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
        final String [] puNames = this.getJpaContext().getMetaData().getPersistenceUnitNames();
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

    @Override
    public ClassLoader getClassLoader() {
        return appContext.getClassLoader();
    }

    @Override
    public Predicate<String> getMasterPersistenceUnitTest() {
        return appContext.getMasterPersistenceUnitTest();
    }

    @Override
    public Predicate<String> getSlavePersistenceUnitTest() {
        return appContext.getSlavePersistenceUnitTest();
    }

    @Override
    public AppAuthenticationSession getAuthenticationSession() {
        return appContext.getAuthenticationSession();
    }

    @Override
    public PropertiesContext getPropertiesPaths() {
        return appContext.getPropertiesPaths();
    }

    @Override
    public Config getConfig() {
        return appContext.getConfig();
    }

    @Override
    public Properties getSettingsConfig() {
        return appContext.getSettingsConfig();
    }

    @Override
    public JpaContext getJpaContext() {
        return appContext.getJpaContext();
    }

    @Override
    public PendingUpdatesManager getPendingMasterUpdatesManager() {
        return appContext.getPendingMasterUpdatesManager();
    }

    @Override
    public PendingUpdatesManager getPendingSlaveUpdatesManager() {
        return appContext.getPendingSlaveUpdatesManager();
    }

    @Override
    public JpaSync getJpaSync() {
        return appContext.getJpaSync();
    }

    @Override
    public ExpirableCache<Object> getExpirableAttributes() {
        return appContext.getExpirableAttributes();
    }
}
