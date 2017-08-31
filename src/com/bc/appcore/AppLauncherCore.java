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

import com.bc.appcore.jpa.JpaContextManager;
import com.bc.appcore.jpa.JpaContextManagerImpl;
import com.authsvc.client.AppAuthenticationSession;
import com.authsvc.client.AppAuthenticationSessionImpl;
import com.authsvc.client.AuthenticationException;
import com.bc.appcore.jpa.predicates.MasterPersistenceUnitTest;
import com.bc.appcore.jpa.predicates.SlavePersistenceUnitTest;
import com.bc.appcore.properties.AuthSvcProperties;
import com.bc.appcore.util.ExpirableCache;
import com.bc.appcore.util.ExpirableCacheImpl;
import com.bc.appcore.util.Copy;
import com.bc.appcore.util.LoggingConfigManagerImpl;
import com.bc.config.Config;
import com.bc.jpa.JpaContext;
import com.bc.jpa.sync.JpaSync;
import com.bc.jpa.sync.PendingUpdatesManager;
import com.bc.jpa.sync.impl.JpaSyncImpl;
import com.bc.jpa.sync.impl.PendingUpdatesManagerImpl;
import com.bc.jpa.sync.impl.UpdaterImpl;
import com.bc.jpa.sync.predicates.PersistenceCommunicationsLinkFailureTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.config.ConfigImpl;
import java.util.ArrayList;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 20, 2017 4:37:34 PM
 */
public class AppLauncherCore<A extends AppCore> {
    
    private final AtomicBoolean busy = new AtomicBoolean();

    private static final Logger logger = Logger.getLogger(AppLauncherCore.class.getName());
    
    private final String defaultCharsetName = "utf-8";
    private boolean productionMode;
    private boolean enableSync; 
    private int maxTrials;
    private String appId;
    private ClassLoader classLoader;
    private Predicate<String> masterPersistenceUnitTest;
    private Predicate<String> slavePersistenceUnitTest;
    private List<String> parentWorkingDirPathList;
    private List<PropertiesContext> parentPropertiesContextList; 
    private PropertiesContext propertiesContext; 
    private String [] dirsToCreate;
    private String [] filesToCreate;
    private ProcessLog processLog;
    private Function<AppContext, A> createApp;

    private boolean launchAttempted;
    private boolean newInstallation;
    private String charsetName;
    private AppAuthenticationSession authenticationSession;
    
    public AppLauncherCore() {
        this.enableSync = true;
        this.maxTrials = 3;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.masterPersistenceUnitTest = new MasterPersistenceUnitTest();
        this.slavePersistenceUnitTest = new SlavePersistenceUnitTest();
        this.processLog = new ProcessLogImpl(this.getClass(), Level.INFO);
    }
    
    public AppLauncherCore productionMode(boolean productionMode) {
        this.productionMode = productionMode;
        return this;
    }
    public AppLauncherCore enableSync(boolean enableSync) {
        this.enableSync = enableSync;
        return this;
    }
    public AppLauncherCore maxTrials(int maxTrials) {
        this.maxTrials = maxTrials;
        return this;
    }
    public AppLauncherCore classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
    public AppLauncherCore appId(String appId) {
        this.appId = appId;
        return this;
    }
    public AppLauncherCore parentWorkingDirPaths(String ...parentWorkingDirPaths) {
        this.parentWorkingDirPaths(Arrays.asList(parentWorkingDirPaths));
        return this;
    }
    public AppLauncherCore parentWorkingDirPaths(List<String> parentWorkingDirPaths) {
        this.parentWorkingDirPathList = parentWorkingDirPaths;
        return this;
    }
    public AppLauncherCore parentPropertiesPaths(PropertiesContext ...propertiesPaths) {
        return this.parentPropertiesPaths(Arrays.asList(propertiesPaths));
    }
    public AppLauncherCore parentPropertiesPaths(List<PropertiesContext> propertiesPaths) {
        this.parentPropertiesContextList = propertiesPaths;
        return this;
    }
    public AppLauncherCore workingDirPath(String workingDirPath) {
        final PropertiesContext pc = PropertiesContext.builder()
                .workingDirPath(workingDirPath).build();
        return this.propertiesContext(pc);
    }
    public AppLauncherCore propertiesContext(PropertiesContext propsCtx) {
        this.propertiesContext = propsCtx;
        return this;
    }
    public AppLauncherCore masterPersistenceUnitTest(Predicate<String> test) {
        this.masterPersistenceUnitTest = test;
        return this;
    }
    public AppLauncherCore slavePersistenceUnitTest(Predicate<String> test) {
        this.slavePersistenceUnitTest = test;
        return this;
    }
    public AppLauncherCore processLog(ProcessLog processLog) {
        this.processLog = processLog;
        return this;
    }
    public AppLauncherCore createAppFromContext(Function<AppContext, A> createApp) {
        this.createApp = createApp;
        return this;
    }
    public AppLauncherCore dirsToCreate(String [] dirs) {
        this.dirsToCreate = dirs;
        return this;
    }
    public AppLauncherCore filesToCreate(String [] files) {
        this.filesToCreate = files;
        return this;
    }
    
    protected void onLaunchBegan() { }
    
    protected void onInstallationCompleted(A app) { }
    
    protected void onLaunchCompleted(A app) { }
    
    protected void onShutdown(A app) { }
    
    protected void onStartupException(Throwable t) {
        onStartupException(t, "Failed to start application", 0);
    }
    
    protected void onStartupException(Throwable t, String description, int exitCode) {
        logger.log(Level.SEVERE, description, t);
        System.exit(exitCode);
    }
    
    public final A launch(String [] args) {

        try{
            
            if(launchAttempted) {
                throw new IllegalStateException("launch(String[]) method may only be called once");
            }
            this.launchAttempted = true;
           
            if(busy.getAndSet(true)) {
                throw new IllegalStateException();
            }

            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                logger.log(Level.WARNING, "Uncaught exception in thread: " + t.getName(), e);
            });
            
            processLog.init();
            
            this.onLaunchBegan();
            
            Objects.requireNonNull(this.appId);
            Objects.requireNonNull(this.classLoader);
            Objects.requireNonNull(this.createApp);
            Objects.requireNonNull(this.masterPersistenceUnitTest);
            Objects.requireNonNull(this.processLog);
            Objects.requireNonNull(this.slavePersistenceUnitTest);
            
            processLog.log("Initializing folders");

            if(this.parentPropertiesContextList == null && this.parentWorkingDirPathList != null) {
                this.parentPropertiesPaths(productionMode, this.parentWorkingDirPathList);
            }
            
            if(appId != null) {
                if(this.parentPropertiesContextList == null) {
                    this.parentPropertiesPathsFromAppIds(productionMode, this.appId);
                }

                if(this.propertiesContext == null) {
                    this.workingPropertiesPathsFromAppId(appId);
                }
            }
            Objects.requireNonNull(this.parentPropertiesContextList);
            Objects.requireNonNull(this.propertiesContext);
            
            final String workingDirPath = propertiesContext.getWorkingDirPath();
            
            if(dirsToCreate == null) {
                dirsToCreate = new String[]{
                        Paths.get(workingDirPath, Names.CONFIGS_DIR).toString(),
                        Paths.get(workingDirPath, Names.LOGS_DIR).toString(),
                        Paths.get(workingDirPath, Names.PENDING_UPDATES_DIR).toString(),
                        Paths.get(workingDirPath, Names.REPORT_BACKUP_DIR).toString()
                };
            }
            logger.fine(() -> "Dirs to create: " + (Arrays.toString(dirsToCreate).replace(',', '\n')));
            for(String toCreate : dirsToCreate) {
                this.initDir(new File(toCreate)); 
            }
            
            if(filesToCreate == null) {
                filesToCreate = new String[] {
                        propertiesContext.getApp().toString(),
                        propertiesContext.getAuthsvc().toString(),
                        propertiesContext.getSettings().toString(),
                        propertiesContext.getLogging().toString()
                };
            }
            logger.fine(() -> "Files to create: " + (Arrays.toString(filesToCreate).replace(',', '\n')));
            for(String toCreate : filesToCreate) {
                this.initFile(new File(toCreate));
            }
            
            this.newInstallation = !this.isInstalled();
            
            processLog.log("Loading configurations");
            
            final Properties appProperties = this.loadProperties(PropertiesContext.TypeName.APP);

            final Config config = new ConfigImpl(appProperties, "EEE MMM dd HH:mm:ss z yyyy");

            this.charsetName = this.getCharsetName(config);
            
            if(this.newInstallation || !this.productionMode) {
                final Set<Path> parentLogConfigPaths = this.getPropertiesPaths(this.parentPropertiesContextList, PropertiesContext.TypeName.LOGGING);
                final Copy copy = new Copy();
                final Path loggingConfigPath = this.propertiesContext.getLogging();
                int i = 0;
                for(Path parentPath : parentLogConfigPaths) {
                    
                    copy.copy(parentPath, loggingConfigPath, charsetName, i++ != 0);
                }
            }
            
            new LoggingConfigManagerImpl(charsetName).read(this.propertiesContext.getLogging().toString());

            processLog.log("Initializing database");

            final JpaContextManager jpaMgr = this.getJpaContextManager();
            
            final String persistencePath = this.getPersistenceFile(config);
            
            final URL url = new ResourceLoader(this.classLoader).get(persistencePath, null);
            final URI persistenceURI = url == null ? null : url.toURI();
            
            logger.info(() -> "Persistence config path: " + persistencePath + ", URI: " + persistenceURI);
            
            JpaContext jpaContext = jpaMgr.createJpaContext(persistenceURI, maxTrials, newInstallation);
            
            final PendingUpdatesManager pendingMasterUpdatesMgr = this.createPendingMasterUpdatesManager(jpaContext);
            
            final PendingUpdatesManager pendingSlaveUpdatesMgr = this.createPendingSlaveUpdatesManager(jpaContext);

            final JpaSync jpaSync = !enableSync ? JpaSync.NO_OP :
                    new JpaSyncImpl(jpaContext, 
                            new UpdaterImpl(jpaContext, this.masterPersistenceUnitTest, this.slavePersistenceUnitTest), 
                            20, 
                            new PersistenceCommunicationsLinkFailureTest());
            
            jpaContext = jpaMgr.configureJpaContext(jpaContext, pendingMasterUpdatesMgr);

            processLog.log("Initializing application context");
            
            final AppAuthenticationSession authSession = this.createAuthSession();

            final ExpirableCache expirableCache = new ExpirableCacheImpl(10, TimeUnit.MINUTES);

            final Properties settingsMeta = this.loadSettingsProperties();
            
            logger.info(() -> "Settings: " + settingsMeta);
            
            final AppContextBuilder appContextBuilder = AppContext.builder();
            
            if(authSession != null) {
                appContextBuilder.authenticationSession(authSession);
            }
            
            final AppContext appContext = (AppContext)appContextBuilder
                    .classLoader(classLoader)
                    .config(config)
                    .expirableCache(expirableCache)
                    .propertiesPaths(propertiesContext)
                    .jpaContext(jpaContext)
                    .jpaSync(jpaSync)
                    .settingsConfig(settingsMeta)
                    .pendingMasterUpdatesManager(pendingMasterUpdatesMgr)
                    .pendingSlaveUpdatesManager(pendingSlaveUpdatesMgr)
                    .build();
            
            final A app = this.createApp.apply(appContext);

            app.init();

            setInstalled(true);

            final boolean installed = this.isInstalled();
            
            logger.info(() -> "Was installed: "+!this.newInstallation+", now installed: " + installed);
            
            if(!installed) {
                throw new IllegalStateException("Failed to create installation file");
            }

            processLog.log(!this.newInstallation ? "App Launch Successful" : "Installation Successful");

            if(this.newInstallation) {

                this.onInstallationCompleted(app);
            }

            addShutdownHook(app);

            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try{
                        
                        initUI(app);
                        
                        callOnLaunchCompletedInSeparateThread(app);

                    }catch(Exception e) {
                        
                        processLog.log("Error");
                        processLog.log(e);
                        
                        onStartupException(e);
                        
                    }finally{
                        
                        processLog.destroy();
                        
                        setBusy(false);
                    }
                }
            });
            
            return app;
            
        }catch(Exception e) {
            
            processLog.log("Error");
            processLog.log(e);

            onStartupException(e);
            
            return null;
        }
    }
    
    public boolean isInstalled() {
        return this.getInstallationFile().exists();
    }
    
    public void setInstalled(boolean installed) throws IOException {
        final File file = this.getInstallationFile();
        if(installed) {
            if(!file.exists()) {
                file.createNewFile();
            }
        }else{
            if(file.exists()) {
                if(!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }
    
    public File getInstallationFile() {
        final Path path = Paths.get(this.propertiesContext.getWorkingDirPath(), this.appId + ".installation"); 
        return path.toFile();
    }
    
    public String getCharsetName(Config config) {
        return config.getString("charsetName", defaultCharsetName);
    }
    
    public String getLookAndFeel(Config config, String outputIfNone) {
        return config.getString("lookAndFeel", outputIfNone);
    }
    
    public String getPersistenceFile(Config config) {
        return config.getString("persistenceFile");
    }
    
    protected AppLauncherCore parentPropertiesPathsFromAppIds(boolean productionMode, String ...appIds) {
        final String [] defaultWorkingDirPaths = new String[appIds.length];
        for(int i=0; i<appIds.length; i++) {
            defaultWorkingDirPaths[i] = this.getAppMetaInfWorkingDirPath(appIds[i]);
        }
        return this.parentPropertiesPaths(productionMode, defaultWorkingDirPaths);
    }
    public String getAppMetaInfWorkingDirPath(String fname) {
        final String metaInf = "META-INF/" + fname;
        final String uriStr = classLoader.getResource(metaInf).toExternalForm();
        logger.fine(() -> "META-INF working dir: " + uriStr);
        return uriStr;
    }
    protected AppLauncherCore parentPropertiesPaths(boolean productionMode, String ...parentWorkingDirPaths) {
        return this.parentPropertiesPaths(productionMode, Arrays.asList(parentWorkingDirPaths));
    }
    protected AppLauncherCore parentPropertiesPaths(boolean productionMode, List<String> parentWorkingDirPaths) {
        final Function<String, PropertiesContext> create = (workingDirPath) ->
                productionMode ? 
                PropertiesContext.builder().workingDirPath(workingDirPath).build() : 
                PropertiesContext.builder().workingDirPath(workingDirPath).suffix("devmode").build();
        final List<PropertiesContext> list = 
                parentWorkingDirPaths.stream().map(create).collect(Collectors.toList());
        return this.parentPropertiesPaths(list);
    }
    protected AppLauncherCore workingPropertiesPathsFromAppId(String appId) {
        this.appId = appId;
        final String workingDir = this.getUserHomeWorkingDirPath(appId);
        return this.workingDirPath(workingDir);
    }
    public String getUserHomeWorkingDirPath(String fname) {
        final String path = Paths.get(System.getProperty("user.home"), fname).toString();
        logger.fine(() -> "User.home working dir: " + path);
        return path;
    }
    
    public JpaContextManager getJpaContextManager() {
        return new JpaContextManagerImpl(this.masterPersistenceUnitTest);
    }
    
    /**
     * <b>This method is called on the AWT Event Queue</b>
     * Initialize the app's User Interface (UI).
     * @param app The app whose UI will be initialized
     */
    protected void initUI(A app) { }
    
    protected PendingUpdatesManager createPendingMasterUpdatesManager(JpaContext jpaContext) {
        return PendingUpdatesManager.NO_OP;
    }
    
    protected PendingUpdatesManager createPendingSlaveUpdatesManager(JpaContext jpaContext) {
        return !enableSync ? PendingUpdatesManager.NO_OP :
                new PendingUpdatesManagerImpl(
                        this.getPendingUpdatesFilePath(Names.PENDING_SLAVE_UPDATES_FILE_NAME).toFile(),
                        new UpdaterImpl(jpaContext, this.masterPersistenceUnitTest, this.slavePersistenceUnitTest),
                        new PersistenceCommunicationsLinkFailureTest());
    }
    
    protected Properties getDefaultAuthProperties() {
        Objects.requireNonNull(this.appId);
        final Properties defaultValues = new Properties();
        final String packageName = this.getClass().getPackage().getName();
        defaultValues.putIfAbsent(AuthSvcProperties.SVC_ENDPOINT, "http://www.looseboxes.com/authsvc");
        defaultValues.putIfAbsent(AuthSvcProperties.APP_DETAILS_FILENAME, packageName + '.' + this.appId + ".app.details");
        defaultValues.putIfAbsent(AuthSvcProperties.APP_TOKEN_FILENAME, packageName + '.' + this.appId + ".app.token");
        return defaultValues;
    }
    
    protected AppAuthenticationSession createAuthSession() 
            throws IOException, ParseException {
        
        AppAuthenticationSession authSession = null;
        int trials = 0;
        while(trials++ < maxTrials) {
            try{
                
                final Properties props = this.loadAuthProperties();

                logger.info(() -> "Auth properties: " + (props == null ? null : props.stringPropertyNames()));

                if(props == null || props.stringPropertyNames().isEmpty()) {
                    continue;
                }
                
                authSession = this.createAuthSession(props);
                
                break;
                
            }catch(AuthenticationException e) {
                logger.warning(e.toString());
            }
        }
        final boolean created = authSession != null;
        logger.info(() -> "Created authentication session: " + created);
        return authSession;
    }
    
    protected AppAuthenticationSession createAuthSession(Properties props) 
            throws IOException, ParseException, AuthenticationException {
        
        if(authenticationSession != null) {
            throw new IllegalStateException("Authentication Session Already exists");
        }
        
        try{
            
            authenticationSession = new AppAuthenticationSessionImpl(
                    props.getProperty(AuthSvcProperties.SVC_ENDPOINT), 
                    this.getPropertiesContext().getDirPath(),
                    props.getProperty(AuthSvcProperties.APP_TOKEN_FILENAME), 
                    props.getProperty(AuthSvcProperties.APP_DETAILS_FILENAME)
            );

            final String app_name = props.getProperty(AuthSvcProperties.APP_NAME);
            final String app_email = props.getProperty(AuthSvcProperties.APP_EMAIL);
            final String app_pass = props.getProperty(AuthSvcProperties.APP_PASS);

            authenticationSession.init(app_name, app_email, app_pass, true);
            
        }catch(RuntimeException | IOException | ParseException | AuthenticationException e) {
            authenticationSession = null;
            throw e;
        }
        
        return authenticationSession;
    }
    
    protected Path getPendingUpdatesFilePath(String fname) {
        return Paths.get(this.propertiesContext.getWorkingDirPath(), Names.PENDING_UPDATES_DIR, fname);
    }
    
    protected Set<Path> getPropertiesPaths(List<PropertiesContext> pathList, String typeName) {
        final Function<PropertiesContext, Path> mapper = (ppl) -> ppl.get(typeName);
        return this.mapPropertiesPaths(pathList, mapper);
    }
    
    protected Set<Path> mapPropertiesPaths(List<PropertiesContext> pathList, Function<PropertiesContext, Path> mapper) {
        return new LinkedHashSet(pathList.stream().map(mapper).collect(Collectors.toList()));
    }
    
    protected void initDirs(Collection<File> dirs) {
        for(File dir : dirs) {
            this.initDir(dir);
        }
    }
    
    protected void initDir(File dir) {
        if(!dir.exists()) {
            logger.info(() -> "Creating dir: " + dir);
            dir.mkdirs();
        }
    }
    
    protected void initFiles(List<File> files) throws IOException{
        for(File file : files) {
            this.initFile(file);
        }
    }
    
    protected void initFile(File file) throws IOException {
        final File parent = file.getParentFile();
        this.initDir(parent);
        if(!file.exists()) {
            logger.info(() -> "Creating file: " + file);
            file.createNewFile();
        }
    }
    
    protected Properties loadAuthProperties() throws IOException {
        return this.loadProperties(PropertiesContext.TypeName.AUTHSVC);
    }
    
    protected Properties loadSettingsProperties() throws IOException {
        return this.loadProperties(PropertiesContext.TypeName.SETTINGS);
    }
    
    protected Properties loadProperties(String typeName) 
            throws IOException {
        
        final List<PropertiesContext> list = new ArrayList(this.parentPropertiesContextList.size() + 1);
        list.addAll(this.parentPropertiesContextList);
        list.add(this.propertiesContext);
        
        return this.combineProperties(list, typeName, charsetName==null?this.defaultCharsetName:charsetName);
    }
    
    public Properties combineProperties(List<PropertiesContext> propsCtxList, 
            String typeName, String charsetName) throws IOException {
        
        final Properties combinedProperties = new Properties();

        for(PropertiesContext propsCtx : propsCtxList) {
            
            try{
                
                final List<InputStream> list = propsCtx.getInputStreams(typeName);
                
                for(InputStream in : list) {
                
                    try{
                        
                        try(Reader reader = new BufferedReader(new InputStreamReader(in, charsetName))) {

                            final Properties props = new Properties();

                            props.load(reader);

                            logger.fine(() -> "Loaded properties from: " + 
                            propsCtx.get(typeName) + "\n" + props.stringPropertyNames());
                            
                            for(String name : props.stringPropertyNames()) {
                                combinedProperties.setProperty(name, props.getProperty(name));
                            }
                        }
                    }catch(IOException e) {
                        logger.log(Level.WARNING, "{0}", e.toString());
                    }
                }
            }catch(IOException e) {
                logger.log(Level.WARNING, "{0}", e.toString());
            }
        }
        
        return combinedProperties;
    }
    
    protected void addShutdownHook(final A app) {
        Runtime.getRuntime().addShutdownHook(new Thread("App_ShutdownHook_Thread") {
            @Override
            public void run() {
                try{
                    if(!app.isShutdown()) {
                        app.shutdown();
                    }

                    AppLauncherCore.this.onShutdown(app);

                }catch(RuntimeException e) {
                    logger.log(Level.WARNING, "Error running shut down hook: "+Thread.currentThread().getName(), e);
                }
            }
        });
    }
    
    private void callOnLaunchCompletedInSeparateThread(A app) {
        new Thread(this.getClass().getName()+"#onLaunchCompleted_WorkerThread"){
            @Override
            public void run() {
                try{

                    AppLauncherCore.this.onLaunchCompleted(app);

                }catch(RuntimeException e) {
                    logger.log(Level.WARNING, "Exception executing method " + 
                            this.getClass().getName()+"#onLaunchCompleted() in thread " +
                            Thread.currentThread().getName(), e);
                }
            }
        }.start();
    }
    
    protected synchronized void setBusy(boolean b) {
        busy.set(b);
    }

    public synchronized boolean isBusy() {
        return busy.get();
    }
    
    public synchronized void waitTillCompletion() throws InterruptedException{
        try{
            while(isBusy()) {
                this.wait(1000);
            }
        }finally{
            this.notifyAll();
        }
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public boolean isEnableSync() {
        return enableSync;
    }

    public int getMaxTrials() {
        return maxTrials;
    }

    public AppAuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    public String getAppId() {
        return appId;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Predicate<String> getMasterPersistenceUnitTest() {
        return masterPersistenceUnitTest;
    }

    public Predicate<String> getSlavePersistenceUnitTest() {
        return slavePersistenceUnitTest;
    }

    public List<String> getParentWorkingDirPathList() {
        return parentWorkingDirPathList;
    }
    
    public List<PropertiesContext> getParentPropertiesContextList() {
        return parentPropertiesContextList;
    }

    public PropertiesContext getPropertiesContext() {
        return propertiesContext;
    }

    public String[] getDirsToCreate() {
        return dirsToCreate;
    }

    public String[] getFilesToCreate() {
        return filesToCreate;
    }

    public ProcessLog getProcessLog() {
        return processLog;
    }

    public Function<AppContext, A> getCreateApp() {
        return createApp;
    }

    public boolean isNewInstallation() {
        return newInstallation;
    }
}
