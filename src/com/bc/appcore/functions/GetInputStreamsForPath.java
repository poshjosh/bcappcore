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

package com.bc.appcore.functions;

import com.bc.appcore.ResourceLoader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 8:41:18 PM
 */
public class GetInputStreamsForPath implements Function<Path, List<InputStream>> {

    private static final Logger logger = Logger.getLogger(GetInputStreamsForPath.class.getName());

    private final ClassLoader classLoader;

    public GetInputStreamsForPath() {
        this(Thread.currentThread().getContextClassLoader());
    }
    
    public GetInputStreamsForPath(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }
    
    @Override
    public List<InputStream> apply(Path path) {
        try{
            return get(path);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public InputStream getSingle(Path path) throws IOException {
        return getSingle(path, get(path));
    }
    
    private <T> T getSingle(Path path, List<T> list) throws IOException {
        if(list.isEmpty()) {
            throw new IOException("For: "+path);
        }
        if(list.size() > 1) {
            throw new RuntimeException("Found > 1 instances for: " + path);
        }
        return list.get(0);
    }

    public List<InputStream> get(Path path) throws IOException {
        List<InputStream> output;
        try{
            output = Collections.singletonList(new FileInputStream(path.toFile()));
        }catch(IOException ioe) {
            final String string = path.toString();
            try{
                final List<URL> urls = new ResourceLoader(classLoader).get(string);
                if(urls.isEmpty()) {
                    throw ioe;
                }else{
                    output = new ArrayList(urls.size());
                    for(URL url : urls) {
                        output.add(url.openStream());
                    }
                }
            }catch(IOException e) {
                throw ioe;
            }
        }
        return output;
    }
}
