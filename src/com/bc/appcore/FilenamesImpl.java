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

/**
 * @author Chinomso Bassey Ikwuagwu on May 2, 2017 6:57:13 PM
 */
public class FilenamesImpl implements Filenames {

    private final String workingDir;
    
    private final String loggingConfigFile;
    
    private final String propertiesFile;

    public FilenamesImpl(String workingDir, String loggingConfigFile, String propertiesFile) {
        this.workingDir = workingDir; 
        this.loggingConfigFile = loggingConfigFile;
        this.propertiesFile = propertiesFile;
    }

    @Override
    public String getWorkingDir() {
        return workingDir;
    }

    @Override
    public String getLoggingConfigFile() {
        return loggingConfigFile;
    }

    @Override
    public String getPropertiesFile() {
        return propertiesFile;
    }
}
