/*
 * Copyright 2018 NUROX Ltd.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 1, 2018 5:52:26 PM
 */
public class PropertiesContextFilenameProvider 
        implements UnaryOperator<String>, Serializable {

    private String prefix;
    
    private String suffix;
    
    private final Map<String, String> typePrefixes;
    
    private final Map<String, String> typeFileNames;
    
    private final Map<String, String> typeSuffixes;
    
    public PropertiesContextFilenameProvider() {
        this.typePrefixes = new HashMap<>();
        this.typeFileNames = new HashMap<>();
        this.typeSuffixes = new HashMap<>();
    }
    
    public PropertiesContextFilenameProvider typePrefix(String typeName, String prefix) {
        this.typePrefixes.put(typeName, prefix);
        return this;
    }
    
    public PropertiesContextFilenameProvider typeFileNames(Map<String, String> typeFileNames) {
        this.typeFileNames.putAll(typeFileNames);
        return this;
    }
    
    public PropertiesContextFilenameProvider typeFileName(String typeName, String fileName) {
        this.typeFileNames.put(typeName, fileName);
        return this;
    }
    
    public PropertiesContextFilenameProvider typeSuffix(String typeName, String suffix) {
        this.typeSuffixes.put(typeName, suffix);
        return this;
    }
    
    public PropertiesContextFilenameProvider prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public PropertiesContextFilenameProvider suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    @Override
    public String apply(String typeName) {
        final String fileName;
        final String cached = typeFileNames.get(typeName);
        if(cached != null) {
            fileName = cached;
        }else{
            final String mPrefix = this.getPrefix(typeName);
            final String mSuffix = this.getSuffix(typeName);
            if(mPrefix == null && mSuffix == null) {
                fileName = typeName + ".properties";
            }else if(mPrefix == null && mSuffix != null) {
                fileName = typeName + '_' + mSuffix  + ".properties";
            }else if(mPrefix != null && mSuffix == null) {
                fileName = mPrefix + '_' + typeName + ".properties";
            }else{
                fileName = mPrefix + '_' + typeName + '_' + mSuffix  + ".properties";
            }
        }
        return fileName;
    }

    public String getPrefix(String typeName) {
        return typePrefixes.getOrDefault(typeName, prefix);
    }

    public String getSuffix(String typeName) {
        return typeSuffixes.getOrDefault(typeName, suffix);
    }
}
