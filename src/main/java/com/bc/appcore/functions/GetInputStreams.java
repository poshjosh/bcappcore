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
import com.bc.functions.GetSingle;
import java.io.File;
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
public class GetInputStreams implements Function<String, List<InputStream>> {

    private static final Logger LOG = Logger.getLogger(GetInputStreams.class.getName());

    private final ClassLoader classLoader;

    public GetInputStreams() {
        this(Thread.currentThread().getContextClassLoader());
    }
    
    public GetInputStreams(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader);
    }
    
    @Override
    public List<InputStream> apply(String source) {
        try{
            return get(source);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public InputStream getSingle(Path path) throws IOException {
        return getSingle(get(path));
    }
    
    public InputStream getSingle(String source) throws IOException {
        return getSingle(get(source));
    }

    private <T> T getSingle(List<T> list) throws IOException {
        return new GetSingle<T>().getOrException(list);
    }

    public List<InputStream> get(Path path) throws IOException {
        return get(path.toString());
    }
    
    public List<InputStream> get(String source) throws IOException {
        List<InputStream> output;
        try{
            final InputStream in = new FileInputStream(new File(source));
            output = Collections.singletonList(in);
            LOG.fine(() -> "Source: " + source + ", Input stream: " + in);
        }catch(IOException ioe) {
            try{
                final List<URL> urls = new ResourceLoader(classLoader).get(source);
                LOG.fine(() -> "URLs: " + urls);
                if(urls.isEmpty()) {
                    throw ioe;
                }else{
                    output = new ArrayList(urls.size());
                    for(URL url : urls) {
                        final InputStream is = url.openStream();
                        LOG.fine(() -> "URL: " + url + ", Input stream: " + is);
                        output.add(is);
                    }
                }
            }catch(IOException e) {
                throw ioe;
            }
        }
        return output;
    }
}
