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

package com.bc.appcore.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 2, 2017 9:37:51 AM
 */
public class PropertiesManagerImpl implements PropertiesManager {

    private static final Logger logger = Logger.getLogger(PropertiesManagerImpl.class.getName());
    
    private final boolean displayPromptAtLeastOnce;
    private final File file; 
    private final Class type;
    private final String charsetName;
    private final Predicate<Properties> validator;
    private final int maxTrials;
    private final Set<String> propertyNames;
    private final OptionsProvider getOptions;
    
    private int validationAttempts;
    
    private int updateCount;

    public PropertiesManagerImpl(
            OptionsProvider getOptions, File dest, Class type, int maxTrials) {
        this(getOptions, dest, type, "utf-8", new PropertyNamesValidator(type), maxTrials, false);
    }
    
    public PropertiesManagerImpl(
            OptionsProvider prompt, File dest, Class type, String charsetName, 
            Predicate<Properties> validator, int maxTrials, boolean displayPromptAtLeastOnce) {
        this.file = Objects.requireNonNull(dest);
        this.type = Objects.requireNonNull(type);
        this.charsetName = Objects.requireNonNull(charsetName);
        this.validator = Objects.requireNonNull(validator);
        this.maxTrials = maxTrials;
        this.propertyNames = this.getPropertyNames(type);
        this.getOptions = Objects.requireNonNull(prompt);
        this.displayPromptAtLeastOnce = displayPromptAtLeastOnce;
        logger.fine(() -> "Type: "+type.getName()+", Property names: "+this.propertyNames);
    }
    
    @Override
    public Properties getProperties(Map defaultValues) throws IOException {

        final Properties props = this.load(defaultValues);
        
        boolean valid = false;
        
        this.updateCount = 0;
        
        this.validationAttempts = 0;
        
        while(!(valid = validator.test(props)) && validationAttempts++ < maxTrials) {

            updateCount = this.updateWithValuesFromUserPrompt(props);
        } 
        
        if(this.displayPromptAtLeastOnce && this.validationAttempts < 1) {
        
            updateCount = this.updateWithValuesFromUserPrompt(props);
        }
        
        if(valid && updateCount > 0) {
            
            this.store(props);
        }
        
        return props;
    }    
    
    @Override
    public Properties load(Map defaultValues) throws IOException {

        final Properties props = new Properties();
        logger.log(Level.FINE, "Loading properties from: {0}", file);
        try(Reader reader = this.createReader(file, charsetName)) {
            props.load(reader);
        }
        
        logger.log(Level.FINER, "Loaded properties: {0}", props);
        
        for(String propertyName : propertyNames) {
            final String prop = props.getProperty(propertyName);
            if(prop == null) {
                final Object val = defaultValues.get(propertyName);
                if(val != null) {
                    props.setProperty(propertyName, this.toString(propertyName, val));
                }
            }
        }
        
        logger.log(Level.FINER, "After updating loaded properties with defaults: {0}", props);
        
        return props;
    }
    
    @Override
    public void store(Properties props) throws IOException {
        try(Writer writer = this.createWriter(file, charsetName)) {
            props.store(writer, "Saved by " + System.getProperty("user.name") + " on " + LocalDateTime.now());
        }
    }
    
    public Reader createReader(File file, String charsetName) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
    }
    
    public Writer createWriter(File file, String charsetName) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charsetName));
    }
    
    public int updateWithValuesFromUserPrompt(Properties props) {
        
        final Map options = new LinkedHashMap(props.size(), 1.0f);
        final Set<String> names = props.stringPropertyNames();
        for(String name : names) {
            final String value = props.getProperty(name);
            options.put(name, this.fromString(name, value));
        }
        
        this.propertyNames.stream().forEach((name) -> options.putIfAbsent(name, null));
        
        logger.log(Level.FINER, "Displaying prompt for: {0}", options);

        final Map<String, Object> userInput = getOptions.get(options);

        logger.log(Level.FINE, "User input: {0}", userInput);

        final AtomicInteger updateCount = new AtomicInteger(0);
        
        userInput.forEach((k, v) -> {
            if(v != null) {
                final String current = props.getProperty(k);
                final String update = toString(k, v);
                if(!Objects.equals(current, update)) {
                    props.setProperty(k, update);
                    updateCount.incrementAndGet();
                }
            }
        });
        
        logger.fine(() -> "Updated " + updateCount.intValue() + " properties. Update: " + props);
        
        return updateCount.intValue();
    }

    public Object fromString(String key, String val) {
        final Object output;
        if(this.isPasswordName(key)) {
            output = val.toCharArray();
        }else {
            output = val;
        }
        this.logOutput(key, val, output);
        return output;
    }
    
    public String toString(String key, Object val) {
        final String output;
        if(this.isPasswordName(key) && val instanceof char[]) {
            output = new String((char[])val);
        }else {
            output = val.toString();
        }
        this.logOutput(key, val, output);
        return output;
    }

    protected void logOutput(String key, Object val, Object output) {
        if(!this.isPasswordName(key)) {
            logger.finer(() -> "Output: "+output+", inputs: ("+key+", "+val+')');
        }
    }
    
    protected boolean isPasswordName(String name) {
        return name.toLowerCase().contains("password");
    }
    
    private Set<String> getPropertyNames(Class type) {
        final Field [] fields = type.getFields(); 
        final Set<String> output = new LinkedHashSet(fields.length);
        for(Field field : fields) {
            try{
                final String name = (String)field.get(null);
                output.add(name);
            }catch(ClassCastException | IllegalArgumentException | IllegalAccessException ignored) { }
        }
        return output.isEmpty() ? Collections.EMPTY_SET : Collections.unmodifiableSet(output);
    }

    @Override
    public int getValidationAttempts() {
        return validationAttempts;
    }

    @Override
    public int getUpdateCount() {
        return updateCount;
    }
    
    public File getFile() {
        return file;
    }

    public Class getType() {
        return type;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public int getMaxTrials() {
        return maxTrials;
    }

    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    public OptionsProvider getGetOptions() {
        return getOptions;
    }
}
