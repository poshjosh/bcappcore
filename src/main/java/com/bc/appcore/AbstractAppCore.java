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

import com.bc.appcore.user.User;
import com.authsvc.client.AppAuthenticationSession;
import com.bc.appcore.exceptions.ObjectFactoryException;
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
import com.bc.appcore.parameter.ParameterException;
import com.bc.appcore.util.Expirable;
import com.bc.appcore.util.ExpirableCache;
import com.bc.appcore.util.Settings;
import com.bc.util.JsonFormat;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.appcore.actions.ActionCommandsCore;
import com.bc.appcore.actions.ActionQueue;
import com.bc.appcore.actions.ActionQueueImpl;
import com.bc.appcore.util.PendingUpdateConsumer;
import com.bc.appcore.util.TargetQueue;
import com.bc.appcore.util.TargetQueueImpl;
import com.bc.config.Config;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.sync.MasterSlavePersistenceContext;
import com.bc.jpa.sync.MasterSlaveSwitch;
import com.bc.jpa.sync.impl.JpaSyncImpl;
import com.bc.jpa.sync.impl.MasterSlavePersistenceContextImpl;
import com.bc.jpa.predicates.DatabaseCommunicationsFailureTest;
import java.nio.file.Path;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:26:00 PM
 */
public abstract class AbstractAppCore implements AppCore {
    
    private transient static final Logger logger = Logger.getLogger(AbstractAppCore.class.getName());

    private final AppContext appContext;
    
    private final Map<String, Object> attributes;
    
    private final ActionQueue actionQueue;
    
    private final TargetQueue pendingSlaveUpdatesManager;
    
    private User user;
    
    private ObjectFactory objectFactory;
    
    private volatile boolean shutdown;
    
    public AbstractAppCore(AppContext appContext) {
    
        this.appContext = Objects.requireNonNull(appContext);
        
        this.deleteTempFiles(Paths.get(this.appContext.getWorkingDir()).toFile());
        
        this.attributes = new HashMap<>(); 
        
        this.actionQueue = new ActionQueueImpl();

        this.pendingSlaveUpdatesManager = this.createPendingSlaveUpdateQueue();
    }
    
    @Override
    public void init() {
        
        this.objectFactory = this.createObjectFactory();
        
        final Settings settingsAreValidatedThus = this.objectFactory.getOrException(Settings.class);
        
        this.user = this.objectFactory.getOrException(User.class);

        logger.fine(() -> "Created user: " + this.user);
    }
    
    @Override
    public void shutdown() {
        
        this.shutdown = true;
        
        this.attributes.clear();
        
        if(!this.getPendingSlaveUpdateQueue().isStopRequested()) {
            this.getPendingSlaveUpdateQueue().requestStop();
        }
        
        if(this.getPersistenceContext().isOpen()) {
            this.getPersistenceContext().close();
        }
        
        try{
            this.getExpirableCache().close();
        }catch(Exception e) {
            logger.log(Level.WARNING, "Error closing: " + this.getExpirableCache().getClass().getName(), e);
        }
        
        this.deleteTempFiles(Paths.get(this.getWorkingDir()).toFile());
    }

    @Override
    public String getName() {
        return this.getConfig().get(APP_NAME, this.getClass().getSimpleName());
    }
    
    @Override
    public String getSerialColumnName() {
        return this.getConfig().getString(SERIAL_COLUMNNAME, "serial");
    }

    @Override
    public String getDateTimePattern() {
        return this.getConfig().getString(DATETIME_PATTERN, "dd-MMM-yy HH:mm");
    }

    @Override
    public String getDatePattern() {
        return this.getConfig().getString(DATE_PATTERN, "dd-MMM-yy");
    }

    @Override
    public MasterSlavePersistenceContext getMasterSlavePersistenceContext() {
        final MasterSlaveSwitch<PersistenceUnitContext> masterSlaveSwitch = this.getPersistenceContextSwitch();
        final PersistenceUnitContext masterCtx = masterSlaveSwitch.getMaster();
        final PersistenceUnitContext slaveCtx = masterSlaveSwitch.getSlaveOptional().orElse(null);
        return new MasterSlavePersistenceContextImpl(masterCtx, slaveCtx);
    }
    
    @Override
    public PersistenceUnitContext getActivePersistenceUnitContext() {
        return this.getPersistenceContextSwitch().getActive();
    }
    
    @Override
    public JpaSync getJpaSync() {
        return !this.isSyncEnabled() || !this.getPersistenceContextSwitch().getSlaveOptional().isPresent() ? 
                JpaSync.NO_OP :
                new JpaSyncImpl(
                        this.getPersistenceContextSwitch().getMaster(),
                        this.getPersistenceContextSwitch().getSlaveOptional().get(),
                        20, 
                        this.getOrException(DatabaseCommunicationsFailureTest.class));
    }
    
    protected TargetQueue createPendingSlaveUpdateQueue() {
        return !this.isSyncEnabled() || !this.getPersistenceContextSwitch().getSlaveOptional().isPresent() ? 
                TargetQueue.NO_OP :
                new TargetQueueImpl(
                        this.getPendingUpdatesFilePath(Names.PENDING_SLAVE_UPDATES_FILE_NAME).toFile(),
                        new PendingUpdateConsumer(
                                this.getPersistenceContextSwitch().getSlaveOptional()
                                        .orElseThrow(() -> new NullPointerException())
                        ),
                        new DatabaseCommunicationsFailureTest());
    }

    protected Path getPendingUpdatesFilePath(String fname) {
        return Paths.get(this.getPropertiesContext().getWorkingDirPath(), Names.PENDING_UPDATES_DIR, fname);
    }
    
    @Override
    public User getUser() {
        return this.user;
    }
    
    protected ObjectFactory createObjectFactory() {
        return new ObjectFactoryImpl(this);
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
    public ObjectFactory getParent() {
        return this.objectFactory.getParent();
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
    
        final ExpirableCache<Object> expirableCache = this.getExpirableCache();
        
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
    public ActionQueue getActionQueue() {
        return this.actionQueue;
    }

    @Override
    public Action getAction(String actionCommand) {
        return this.getAction(actionCommand, Level.FINER);
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
    public <T> SearchContext<T> getSearchContext(Class<T> entityType) {
        return this.appContext.getSearchContext(entityType);
    }

    @Override
    public boolean isSyncEnabled() {
        return appContext.isSyncEnabled();
    }

    @Override
    public ClassLoader getClassLoader() {
        return appContext.getClassLoader();
    }

    @Override
    public MasterSlaveSwitch<PersistenceUnitContext> getPersistenceContextSwitch() {
        return appContext.getPersistenceContextSwitch();
    }

    @Override
    public Optional<AppAuthenticationSession> getAuthenticationSession() {
        return appContext.getAuthenticationSession();
    }

    @Override
    public PropertiesContext getPropertiesContext() {
        return appContext.getPropertiesContext();
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
    public PersistenceContext getPersistenceContext() {
        return appContext.getPersistenceContext();
    }

    @Override
    public TargetQueue getPendingSlaveUpdateQueue() {
        return this.pendingSlaveUpdatesManager;
    }

    @Override
    public ExpirableCache<Object> getExpirableCache() {
        return appContext.getExpirableCache();
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public TargetQueue getPendingSlaveUpdatesManager() {
        return pendingSlaveUpdatesManager;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }
}
