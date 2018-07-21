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
import com.bc.jpa.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceContextEclipselinkOptimized;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.predicates.DatabaseCommunicationsFailureTest;
import com.bc.sql.MySQLDateTimePatterns;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 19, 2017 3:19:18 AM
 */
public class PersistenceContextManagerImpl implements PersistenceContextManager {

    private static final Logger logger = Logger.getLogger(PersistenceContextManagerImpl.class.getName());
    
    public PersistenceContextManagerImpl() {  }
    
    @Override
    public PersistenceContext create(URI uri, int maxTrials, boolean freshInstall) 
            throws URISyntaxException {
        
        PersistenceContext persistenceContext = null;
        
        int trials = 0;
        
        while(trials++ < maxTrials) {
            try{
                
                persistenceContext = this.newInstance(uri);

                final Set<String> puNames = persistenceContext.getMetaData(false).getPersistenceUnitNames();
                
                for(String puName : puNames) {
                    
                    PersistenceUnitContext puContext = persistenceContext.getContext(puName);
                    
                    this.init(puContext, freshInstall);
                }
                
                break;
                
            }catch(RuntimeException | IOException | SQLException e) {
                
                if(new DatabaseCommunicationsFailureTest().test(e)) {
                    throw new UserRuntimeException("Internet connection failed");
                }else{
                    logger.log(Level.WARNING, "Exception creating instance of " + 
                            PersistenceContext.class.getName(), e);
                }
            }
        }
        
        Objects.requireNonNull(persistenceContext, 
                PersistenceUnitContext.class.getSimpleName() + " must not be NULL");
        
        return persistenceContext;
    }
    
    @Override
    public PersistenceContext newInstance(URI uri) {
        final PersistenceContext jpaContext = new PersistenceContextEclipselinkOptimized(
                uri, 
                new EntityManagerFactoryCreatorImpl(uri, this.getPropertiesProvider()), 
                new MySQLDateTimePatterns()
        );
        return jpaContext;
    }
    
    public Function<String, Properties> getPropertiesProvider() {
        return (persistenceUnit) -> new Properties();
    }
    
    public final void init(PersistenceUnitContext puContext, boolean freshInstall) 
            throws SQLException, IOException {
        
        logger.entering(this.getClass().getName(), "init(PersistenceUnitContext)", puContext);
        
        if(freshInstall) {
            
            if(this.isVirgin(puContext)) {

                this.initDatabaseData(puContext);
            }
        }

        if(!puContext.isMetaDataLoaded()) {
            
            puContext.loadMetaData();
        }
        
        if(freshInstall) {
        
            this.validate(puContext);
        }
    }
    
    @Override
    public boolean isVirgin(PersistenceUnitContext puContext) throws SQLException {
            
        logger.entering(this.getClass().getName(), "isVirgin(PersistenceUnitContext)", puContext.getName());
        
        final List<String> existingListedTables = puContext.getMetaData(false)
                .fetchExistingListedTables(puContext.getPersistenceContext());
        
        final boolean virgin = existingListedTables.isEmpty();

        logger.fine(() -> "Virgin: "+virgin+", persistence unit; " + puContext.getPersistenceUnitName());
        
        return virgin;
    }
    
    @Override
    public void initDatabaseData(PersistenceUnitContext puContext) 
            throws IOException, SQLException { 
    }

    @Override
    public void validate(PersistenceUnitContext puContext) throws SQLException { 
        
        logger.fine(() -> "Validating persistence unit: " + puContext.getName());
        
        if(!puContext.isMetaDataLoaded()) {
            puContext.loadMetaData();
        }
        
        final String persistenceUnit = puContext.getPersistenceUnitName();
        
        final Set<Class> classes = puContext.getMetaData().getEntityClasses();
        
        for(Class cls : classes) {
            
            final Number count = puContext.getDao().forSelect(Number.class)
                    .from(cls).count().getSingleResultAndClose();
            logger.finer(() -> "Count: " + count + ", persistence unit: " + 
                    persistenceUnit + ", entity type: " + cls.getName());
//            jpaContext.getDaoForSelect(cls).from(cls).createQuery()
//                    .setFirstResult(0).setMaxResults(1).getResultList();
        }
    }
}
