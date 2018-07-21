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

import com.bc.appcore.jpa.PersistenceContextManagerImpl;
import com.authsvc.client.AppAuthenticationSession;
import com.authsvc.client.AppAuthenticationSessionImpl;
import com.authsvc.client.AuthenticationException;
import com.authsvc.client.AuthenticationSession;
import com.bc.appcore.properties.AuthSvcProperties;
import com.bc.appcore.util.Copy;
import com.bc.appcore.util.LoggingConfigManagerImpl;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.bc.appcore.properties.PropertiesContext;
import com.bc.config.ConfigImpl;
import com.bc.jpa.context.PersistenceContext;
import java.util.ArrayList;
import com.bc.appcore.jpa.PersistenceContextManager;
import com.bc.appcore.jpa.nodequery.RenameUppercaseSlaveColumnsThenTables;
import com.bc.appcore.properties.PropertiesContextBuilder;
import com.bc.config.Config;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.predicates.DatabaseCommunicationsFailureTest;
import com.bc.jpa.sync.MasterSlaveSwitch;
import com.bc.util.JsonFormat;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 20, 2017 4:37:34 PM
 */
public class AppLauncherCore<A extends AppCore> {
    
    private final AtomicBoolean busy = new AtomicBoolean();

    private static final Logger LOG = Logger.getLogger(AppLauncherCore.class.getName());
    
    private final String defaultCharsetName = StandardCharsets.UTF_8.name();
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
    private Function<AppContext, Optional<A>> createApp;

    private boolean launchAttempted;
    private boolean newInstallation;
    private String charsetName;
    private AppAuthenticationSession authenticationSession;
    
    private AppContextBuilder appContextBuilder;
    
    public AppLauncherCore() {
        this.enableSync = true;
        this.maxTrials = 3;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.masterPersistenceUnitTest = (name) -> true;
        this.slavePersistenceUnitTest = this.masterPersistenceUnitTest.negate();
        this.processLog = new ProcessLogImpl(LOG, Level.INFO);
        this.appContextBuilder = new AppContextBuilder();
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
    public AppLauncherCore appContextBuilder(AppContextBuilder appCtxBuilder) {
        this.appContextBuilder = appCtxBuilder;
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
    public AppLauncherCore init(
            boolean productionMode, 
            String appId, 
            List<String> propertyTypesWithDevmodeSuffixes,
            String... externalResources) {
        
        return this.init(productionMode, 
                Thread.currentThread().getContextClassLoader(), appId, 
                propertyTypesWithDevmodeSuffixes, externalResources);
    }
    public AppLauncherCore init(
            boolean productionMode, ClassLoader classLoader, 
            String appId, 
            List<String> propertyTypesWithDevmodeSuffixes, 
            String... externalResources) {
        
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(appId);
        Objects.requireNonNull(externalResources);
        
        this.productionMode(productionMode);
        this.appId(appId);
        this.classLoader(classLoader);
        
        final String internalResource = "META-INF/"+appId;
        
        final List<PropertiesContext> propsCtxList = new ArrayList();
        
        for(String externalResource : externalResources) {
            final PropertiesContext externalPropsCtx = PropertiesContext.builder()
                    .workingDirPath(externalResource)
                    .build();
            propsCtxList.add(externalPropsCtx);
        }
        
        final PropertiesContextBuilder propsCtxBuilder = PropertiesContext.builder();

        propsCtxBuilder
                .workingDirPath(internalResource)
                .classLoader(classLoader);
        
        for(String typeName : propertyTypesWithDevmodeSuffixes) {
            
            propsCtxBuilder.typeSuffix(typeName, productionMode ? null : "devmode");
        }
        
        final PropertiesContext internalPropsCtx = propsCtxBuilder.build();
        
        propsCtxList.add(internalPropsCtx);
        
        return this.parentPropertiesPaths(propsCtxList);
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
    public AppLauncherCore createAppFromContext(Function<AppContext, Optional<A>> createApp) {
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
    
    protected void onAppContextCreated(AppContext appContext) {}
    
    protected void onAppCreated(A app) {}
    
    protected void onAppInitialized(A app) { }
    
    protected void onInstallationCompleted(A app) { }
    
    protected void onLaunchCompleted(A app) { }
    
    protected void onShutdown(A app) { }
    
    protected void onStartupException(Throwable t) {
        onStartupException(t, "Failed to start application", 0);
    }
    
    protected void onStartupException(Throwable t, String description, int exitCode) {
        try{
            LOG.log(Level.SEVERE, description, t);
        }finally{
            System.exit(exitCode);
        }
    }
    
    public final Optional<A> launch(String [] args) {
        
        LOG.entering(this.getClass().getName(), "launch(String[])");
        try{
            
            if(launchAttempted) {
                throw new IllegalStateException("launch(String[]) method may only be called once");
            }
            this.launchAttempted = true;
           
            if(busy.getAndSet(true)) {
                throw new IllegalStateException();
            }

            Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                LOG.log(Level.WARNING, "Uncaught exception in thread: " + t.getName(), e);
            });
            
            processLog.init();
            
            this.onLaunchBegan();
            
            Objects.requireNonNull(this.appId);
            Objects.requireNonNull(this.classLoader);
            Objects.requireNonNull(this.createApp);
            Objects.requireNonNull(this.masterPersistenceUnitTest);
            Objects.requireNonNull(this.processLog);
            Objects.requireNonNull(this.slavePersistenceUnitTest);
            Objects.requireNonNull(this.appContextBuilder);
            
            processLog.log("Initializing properties");

            if(this.parentPropertiesContextList == null && this.parentWorkingDirPathList != null) {
                this.parentPropertiesContexts(productionMode, this.parentWorkingDirPathList);
            }
            
            if(appId != null) {
                if(this.parentPropertiesContextList == null) {
                    this.parentPropertiesPathsFromAppIds(productionMode, this.appId);
                }

                if(this.propertiesContext == null) {
                    this.workingPropertiesContextFromAppId(appId);
                }
            }
            Objects.requireNonNull(this.parentPropertiesContextList);
            Objects.requireNonNull(this.propertiesContext);
            
            final String workingDirPath = propertiesContext.getWorkingDirPath();
            LOG.fine(() -> "Working dir path: " + workingDirPath);
            
            if(dirsToCreate == null) {
                dirsToCreate = new String[]{
                        Paths.get(workingDirPath, Names.CONFIGS_DIR).toString(),
                        Paths.get(workingDirPath, Names.LOGS_DIR).toString(),
                        Paths.get(workingDirPath, Names.PENDING_UPDATES_DIR).toString(),
                        Paths.get(workingDirPath, Names.REPORT_BACKUP_DIR).toString()
                };
            }
            if(filesToCreate == null) {
                filesToCreate = new String[] {
                        propertiesContext.getApp().toString(),
                        propertiesContext.getAuthsvc().toString(),
                        propertiesContext.getSettings().toString(),
                        propertiesContext.getLogging().toString()
                };
            }
            if(dirsToCreate.length > 0 || filesToCreate.length > 0) {
                processLog.log("Creating folders and files");
            }

            LOG.fine(() -> "Dirs to create: " + (Arrays.toString(dirsToCreate).replace(',', '\n')));
            for(String toCreate : dirsToCreate) {
                this.initDir(new File(toCreate)); 
            }
            
            LOG.fine(() -> "Files to create: " + (Arrays.toString(filesToCreate).replace(',', '\n')));
            for(String toCreate : filesToCreate) {
                this.initFile(new File(toCreate));
            }
            
//            if(this.isEnableSync()) {
                System.setProperty("derby.system.home", Paths.get(workingDirPath, "derby_db").toString());            
//            }
            
            this.newInstallation = !this.isInstalled();
            
            processLog.log("Loading configurations");
            
            final List<PropertiesContext> propsCtxList = new ArrayList(this.parentPropertiesContextList);

            final Properties appProperties = this.loadAppProperties(propsCtxList);
            
            final Config<Properties> config = new ConfigImpl(appProperties, "EEE MMM dd HH:mm:ss z yyyy");

            this.charsetName = this.getCharsetName(config);
            
            if(!this.isProductionMode()) {
                
            }

            this.loadLoggingProperties(propsCtxList);

            new LoggingConfigManagerImpl(charsetName).read(this.propertiesContext.getLogging().toString());

            processLog.log("Initializing database");

            final PersistenceContextManager persistenceCtxMgr = this.getPersistenceContextManager();

            final URI persistenceURI = this.getPersistenceUri(config);
            
            final PersistenceContext persistenceContext = persistenceCtxMgr.create(
                    persistenceURI, maxTrials, newInstallation);
            
            processLog.log("Initializing application context");
            
            final Optional<AppAuthenticationSession> optAuthSession = this.createAuthSession(propsCtxList);

            final Properties settingsMeta = this.loadSettingsProperties(propsCtxList);
            
            LOG.fine(() -> "Settings: " + settingsMeta);
            
            optAuthSession.ifPresent((sess) -> {
                appContextBuilder.authenticationSession(sess);
            });
            
            final AppContext appContext = appContextBuilder
                    .classLoader(classLoader)
                    .masterPersistenceUnitTest(masterPersistenceUnitTest)
                    .slavePersistenceUnitTest(slavePersistenceUnitTest)
                    .config(config)
//                    .expirableCache(expirableCache)
                    .propertiesContext(propertiesContext)
                    .persistenceContext(persistenceContext)
                    .syncEnabled(enableSync)
                    .settingsConfig(settingsMeta)
                    .build();
            
            this.onAppContextCreated(appContext);

            if(this.isNewInstallation()) {
                
                final MasterSlaveSwitch<PersistenceUnitContext> masterSlaveSwitch = 
                        appContext.getPersistenceContextSwitch();

                final Optional<PersistenceUnitContext> optSlavePuCtx = masterSlaveSwitch.getSlaveOptional();
                if(optSlavePuCtx.isPresent()) {
                    try{
                        new RenameUppercaseSlaveColumnsThenTables().execute(
                                masterSlaveSwitch.getMaster(), optSlavePuCtx.get()
                        );        
                    }catch(Throwable e) {
                        if(new DatabaseCommunicationsFailureTest().test(e)) {
                            LOG.warning(e.toString());
                        }else{
                            throw e;
                        }
                    }
                }
            }
            
            final Optional<A> optionalApp = this.createApp.apply(appContext);

            final A app = optionalApp.orElseThrow(() -> 
                    new NullPointerException("Failed to create instance of App from AppContext")
            );
            
            this.onAppCreated(app);
            
            app.init();

            setInstalled(true);

            this.onAppInitialized(app);

            final boolean installed = this.isInstalled();
           
            LOG.info(() -> "Was installed: "+!this.newInstallation+", now installed: " + installed);
            
            if(!installed) {
                throw new IllegalStateException("Failed to create installation file");
            }

            processLog.log(!this.newInstallation ? "App Launch Successful" : "Installation Successful");

            if(this.newInstallation) {

                this.onInstallationCompleted(app);
            }

            addShutdownHook(app);
            
            java.awt.EventQueue.invokeLater(() -> {
                try{
                    
                    initUI(app);
                    
                }catch(Exception e) {
                    
                    handleStartupException("Statup Exception. While initializing User Interface", e);
                    
                }finally{
                    finishStartupProcess();
                }
            });
            
            this.waitTillCompletion();
            
            this.onLaunchCompleted(app);
            
            return Optional.of(app);
            
        }catch(Exception e) {

            this.handleStartupException("Startup Exception", e);

            return Optional.empty();
            
        }finally{
            this.finishStartupProcess();
        }
    }
    
    private void handleStartupException(String msg, Exception e) {
        try{

            LOG.log(Level.WARNING, msg, e);

            processLog.log(e);

            onStartupException(e);

        }catch(Exception inner) {

            LOG.log(Level.WARNING, "Unexpected Exception", inner);
        }
    }
    
    private void finishStartupProcess() {
        try{
            processLog.destroy();
        }finally{
            busy.set(false);
        }
    }
    
    public boolean isInstalled() {
        return this.getInstallationFile().exists();
    }
    
    protected void setInstalled(boolean installed) throws IOException {
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
    
    public URI getPersistenceUri(Config config) 
            throws IOException, URISyntaxException {
        
        final String persistencePath = this.getPersistenceFile(config);

        final URI persistenceURI;
        if((Paths.get(persistencePath).toFile()).exists()) {
            persistenceURI = Paths.get(persistencePath).toUri();
        }else{
            final URL url = new ResourceLoader(this.classLoader).get(persistencePath, null);
            persistenceURI = url == null ? null : url.toURI();
        }
        
        LOG.info(() -> "Persistence config path: " + persistencePath + ", URI: " + persistenceURI);
        
        return persistenceURI;
    }
    
    public String getPersistenceFile(Config config) {
        final String output = config.getString("persistenceFile", "META-INF/persistence.xml");
        LOG.finer(() -> "Persistence file: " + output);
        return output;
    }
    
    protected AppLauncherCore parentPropertiesPathsFromAppIds(boolean productionMode, String ...appIds) {
        final String [] defaultWorkingDirPaths = new String[appIds.length];
        for(int i=0; i<appIds.length; i++) {
            defaultWorkingDirPaths[i] = this.getAppMetaInfWorkingDirPath(appIds[i]);
        }
        return this.parentPropertiesContexts(productionMode, defaultWorkingDirPaths);
    }
    public String getAppMetaInfWorkingDirPath(String fname) {
        final String metaInf = "META-INF/" + fname;
//        try{
            LOG.fine(() -> "META-INF working dir: " + metaInf);
            return metaInf; // This worked for desktop client: BuzzwearsProductUploader
//        }catch(URISyntaxException e) { 
//            throw new RuntimeException(e);
//        }
    }
    protected AppLauncherCore parentPropertiesContexts(boolean productionMode, String ...parentWorkingDirPaths) {
        return this.parentPropertiesContexts(productionMode, Arrays.asList(parentWorkingDirPaths));
    }
    protected AppLauncherCore parentPropertiesContexts(boolean productionMode, List<String> parentWorkingDirPaths) {
        final Function<String, PropertiesContext> create = (workingDirPath) ->
                productionMode ? 
                PropertiesContext.builder().workingDirPath(workingDirPath).build() : 
                PropertiesContext.builder().workingDirPath(workingDirPath).suffix("devmode").build();
        final List<PropertiesContext> list = 
                parentWorkingDirPaths.stream().map(create).collect(Collectors.toList());
        return this.parentPropertiesPaths(list);
    }
    protected AppLauncherCore workingPropertiesContextFromAppId(String appId) {
        this.appId = appId;
        final String workingDir = this.getUserHomeWorkingDirPath(appId);
        return this.workingDirPath(workingDir);
    }
    public String getUserHomeWorkingDirPath(String fname) {
        final String path = Paths.get(System.getProperty("user.home"), fname).toString();
        LOG.fine(() -> "User.home working dir: " + path);
        return path;
    }
    
    public PersistenceContextManager getPersistenceContextManager() {
        return new PersistenceContextManagerImpl();
    }
    
    /**
     * <b>This method is called on the AWT Event Queue</b>
     * Initialize the app's User Interface (UI).
     * @param app The app whose UI will be initialized
     */
    protected void initUI(A app) { }
    
    protected Optional<AppAuthenticationSession> createAuthSession(List<PropertiesContext> propsCtxList) 
            throws IOException, ParseException {
        
        AppAuthenticationSession authSession = null;
        int trials = 0;
        while(trials++ < maxTrials) {
            try{

                final Properties props = this.loadAuthProperties(propsCtxList);

                LOG.info(() -> "Auth properties: " + (props == null ? null : props.stringPropertyNames()));

                if(props == null || props.stringPropertyNames().isEmpty()) {
                    continue;
                }
                
                authSession = this.createAuthSession(props);
                
                break;
                
            }catch(AuthenticationException e) {
                this.logShortWarning("Exception creating: instance of " + AuthenticationSession.class.getName(), e);
            }
        }
        final boolean created = authSession != null;
        
        LOG.info(() -> "Created authentication session: " + created);
        
        return Optional.ofNullable(authSession);
    }
    
    protected AppAuthenticationSession createAuthSession(Properties props) 
            throws IOException, ParseException, AuthenticationException {

        if(authenticationSession != null) {
            throw new IllegalStateException("Authentication Session Already exists");
        }
        
        try{
            
            authenticationSession = new AppAuthenticationSessionImpl(
                    Objects.requireNonNull(props.getProperty(AuthSvcProperties.SVC_ENDPOINT)), 
                    Objects.requireNonNull(this.getPropertiesContext().getDirPath()),
                    Objects.requireNonNull(props.getProperty(AuthSvcProperties.APP_TOKEN_FILENAME)), 
                    Objects.requireNonNull(props.getProperty(AuthSvcProperties.APP_DETAILS_FILENAME))
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
            LOG.info(() -> "Creating dir: " + dir);
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
            LOG.info(() -> "Creating file: " + file);
            file.createNewFile();
        }
    }
    /**
     * @param typeName
     * @throws IOException
     * @deprecated Rather use {@link #loadAndUpdateCombinedProperties(java.lang.String)}
     */
    @Deprecated
    protected void updatePropertiesFor(String typeName) throws IOException {
        final Set<Path> parentLogConfigPaths = this.getPropertiesPaths(
                this.parentPropertiesContextList, typeName);
        final Copy copy = new Copy();
        final Path loggingConfigPath = this.propertiesContext.getLogging();
        int i = 0;
        for(Path parentPath : parentLogConfigPaths) {
            LOG.info(() -> "Copying: "+parentPath+", to: "+loggingConfigPath);
            copy.copy(parentPath, loggingConfigPath, this.getCharsetName(), i++ != 0);
        }
    }

    protected Properties loadAppProperties(List<PropertiesContext> contextList) throws IOException {
        return this.load(contextList, PropertiesContext.TypeName.APP, this.defaultCharsetName);
    }
    
    protected Properties loadLoggingProperties(List<PropertiesContext> contextList) throws IOException {
        return this.load(contextList, PropertiesContext.TypeName.LOGGING);
    }

    protected Properties loadAuthProperties(List<PropertiesContext> contextList) throws IOException {
        return this.load(contextList, PropertiesContext.TypeName.AUTHSVC);
    }
    
    protected Properties loadSettingsProperties(List<PropertiesContext> contextList) throws IOException {
        return this.load(contextList, PropertiesContext.TypeName.SETTINGS);
    }
    
    protected Properties load(
            List<PropertiesContext> propsCtxList, String typeName) 
            throws IOException {
        return this.load(propsCtxList, typeName, this.getCharsetName());
    }
    
    protected Properties load(
            List<PropertiesContext> propsCtxList, String typeName, String charsetName) 
            throws IOException {
        final Properties output;
        if(this.newInstallation || !this.productionMode) {
            if(!this.productionMode) {
                LOG.log(Level.INFO, "Clearing properties at: {0}", this.propertiesContext.get(typeName));
                this.storeProperties(new Properties(), typeName);
            }
            output = this.loadAndUpdateCombinedProperties(
                propsCtxList, typeName);
        }else{
            output = this.loadProperties(
                    propertiesContext, typeName, charsetName);
        }
        return output;
    }

    protected Properties loadAndUpdateCombinedProperties(
            List<PropertiesContext> contextList, String typeName) throws IOException {
        final Properties properties = this.loadCombinedProperties(contextList, typeName);
        LOG.info(() -> "Storing combined "+typeName+" properties to: " + this.propertiesContext.get(typeName));
        LOG.fine(() -> "Combined "+typeName+" property names: " + properties.stringPropertyNames());
        this.storeProperties(properties, typeName);
        LOG.finer(() -> "Type: " + typeName + ", Properties:\n" + new JsonFormat(true, true, "  ").toJSONString(properties));
        return properties;
    }
    
    protected Properties storeProperties(Properties properties, String typeName) throws IOException {
        final Path path = this.propertiesContext.get(typeName);
        try(final Writer writer = new OutputStreamWriter(
                new FileOutputStream(path.toFile(), false), this.getCharsetName()
        )) {
            properties.store(writer, typeName + " properties saved by " + System.getProperty("user.name"));
        }
        return properties;
    }

    protected Properties loadCombinedProperties(List<PropertiesContext> contextList, String typeName) 
            throws IOException {
        
        return this.loadCombinedProperties(contextList, typeName, this.getCharsetName());
    }
    
    private String getCharsetName() {
        return charsetName==null?this.defaultCharsetName:charsetName;
    }
    
    protected List<PropertiesContext> combinePropertiesContexts() {
        final List<PropertiesContext> list = new ArrayList(this.parentPropertiesContextList.size() + 1);
        list.addAll(this.parentPropertiesContextList);
        list.add(this.propertiesContext);
        return list;
    } 
    
    protected Properties loadCombinedProperties(List<PropertiesContext> propsCtxList, 
            String typeName, String charsetName) throws IOException {
        
        final Properties combinedProperties = new Properties();

        for(PropertiesContext propsCtx : propsCtxList) {
            
            try{
                
                final Properties props = this.loadProperties(propsCtx, typeName, charsetName);

                final Set<String> stringNames = props.stringPropertyNames();

                for(String name : stringNames) {
                    final String existing = combinedProperties.getProperty(name);
                    final String update = props.getProperty(name);
                    if(!Objects.equals(existing, update)) {
                        combinedProperties.setProperty(name, update);
                        LOG.finest(() -> "Old: " + existing + ", new: " + update);
                    }
                }
            }catch(IOException e) {
                this.logShortWarning("Unexpected Exception", e);
            }
        }
        
        return combinedProperties;
    }

    protected Properties loadProperties(
            PropertiesContext propsCtx, String typeName, String charsetName) throws IOException {
       
        Properties output = null;

        final Path path = propsCtx.get(typeName);

        LOG.fine(() -> "Adding to combined, "+typeName+" properties from path: " + path);

        final List<InputStream> list = propsCtx.getInputStreams(typeName);

        for(InputStream in : list) {

            try{

                try(Reader reader = new BufferedReader(new InputStreamReader(in, charsetName))) {

                    if(output == null) {
                        output = new Properties();
                    }else{
                        output = new Properties(output);
                    }
                    output.load(reader);

                    final Set<String> stringNames = output.stringPropertyNames();

                    LOG.fine(() -> "Loaded "+typeName+" properties from: " + 
                    path + "\nProperty names: " + stringNames);
                }
            }catch(IOException e) {
                this.logShortWarning("Unexpected Exception", e);
            }
        }
        
        return output == null ? new Properties() : output;
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
                    LOG.log(Level.WARNING, "Error running shut down hook: "+Thread.currentThread().getName(), e);
                }
            }
        });
    }
    
    public synchronized void waitTillCompletion() throws InterruptedException{
        try{
            while(busy.get()) {
                this.wait(1000);
            }
        }finally{
            this.notifyAll();
        }
    }
    
    public void logShortWarning(String msg, Exception e) {
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.WARNING, msg, e);
        }else{
            LOG.warning(() -> msg + " caused by: " + e);
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

    public Function<AppContext, Optional<A>> getCreateApp() {
        return createApp;
    }

    public boolean isNewInstallation() {
        return newInstallation;
    }
}
