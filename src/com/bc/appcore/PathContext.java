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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 7:53:15 PM
 */
public interface PathContext {

    Path get(String typeName);
    
    InputStream getInputStream(String typeName) throws IOException;
    
    List<InputStream> getInputStreams(String typeName) throws IOException;
    
    OutputStream getOutputStream(String typeName, boolean append) throws IOException;
    
    List<OutputStream> getOutputStreams(String typeName, boolean append) throws IOException;
}
