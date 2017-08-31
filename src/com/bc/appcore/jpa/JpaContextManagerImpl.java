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

import com.bc.appcore.exceptions.UserRuntimeException;
import com.bc.jpa.JpaContext;
import com.bc.jpa.JpaContextImpl;
import com.bc.jpa.JpaMetaData;
import com.bc.jpa.sync.PendingUpdatesManager;
import com.bc.jpa.sync.predicates.PersistenceCommunicationsLinkFailureTest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 19, 2017 3:19:18 AM
 */
public class JpaContextManagerImpl implements JpaContextManager {

    private static final Logger logger = Logger.getLogger(JpaContextManagerImpl.class.getName());
    
    private final Predicate<String> persistenceUnitTest;
    
    public JpaContextManagerImpl() {
        this((persistenceUnit) -> true);
    }
    
    public JpaContextManagerImpl(Predicate<String> persistenceUnitTest) {
        this.persistenceUnitTest = Objects.requireNonNull(persistenceUnitTest);
    }
    
    @Override
    public JpaContext createJpaContext(URI uri, int maxTrials, boolean freshInstall) 
            throws URISyntaxException {
        
        JpaContext jpaContext = null;
        int trials = 0;
        while(trials++ < maxTrials) {
            try{
                
                jpaContext = this.createJpaContext(uri);

                if(freshInstall && this.isVirgin(jpaContext)) {
                    
                    this.importInitialData(jpaContext);
                }
                
                this.validateJpaContext(jpaContext);
                
                break;
                
            }catch(IOException | SQLException e) {
                
                if(new PersistenceCommunicationsLinkFailureTest().test(e)) {
                    throw new UserRuntimeException("Internet connection failed");
                }
                
                logger.log(Level.WARNING, "JpaContext Exception", e);
            }
        }
        
        Objects.requireNonNull(jpaContext, "JpaContext must not be NULL");
        
        return jpaContext;
    }
    
    @Override
    public boolean isVirgin(JpaContext jpaContext) throws SQLException {
        boolean virgin = true;
        final String [] puNames = jpaContext.getMetaData().getPersistenceUnitNames();
        for(String puName : puNames) {
            if(!this.persistenceUnitTest.test(puName)) {
                continue;
            }
            if( !(virgin = this.isVirgin(jpaContext, puName)) ) {
                break;
            } 
        }
        return virgin;
    }
     
    @Override
    public boolean isVirgin(JpaContext jpaContext, String persistenceUnit) 
            throws SQLException {
            
        final JpaMetaData metaData = jpaContext.getMetaData();

        final boolean virgin = !metaData.isAnyTableExisting(persistenceUnit);

        logger.fine(() -> "Virgin: "+virgin+", persistence unit; " + persistenceUnit);
        
        return virgin;
    }
    
    @Override
    public JpaContext createJpaContext(URI uri) throws IOException {
        
        final JpaContext jpaContext = new JpaContextImpl(uri, null);
        
        return jpaContext;
    }
    
    @Override
    public void importInitialData(JpaContext jpaContext) throws IOException, SQLException { }
    
    @Override
    public void validateJpaContext(JpaContext jpaContext) { }
    
    @Override
    public JpaContext configureJpaContext(JpaContext jpaContext, PendingUpdatesManager pum) { 
        return jpaContext;
    }

    public Predicate<String> getPersistenceUnitTest() {
        return persistenceUnitTest;
    }
}
