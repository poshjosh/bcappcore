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

import com.bc.appcore.AppContext;
import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;
import com.bc.appcore.properties.PropertiesContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 14, 2017 10:37:29 PM
 */
public class GetPropertiesFileForPersistenceUnit implements Function<String, File> {
    
    private final String parentDir;

    public GetPropertiesFileForPersistenceUnit(AppContext appContext) {
        this(appContext.getPropertiesPaths());
    }
    
    public GetPropertiesFileForPersistenceUnit(PropertiesContext propertiesPaths) {
        this(propertiesPaths.getDirPath());
    }
    
    public GetPropertiesFileForPersistenceUnit(String parentDir) {
        this.parentDir = Objects.requireNonNull(parentDir);
    }

    @Override
    public File apply(String persistenceUnit) {
        return Paths.get(this.parentDir, PropertiesContext.TypeName.JDBC_JPA + '_'+persistenceUnit+".properties").toFile();
    }
}
