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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 20, 2017 7:14:29 PM
 */
public class LoggingConfigManagerImpl implements LoggingConfigManager {

    private static final Logger logger = Logger.getLogger(LoggingConfigManagerImpl.class.getName());

    private final List<String> readOnly = Arrays.asList(new String[]{
        "java", "javax", "sun", "com.sun", "com.mysql", "org"
    });
    
    
    private class IsLevelProperty implements Predicate<String> {
        public IsLevelProperty() { }
        @Override
        public boolean test(String toTest) {
            return toTest.endsWith(".level");
        }
    }
    
    private class IsReadOnlyProperty implements Predicate<String> {
        public IsReadOnlyProperty() { }
        @Override
        public boolean test(String toTest) {
            for(String s : readOnly) {
                if(toTest.startsWith(s)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private final String charsetName;

    public LoggingConfigManagerImpl() {
        this("utf-8");
    }
    
    public LoggingConfigManagerImpl(String charsetName) {
        this.charsetName = Objects.requireNonNull(charsetName);
    }

    @Override
    public void read(String loggingConfigPath) throws IOException {
        
        final String loggingFilePropertyName = "java.util.logging.config.file";
        
        logger.finer(() ->  loggingFilePropertyName + " = " + System.getProperty(loggingFilePropertyName));
        
        System.setProperty(loggingFilePropertyName, loggingConfigPath);
        
        logger.log(Level.FINER, "Reading logging config file: {0}", loggingConfigPath);
            
        try(InputStream in = new FileInputStream(loggingConfigPath)) {
            
            logger.log(Level.FINER, "Input stream: {0}", in);
            
            LogManager.getLogManager().readConfiguration(in);
        }
        
        logger.info(() -> "Read: "+loggingFilePropertyName+", from: "+loggingConfigPath);
    }

    @Override
    public Path getLogsDir(String loggingConfigPath, Path outputIfNone) throws IOException {
    
        final Path output;
        
        final Properties props = this.getProperties(loggingConfigPath);
        
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
    public void updateLevel(String loggingConfigPath, Level level) throws IOException {
        
        Objects.requireNonNull(loggingConfigPath);
        Objects.requireNonNull(level);
        
        final Properties props = this.getProperties(loggingConfigPath);

        final StringBuilder comments = new StringBuilder();
        comments.append(new Date()).append(' ');
        comments.append(System.getProperty("user.name"));
        comments.append(" updated: ");

        final String update = level.getName();
        
        final Predicate<String> predicate = new IsLevelProperty().and(new IsReadOnlyProperty().negate());
        
        for(String name : props.stringPropertyNames()) {
            if(predicate.test(name)) {
                props.setProperty(name, update);
                comments.append(name).append(" to ").append(update).append(',');
            }
        }
        
        try(Writer writer = new OutputStreamWriter(new FileOutputStream(loggingConfigPath, false), charsetName)) {
            props.store(writer, comments.toString());
        }
    }

    @Override
    public Properties getProperties(String path) throws IOException {
        
        final Properties props = new Properties();
        
        try(Reader reader = new InputStreamReader(new FileInputStream(path), charsetName)) {
            props.load(reader);
        }

        return props;
    }
}
