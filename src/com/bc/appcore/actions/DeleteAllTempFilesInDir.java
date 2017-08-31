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

package com.bc.appcore.actions;

import com.bc.appcore.exceptions.TaskExecutionException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.bc.appcore.AppCore;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 22, 2017 8:29:08 AM
 */
public class DeleteAllTempFilesInDir implements Action<AppCore, List<File>>{

    public static final String RECURSIVE = "recursive";
    
    @Override
    public List<File> execute(AppCore app, Map<String, Object> params) throws TaskExecutionException {
        
        final File dir = (File)params.get(File.class.getName());
        
        final Boolean recursive = params.get(RECURSIVE) == null ? 
                Boolean.TRUE : (Boolean)params.get(RECURSIVE);
        
        return this.deleteTempFilesIn(dir, recursive);
    }

    public List<File> deleteTempFilesIn(File dir, boolean recursive) throws TaskExecutionException {
        
        final List<File> output;
        
        if(dir == null || !dir.exists()) {
            output = Collections.EMPTY_LIST;
        }else{
            
            final File [] tempFiles = dir.listFiles(new FilenameFilter(){
                @Override
                public boolean accept(File dir, String name) {
                    final int end = name.lastIndexOf('.');
                    if(end != -1) { 
                        name = name.substring(0, end);
                    }
                    return name.endsWith("temp") || name.endsWith("TEMP");
                }
            });
            if(tempFiles == null || tempFiles.length == 0) {
                output = Collections.EMPTY_LIST;
            }else{
                output = new ArrayList(tempFiles.length);
                for(File tempFile : tempFiles) {
                    if(tempFile.isDirectory()) {
                        if(recursive) {
                            this.deleteTempFilesIn(tempFile, recursive);
                        }
                    }
                    if(tempFile.delete()) {
                        output.add(tempFile);
                    }else{
                        tempFile.deleteOnExit();
                    }
                }
            }
        }
        
        return output;
    }
}
