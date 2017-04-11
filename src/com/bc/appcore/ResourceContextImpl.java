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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 7, 2017 12:17:32 PM
 */
public class ResourceContextImpl implements ResourceContext {
    
    private transient static final Logger logger = Logger.getLogger(ResourceContextImpl.class.getName());
    
    public ResourceContextImpl() throws IOException {
        this(new String[0], new String[0]);
    }
    
    public ResourceContextImpl(String [] dirsToCreateIfNone, String [] filesToCreateIfNone) 
            throws IOException {
        
        for(String dirToCreateIfNone : dirsToCreateIfNone) {
            ResourceContextImpl.this.initDir(dirToCreateIfNone);
        }
        
        for(String fileToCreateIfNone : filesToCreateIfNone) {
            ResourceContextImpl.this.initFile(fileToCreateIfNone);
        }
    }
    
    public void initDir(String dir) {
        final File file = this.getPath(dir).toFile();
        if(!file.exists()) {
            file.mkdirs();
        }
    }
    
    public File initFile(String path) throws IOException {
        final File file = getPath(path).toFile();
        this.initFile(file);
        return file;
    }

    public void initFile(File file) throws IOException {
        final File parent = file.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }
        if(!file.exists()) {
            file.createNewFile();
        }
    }
    
    @Override
    public URL getResource(String path) {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        return url;
    }
    
    @Override
    public InputStream getResourceAsStream(String path) {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return in;
    }
    
    @Override
    public Path getPath(String first, String... more) {
        return Paths.get(first, more);
    }
}