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

package com.bc.appcore.jpa;

import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceUnitContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 19, 2017 3:38:29 AM
 */
public interface PersistenceContextManager {

    PersistenceContext create(URI uri, int maxTrials, boolean freshInstall) throws URISyntaxException;

    PersistenceContext newInstance(URI uri) throws IOException, SQLException;
    
    void initDatabaseData(PersistenceUnitContext puContext) throws IOException, SQLException;

    boolean isVirgin(PersistenceUnitContext puContext) throws SQLException;

    void validate(PersistenceUnitContext puContext) throws SQLException;
}
