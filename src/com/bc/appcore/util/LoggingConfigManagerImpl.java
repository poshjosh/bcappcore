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

package com.bc.appcore.util;

import com.bc.appcore.ResourceContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 9, 2017 12:27:04 PM
 */
public class LoggingConfigManagerImpl implements LoggingConfigManager {

    private transient static final Logger logger = Logger.getLogger(
            LoggingConfigManagerImpl.class.getName());
    
    private final ResourceContext resourceContext;
    
    private final List<String> readOnly = Arrays.asList(new String[]{
        "java", "javax", "sun", "com.sun", "com.mysql", "org"
    });
    
    public LoggingConfigManagerImpl(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }
    
    @Override
    public void init(String source, String target, Level level) 
            throws URISyntaxException, IOException {
        
        final Path loggingConfigPath = resourceContext.getPath(target);
        
        logger.log(Level.INFO, "Copying: {0}, to: {1}", new Object[]{source, target});

        final StringBuilder contents = this.getContents(source);
        Files.write(loggingConfigPath, contents.toString().getBytes(), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING, 
                StandardOpenOption.WRITE);
        
        this.read(target, level);
    }
    
    @Override
    public void read(String resourcePath, Level level) throws URISyntaxException, IOException {
        
        if(level != null) {
            this.updateLevel(resourcePath, level);
        }
        
        final String loggingFilePropertyName = "java.util.logging.config.file";
        
        logger.log(Level.FINE, "{0} = {1}", new Object[]{loggingFilePropertyName, System.getProperty(loggingFilePropertyName)});
        
        final Path path = this.getResourcePath(resourcePath, null);
        System.setProperty(loggingFilePropertyName, path==null?resourcePath:path.toString());
        
        logger.log(Level.FINER, "Reading logging config file: {0}", resourcePath);
            
        try(InputStream in = this.getInputStream(resourcePath)) {
            
            logger.log(Level.FINER, "Input stream: {0}", in);
            
            LogManager.getLogManager().readConfiguration(in);
        }
        
        logger.log(Level.INFO, "Read {0} from {1}", new Object[]{loggingFilePropertyName, resourcePath});
    }
    
    @Override
    public Path getLogsDir(String resourcePath, Path outputIfNone) throws IOException {
    
        final Path output;
        
        final Properties props = this.getProperties(resourcePath);
        
        final String pattern = props.getProperty("java.util.logging.FileHandler.pattern");
        
        if(pattern == null) {
         
            output = outputIfNone;
        }else{
            
            final int n = pattern.lastIndexOf('/');
            
            if(n == -1) {
                
                output = outputIfNone;
                
            }else{
                String path = pattern.substring(0, n);
                path = path.replace("%t", System.getProperty("java.io.tmpdir"));
                path = path.replace("%h", System.getProperty("user.home"));
                output = Paths.get(path);
            }
        }
        return output;
    }
    
    @Override
    public void updateLevel(String resourcePath, Level level) throws IOException {
        
        final Properties props = this.getProperties(resourcePath);

        final StringBuilder comments = new StringBuilder();
        comments.append(new Date()).append(' ');
        comments.append(System.getProperty("user.name"));
        comments.append(" updated: ");

        final String update = level.getName();
        final Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean test(String toTest) {
                if(toTest.endsWith(".level")) {
                    for(String s : readOnly) {
                        if(toTest.startsWith(s)) {
                            return false;
                        }
                    }
                    return true;
                }else{
                    return false;
                }
            }
        };
        
        for(String name : props.stringPropertyNames()) {
            if(predicate.test(name)) {
                props.setProperty(name, update);
                comments.append(name).append(" to ").append(update).append(',');
            }
        }
        
        try(Writer writer = new OutputStreamWriter(this.getOutputStream(resourcePath, false))) {
            props.store(writer, comments.toString());
        }
    }
    
    public Properties getProperties(String resourcePath) throws IOException {
        
        final Properties props = new Properties();

        try(Reader reader = new InputStreamReader(this.getInputStream(resourcePath))) {
            props.load(reader);
        }

        return props;
    }
    
    public StringBuilder getContents(String resourcePath) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(this.getInputStream(resourcePath)))) {
            final String nl = System.getProperty("line.separator");
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                builder.append(line).append(nl);
            }
            return builder;
        }
    }
    
    public InputStream getInputStream(String path) throws FileNotFoundException {
        InputStream in = resourceContext.getResourceAsStream(path);
        if(in == null) {
            in = new FileInputStream(path);
        }
        logger.log(Level.FINER, "InputStream: {0}", in);
        return in;
    }
    
    public OutputStream getOutputStream(String path, boolean append) throws FileNotFoundException {
        OutputStream out;
        try {
            out = new FileOutputStream(path, append);
        } catch (FileNotFoundException e) {
            try {
                Path resPath = this.getResourcePath(path, null);
                if(resPath == null) {
                    out = null;
                }else{
                    out = new FileOutputStream(resPath.toString(), false);
                }
            } catch (URISyntaxException use) {
                throw e;
            }
        }
        return out;
    }

    public Path getResourcePath(String path, Path outputIfNone) throws URISyntaxException {
        
        final URL url = resourceContext.getResource(path);
        
        logger.log(Level.FINE, "Resolved resource: {0} to URL: {1}", new Object[]{path, url});
        
        return url == null ? Paths.get(path) : this.getPath(url.toURI(), outputIfNone);
    }
    
    public Path getPath(URI uri, Path outputIfNone) {
        Path output;
        try{
            output = Paths.get(uri);
        }catch(java.nio.file.FileSystemNotFoundException fsnfe) {
            
            logger.log(Level.WARNING, "For URI: "+uri, fsnfe);
            
            final Map<String, String> env = Collections.singletonMap("create", "true");
            
            try(FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
                
                output = Paths.get(uri);
                    
            }catch(IOException ioe) {
                
                logger.log(Level.WARNING, "Exception creating FileSystem for: "+uri, ioe);
                
                output = outputIfNone;
            }
        }
        
        logger.log(Level.FINE, "Resolved URI: {0} to Path: {1}", new Object[]{uri, output});
        
        return output;
    }
}
