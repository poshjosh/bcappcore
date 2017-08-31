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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 10:36:22 PM
 */
public class GetOutputStreamsForPath implements Function<Path, List<OutputStream>> {

    private static final Logger logger = Logger.getLogger(GetOutputStreamsForPath.class.getName());

    private final ClassLoader classLoader;
    
    private final boolean append;

    public GetOutputStreamsForPath() {
        this(Thread.currentThread().getContextClassLoader(), false);
    }
    
    public GetOutputStreamsForPath(ClassLoader classLoader, boolean append) {
        this.classLoader = Objects.requireNonNull(classLoader);
        this.append = append;
    }
    
    @Override
    public List<OutputStream> apply(Path path) {
        try{
            return get(path);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public OutputStream getSingle(Path path) throws IOException {
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

    public List<OutputStream> get(Path path) throws IOException {
        List<OutputStream> output;
        try {
            output = Collections.singletonList(new FileOutputStream(path.toFile(), append));
        } catch (FileNotFoundException fnfe) {
            try {
                final List<URL> urls = new ResourceLoader(classLoader).get(path.toString());
                if(urls.isEmpty()) {
                    throw fnfe;
                }else{
                    output = new ArrayList<>(urls.size());
                    for(URL url : urls) {
                        try{
                            output.add(url.openConnection().getOutputStream());
                        }catch(IOException ioe) {
                            final Path updatedPath = this.getPathCreateFileSystemIfNeed(url.toURI(), null);
                            if(updatedPath != null) {
                                output.add(new FileOutputStream(updatedPath.toFile(), append));
                            }else{
                                throw fnfe;
                            }
                        }
                    }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return output;
    }
    
    private Path getPathCreateFileSystemIfNeed(URI uri, Path outputIfNone) {
        Path output;
        try{
            output = Paths.get(uri);
        }catch(java.nio.file.FileSystemNotFoundException fsnfe) {
            
            logger.log(Level.WARNING, "For URI: "+uri, fsnfe);
            
            final Map<String, String> env = Collections.singletonMap("create", "true");
            
            try(FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
                
                output = Paths.get(uri);
                    
            }catch(IOException ioe) {
                
                logger.log(Level.WARNING, "Exception creating FileSystem for: "+uri, ioe);
                
                output = outputIfNone;
            }
        }
        
        logger.log(Level.FINE, "Resolved URI: {0} to Path: {1}", new Object[]{uri, output});
        
        return output;
    }

}
