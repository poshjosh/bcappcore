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

import com.bc.config.Config;
import com.bc.config.ConfigService;
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

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 8, 2017 3:02:15 AM
 */
public class SettingsImpl extends HashMap<String, Object> implements Settings {
    
    private final ConfigService svc;

    private final Config data;
    
    private final Properties metaData;
    
    private final Map<String, Object> byLabels;

    public SettingsImpl(ConfigService svc, Config data, Properties metaData) {
        this.svc = Objects.requireNonNull(svc);
        this.data = Objects.requireNonNull(data);
        this.metaData = Objects.requireNonNull(metaData);
        this.byLabels = new HashMap<>();
        final Set<String> allNames = data.stringPropertyNames();
        for(String name : allNames) {
            if(this.getAlias(name, null) != null||
                    this.getLabel(name, null) != null ||
                    this.getDescription(name, null) != null ||
                    this.getTypeName(name, null) != null ||
                    !this.getOptions(name).isEmpty()) {
                
                final Object value = this.get(name, null);
                
                super.put(name, value);
                
                this.byLabels.put(this.getLabel(name, name), value);
            }
        }
    }

    @Override
    public Map<String, Object> getByLabels() {
        return this.byLabels;
    }

    @Override
    public Object put(String name, Object newValue) {
        final Object oldValue = this.getOrDefault(name, null);
        this.set(name, newValue);
        try{
            this.svc.store();
        }catch(IOException e) {
            this.set(name, oldValue);
            throw new RuntimeException(e);
        }
        super.put(name, newValue);
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
        final String [] arr = new com.bc.config.ConfigImpl(metaData, null).getArray(name);
        return arr == null || arr.length == 0 ? Collections.EMPTY_LIST : Arrays.asList(arr);
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
            value = data.setString(name, (String)newValue);
        }else if(type == Double.class) {
            value = data.setDouble(name, (Double)newValue);
        }else if(type == Long.class) {
            value = data.setLong(name, (Long)newValue);
        }else if(type == Integer.class) {
            value = data.setInt(name, (Integer)newValue);
        }else if(type == Float.class) {
            value = data.setFloat(name, (Float)newValue);
        }else if(type == Short.class) {
            value = data.setShort(name, (Short)newValue);
        }else if(type == Boolean.class) {
            value = data.setBoolean(name, (Boolean)newValue);
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
