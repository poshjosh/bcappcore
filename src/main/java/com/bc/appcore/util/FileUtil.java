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

import java.io.File;
import java.io.IOException;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 3, 2017 11:03:41 AM
 */
public class FileUtil {
    
    public static File[] createFiles(String... pathnames) throws IOException{
        final File [] files = new File[pathnames.length];
        for(int i=0; i<files.length; i++) {
            files[i] = new File(pathnames[i]);
            final File parent = files[i].getParentFile();
            if(!parent.exists()) {
                parent.mkdirs();
            }
            if(!files[i].exists()) {
                files[i].createNewFile();
            }
        }
        return files;
    }
    
    public static String convertToExtension(String path, String extension) {
        final int n = path.lastIndexOf('.');
        if(n == -1) {
            return path + '.' + extension;
        }else{
            return path.substring(0, n) + '.' + extension;
        }
    }
    
    public static String constructFilename(String path, String suffix) {
        final int n = path.lastIndexOf('.');
        if(n == -1) {
            throw new UnsupportedOperationException("Path has no extension: "+path);
        }else{
            return path.substring(0, n) + '_' + suffix + path.substring(n);
        }
    }
}
