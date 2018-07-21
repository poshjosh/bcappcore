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

import com.bc.appcore.functions.GetInputStreams;
import com.bc.appcore.functions.GetOutputStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.bc.appcore.Names;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 8:28:05 PM
 */
public class PropertiesContextBuilder implements Serializable {

    private transient static final Logger LOG = Logger.getLogger(PropertiesContextBuilder.class.getName());

    private ClassLoader classLoader;
    
    private String workingDirPath;
    
    private String dirName;
    
    private String prefix;
    
    private String suffix;
    
    private boolean buildAttempted;
    
    private final Map<String, String> typePrefixes;
    
    private final Map<String, String> typeFileNames;
    
    private final Map<String, String> typeSuffixes;
    
    public PropertiesContextBuilder() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.dirName = Names.CONFIGS_DIR;
        this.typePrefixes = new HashMap<>();
        this.typeFileNames = new HashMap<>();
        this.typeSuffixes = new HashMap<>();
    }
    
    public PropertiesContext build() {
        
        if(buildAttempted) {
            throw new IllegalStateException();
        }
        this.buildAttempted = true;
        
        Objects.requireNonNull(classLoader);
        Objects.requireNonNull(workingDirPath);
        Objects.requireNonNull(typeFileNames);
        
        return new PropertiesContextBuilder.PropertiesContextImpl();
    }
    
    public PropertiesContextBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
    
    public PropertiesContextBuilder workingDirPath(String workingDirPath) {
        this.workingDirPath = workingDirPath;
        return this;
    }
    
    public PropertiesContextBuilder dirName(String dirName) {
        this.dirName = dirName;
        return this;
    }
    
    public PropertiesContextBuilder typePrefix(String typeName, String prefix) {
        this.typePrefixes.put(typeName, prefix);
        return this;
    }
    
    public PropertiesContextBuilder typeFileNames(Map<String, String> typeFileNames) {
        this.typeFileNames.putAll(typeFileNames);
        return this;
    }
    
    public PropertiesContextBuilder typeFileName(String typeName, String fileName) {
        this.typeFileNames.put(typeName, fileName);
        return this;
    }
    
    public PropertiesContextBuilder typeSuffix(String typeName, String suffix) {
        this.typeSuffixes.put(typeName, suffix);
        return this;
    }
    
    public PropertiesContextBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public PropertiesContextBuilder suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    private class PropertiesContextImpl implements PropertiesContext {
        @Override
        public String getWorkingDirPath() {
            return workingDirPath;
        }

        @Override
        public String getDirPath() {
            if(dirName == null || dirName.isEmpty()) {
                return workingDirPath;
            }else{
                return Paths.get(workingDirPath, dirName).toString();
            }
        }

        @Override
        public Path get(String typeName) {
            final String parent = this.getDirPath();
            final String filename = this.getFileName(typeName);
            final Path path = Paths.get(parent, filename);
            LOG.fine(() -> "Parent: " + parent + ", name: " + filename + "\nPath: " + path);
            return path;
        }

        @Override
        public InputStream getInputStream(String typeName) throws IOException {
            final Path path = this.get(typeName);
            final InputStream output = new GetInputStreams(classLoader).getSingle(path);
            return output;
        }

        @Override
        public List<InputStream> getInputStreams(String typeName) throws IOException {
            final Path path = this.get(typeName);
            final List<InputStream> output = new GetInputStreams(classLoader).get(path);
            return output;
        }

        @Override
        public OutputStream getOutputStream(String typeName, boolean append) throws IOException {
            final Path path = this.get(typeName);
            final OutputStream output = new GetOutputStreams(classLoader, append).getSingle(path);
            return output;
        }
        
        @Override
        public List<OutputStream> getOutputStreams(String typeName, boolean append) throws IOException {
            final Path path = this.get(typeName);
            final List<OutputStream> output = new GetOutputStreams(classLoader, append).get(path);
            return output;
        }
        
        public String getFileName(String typeName) {
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

        public String getDirName() {
            return dirName;
        }
    }
}
