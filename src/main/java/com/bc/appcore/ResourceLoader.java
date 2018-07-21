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

import com.bc.functions.GetSingle;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 9:45:47 PM
 */
public class ResourceLoader {

    private static final Logger logger = Logger.getLogger(ResourceLoader.class.getName());

    private final ClassLoader classLoader;

    public ResourceLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }
    
    public ResourceLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }
    
    public URL get(String resource, URL outputIfNone) throws IOException {
        final List<URL> list = get(resource);
        return new GetSingle<URL>().getOrDefault(list, outputIfNone);
    }
    
    public List<URL> get(String resource) throws IOException {
        final List<URL> output;
        final URL internal = this.getInternal(resource);
        if(internal == null) {
            final Enumeration<URL> urls = this.getExternal(resource);
            output = urls == null || !urls.hasMoreElements() ? 
                    Collections.EMPTY_LIST : Collections.list(urls);
        }else{
            output = Collections.singletonList(internal);
        }
        logger.fine(() -> "Resolved resource: "+resource+" to URL: "+output);
        return output;
    }
    
    public URL getInternal(String resource) {
        resource = resource.replace('\\', '/');
        return classLoader.getResource(resource);
    } 
    
    public Enumeration<URL> getExternal(String resource) throws IOException {
        resource = resource.replace('\\', '/');
        return classLoader.getResources(resource);
    }
}
