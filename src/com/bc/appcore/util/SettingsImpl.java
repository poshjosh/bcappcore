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

import com.bc.appcore.AppCore;
import com.bc.appcore.actions.Action;
import com.bc.appcore.functions.CreateActionFromClassName;
import com.bc.appcore.exceptions.TaskExecutionException;
import com.bc.appcore.parameter.ParameterException;
import com.bc.config.Config;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 8, 2017 3:02:15 AM
 */
public class SettingsImpl extends HashMap<String, Object> implements Settings {

    private static final Logger logger = Logger.getLogger(SettingsImpl.class.getName());
    
    private final AppCore app;
    
    private final Config data;
    
    private final Properties metaData;
    
    private final Map<String, Object> byLabels;

    public SettingsImpl(AppCore app) {
        this(app, app.getConfig(), app.getSettingsConfig());
    }
    
    public SettingsImpl(AppCore app, Config data, Properties metaData) {
        this.app = Objects.requireNonNull(app);
        this.data = Objects.requireNonNull(data);
        this.metaData = Objects.requireNonNull(metaData);
        
        this.byLabels = new HashMap<>();
        
        final Set<String> allNames = data.stringPropertyNames();
        
        logger.log(Level.FINER, "All names: {0}", allNames);
        
        this.init(allNames);
    }
    
    private void init(Set<String> allNames) {
        for(String name : allNames) {
            if(this.getAlias(name, null) != null ||
                    this.getLabel(name, null) != null ||
                    this.getDescription(name, null) != null ||
                    this.getTypeName(name, null) != null ||
                    !this.getOptions(name).isEmpty()) {
                
                this.addInit(name);
            }    
        }
    }
    
    private void addInit(String name) {
        
        final List<Action> actionsAreValidatedThus = this.getActions(name);
        
        final Object value = this.get(name, null);
        
        logger.log(Level.FINER, () -> "Adding setting: " + name + '=' + value);

        super.put(name, value);

        this.byLabels.put(this.getLabel(name, name), value);
    }

    @Override
    public Map<String, Object> getByLabels() {
        return this.byLabels;
    }

    /**
     * As against {@link #putAll(java.util.Map)} this method persists the
     * added data across sessions and application re-launches.
     * @param m 
     * @throws java.io.IOException 
     */
    @Override
    public void updateAll(Map<? extends String, ? extends Object> m) throws IOException {
        for(Entry<? extends String, ? extends Object> entry : m.entrySet()) {
            this.update(entry.getKey(), entry.getValue(), false);
        }
    }

    /**
     * As against {@link #put(java.lang.Object, java.lang.Object)} this method
     * persists the added data across sessions and application re-launches.
     * @param name
     * @param newValue
     * @return 
     * @throws java.io.IOException 
     */
    @Override
    public Object update(String name, Object newValue) throws IOException {
        return this.update(name, newValue, false);
    }

    public Object update(String name, Object newValue, boolean onlyIfAbsent) 
            throws IOException {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Putting: {0}={1}", new Object[]{name, newValue});
        }
        
        final Object oldValue = this.getOrDefault(name, null);
        
        if(!onlyIfAbsent || oldValue == null) {
            
            boolean set = false;
            boolean stored = false;
            
            this.set(name, newValue);
            set = true;

            try{

                stored = app.saveConfig();
                
                if(stored) {
                    
                    final List<Action> actions = this.getActions(name);

                    logger.fine(() -> "Setting: " + name + ", has " + actions.size() + " actions.");

                    for(Action action : actions) {
                        
                        logger.fine(() -> "For setting: " + name + ", executing action: " + action);

                        action.execute(app, Collections.singletonMap(name, newValue));
                    }

                    logger.fine("Save successful");
                }else{
                    if(set) {
                        this.set(name, oldValue);
                    }
                }

            }catch(ParameterException | TaskExecutionException e) {

                if(set) {
                    this.set(name, oldValue);
                }
                
                if(stored) {
                    app.saveConfig();
                }

                throw new RuntimeException(e);
            }

            super.put(name, newValue);
        }
        
        return oldValue;
    }
    
    @Override
    public Class getValueType(String name, Class outputIfNone) {
        final String typeName = this.getTypeName(name, null);
        if(typeName == null) {
            return outputIfNone;
        }else{
            try{
                return Class.forName(typeName);
            }catch(ClassNotFoundException e) {
                return outputIfNone;
            }
        }
    }

    @Override
    public String getLabel(String name, String outputIfNone) {
        name = this.getAlias(name, name);
        return this.metaData.getProperty(name+".label", outputIfNone);
    }

    @Override
    public String getName(String label, String outputIfNone) {
        for(String name : this.keySet()) {
            if(label.equals(this.getLabel(name, null))) {
                return name;
            }
        }
        return outputIfNone;
    }
    
    @Override
    public String getDescription(String name, String outputIfNone) {
        name = this.getAlias(name, name);
        return this.metaData.getProperty(name+".description", outputIfNone);
    }

    @Override
    public String getTypeName(String name, String outputIfNone) {
        name = this.getAlias(name, name);
        return this.metaData.getProperty(name+".valueType", outputIfNone);
    }
    
    @Override
    public String getAlias(String name, String outputIfNone) {
        return this.metaData.getProperty(name+".alias", outputIfNone);
    }

    @Override
    public List<String> getOptions(String name) {
        
        name = this.getAlias(name, name);

        final com.bc.config.Config config = new com.bc.config.ConfigImpl(metaData, null);

        final String [] arr = config.getArray(name+".options");

        return arr == null || arr.length == 0 ? Collections.EMPTY_LIST : Arrays.asList(arr);
    }
    
    @Override
    public List<Action> getActions(String name) {
        
        name = this.getAlias(name, name);

        final com.bc.config.Config config = new com.bc.config.ConfigImpl(metaData, null);

        final String [] arr = config.getArray(name+".actions");
        
        final List<Action> actions;

        if(arr == null || arr.length == 0) {
            
            actions = Collections.EMPTY_LIST;
            
        }else{
            
            actions = Arrays.asList(arr).stream().map(new CreateActionFromClassName()).collect(Collectors.toList());
        }
        
        return actions;
    }
    
    public Object get(String name, Object outputIfNone) {
        Object value = outputIfNone;
        final Class type = this.getValueType(name, null);
        if(type == String.class) {
            value = data.getString(name);
        }else if(type == Double.class) {
            value = data.getDouble(name);
        }else if(type == Long.class) {
            value = data.getLong(name);
        }else if(type == Integer.class) {
            value = data.getInt(name);
        }else if(type == Float.class) {
            value = data.getFloat(name);
        }else if(type == Short.class) {
            value = data.getShort(name);
        }else if(type == Boolean.class) {
            value = data.getBoolean(name);
        }else if(type == Date.class) {
            try{
                value = data.getTime(name);
            }catch(ParseException e) {
                throw new RuntimeException(e);
            }
        }else if(type == Collection.class) {
            value = data.getCollection(name);
        }else if(type == Map.class) {
            value = data.getMap(name, "&");
        }else {
            value = data.getProperty(name);
        }
        return value;
    }

    public Object set(String name, Object newValue) {
        Object value = newValue;
        final Class type = this.getValueType(name, null);
        if(type == String.class) {
            value = data.setString(name, newValue==null?null:newValue.toString());
        }else if(type == Double.class) {
            value = data.setDouble(name, newValue==null?null:newValue instanceof Double?(Double)newValue:Double.parseDouble(newValue.toString()));
        }else if(type == Long.class) {
            value = data.setLong(name, newValue==null?null:newValue instanceof Long?(Long)newValue:Long.parseLong(newValue.toString()));
        }else if(type == Integer.class) {
            value = data.setInt(name, newValue==null?null:newValue instanceof Integer?(Integer)newValue:Integer.parseInt(newValue.toString()));
        }else if(type == Float.class) {
            value = data.setFloat(name, newValue==null?null:newValue instanceof Float?(Float)newValue:Float.parseFloat(newValue.toString()));
        }else if(type == Short.class) {
            value = data.setShort(name, newValue==null?null:newValue instanceof Short?(Short)newValue:Short.parseShort(newValue.toString()));
        }else if(type == Boolean.class) {
            value = data.setBoolean(name, newValue==null?null:newValue instanceof Boolean?(Boolean)newValue:Boolean.parseBoolean(newValue.toString()));
        }else if(type == Date.class) {
            try{
                final Calendar cal = Calendar.getInstance();
                cal.setTime((Date)newValue);
                value = data.setTime(name, cal); 
            }catch(ParseException e) {
                throw new RuntimeException(e);
            }
        }else if(type == Collection.class) {
            value = data.setCollection(name, (Collection)newValue);
        }else if(type == Map.class) {
            value = data.setMap(name, (Map)newValue, "&");
        }else {
            value = data.setProperty(name, (String)newValue);
        }
        return value;
    }
}
