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
import com.bc.util.Util;
import com.bc.appcore.jpa.SearchContextImpl;
import com.bc.jpa.sync.JpaSync;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.actions.Action;
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.actions.ActionCommandsBase;
import com.bc.appcore.actions.TaskExecutionException;
import com.bc.appcore.jpa.model.ResultModel;
import com.bc.appcore.parameter.ParameterException;
import com.bc.appcore.util.Expirable;
import com.bc.appcore.util.Settings;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:26:00 PM
 */
public abstract class AbstractAppCore implements AppCore {
    
    private transient static final Logger logger = Logger.getLogger(AbstractAppCore.class.getName());
    
    private final Path workingDir;
    
    private final JpaContext jpaContext;
    
    private final ConfigService configService;
    
    private final Config config;
    
    private final Properties settingsConfig;
    
    private final Map<String, Object> attributes;
    
    private final ExecutorService updateOutputService;
    
    private final SlaveUpdates slaveUpdates;
    
    private final JpaSync jpaSync;
    
    private final ReadWriteLock expirablesLock;
    
    private final Map<Object, Expirable> expirables;
    
    private final ScheduledExecutorService clearExpiredService;
    
    private ObjectFactory objectFactory;
    
    private volatile boolean shutdown;
    
    public AbstractAppCore(
            Path workingDir, ConfigService configService, 
            Config config, Properties settingsConfig, JpaContext jpaContext,
            ExecutorService dataOutputService, SlaveUpdates slaveUpdates, JpaSync jpaSync) {
        
        this.workingDir = Objects.requireNonNull(workingDir);
        
        this.deleteTempFiles(Paths.get(this.workingDir.toString()).toFile());
        
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.configService = Objects.requireNonNull(configService);
        this.config = Objects.requireNonNull(config);
        this.settingsConfig = Objects.requireNonNull(settingsConfig);
        this.updateOutputService = Objects.requireNonNull(dataOutputService);
        this.slaveUpdates = Objects.requireNonNull(slaveUpdates);
        this.jpaSync = Objects.requireNonNull(jpaSync);
        
        this.attributes = new HashMap<>(); 
        
        this.expirablesLock = new ReentrantReadWriteLock();
        
        this.expirables = new HashMap<>();
        
        this.clearExpiredService = Executors.newSingleThreadScheduledExecutor();
        
        final Runnable clearExpired = new Runnable() {
            @Override
            public void run() {
                try{
                    final Iterator iter = expirables.keySet().iterator();
                    while(iter.hasNext()) {
                        final Expirable expirable;
                        try{
                            expirablesLock.readLock().lock();
                            final Object key = iter.next();
                            expirable = expirables.get(key);
                        }finally{
                            expirablesLock.readLock().unlock();
                        }
                        if(expirable.isExpired()) {
                            try{
                                expirablesLock.writeLock().lock();
                                iter.remove();
                            }finally{
                                expirablesLock.writeLock().unlock();
                            }
                        }
                    }
                }catch(RuntimeException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                            "Unexpected exception clearing expired "+Expirable.class.getName(), e);
                }
            }
        };
        clearExpiredService.scheduleWithFixedDelay(clearExpired, 10, 10, TimeUnit.SECONDS);
        clearExpiredService.shutdown();
    }
    
    @Override
    public void init() {
        this.init(new ObjectFactoryImpl(this));
    }
    
    protected void init(ObjectFactory objectFactory) {
        this.objectFactory = Objects.requireNonNull(objectFactory);
    }

    @Override
    public Expirable addExpirable(Object id, Expirable expirable) {
        try{
            this.expirablesLock.writeLock().lock();
            return this.expirables.put(id, expirable);
        }finally{
            this.expirablesLock.writeLock().unlock();
        }
    }

    @Override
    public Expirable getExpirable(Object id, Expirable outputIfNone) {
        try{
            this.expirablesLock.readLock().lock();
            return this.expirables.getOrDefault(id, outputIfNone);
        }finally{
            this.expirablesLock.readLock().unlock();
        }
    }

    @Override
    public Expirable removeExpirable(Object id, Expirable outputIfNone) {
        try{
            this.expirablesLock.writeLock().lock();
            final Expirable output = this.expirables.remove(id);
            return output == null ? outputIfNone : output;
        }finally{
            this.expirablesLock.writeLock().unlock();
        }
    }

    @Override
    public <T> T get(Class<T> type) {
        try{
            return this.objectFactory.get(type);
        }catch(ObjectFactoryException e) {
            throw new RuntimeException(e);
        }
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
        
        Util.shutdownAndAwaitTermination(this.updateOutputService, 1, TimeUnit.SECONDS);
        
        Util.shutdownAndAwaitTermination(this.clearExpiredService, 1, TimeUnit.SECONDS);
        
        this.deleteTempFiles(Paths.get(this.workingDir.toString()).toFile());
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
    public Path getWorkingDir() {
        return workingDir;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
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
        return this.get(Settings.class);
    }
    
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
}
