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

package com.bc.appcore.jpa.decorators;

import com.bc.jpa.EntityManagerFactoryCreator;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceContextImpl;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.context.PersistenceUnitContextImpl;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.DaoImpl;
import com.bc.jpa.metadata.PersistenceMetaData;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import com.bc.sql.SQLDateTimePatterns;
import java.net.URI;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:42:43 AM
 */
public class PersistenceContextWithUpdateListener extends PersistenceContextImpl {
    
    private static class PersistenceUnitContextWithUpdateListener extends PersistenceUnitContextImpl {

        private final EntityManagerUpdateListener listener;

        public PersistenceUnitContextWithUpdateListener(
                PersistenceContext persistenceContext, String persistenceUnit, 
                PersistenceUnitMetaData metaData, EntityManagerFactoryCreator emfCreator, 
                SQLDateTimePatterns sqlDateTimePatterns, EntityManagerUpdateListener listener) {
            super(persistenceContext, persistenceUnit, metaData, emfCreator, sqlDateTimePatterns);
            
            this.listener = Objects.requireNonNull(listener);
        }
        
        @Override
        public EntityManagerFactory getEntityManagerFactory() {
            return new EntityManagerFactoryWithUpdateListener(
                    super.getEntityManagerFactory(), this.listener
            ); 
        }
        
        @Override
        public EntityManager getEntityManager() {
            return this.getEntityManagerFactory().createEntityManager();
        }

        @Override
        public Dao getDao() {
            return new DaoImpl(this.getEntityManager(), this.getDatabaseFormat());
        }
    }

    private final EntityManagerUpdateListener emUpdateListener;

    public PersistenceContextWithUpdateListener(EntityManagerUpdateListener emUpdateListener, URI persistenceConfigUri, SQLDateTimePatterns dateTimePatterns) {
        super(persistenceConfigUri, dateTimePatterns);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    public PersistenceContextWithUpdateListener(EntityManagerUpdateListener emUpdateListener, URI persistenceConfigUri, EntityManagerFactoryCreator emfCreator, SQLDateTimePatterns dateTimePatterns) {
        super(persistenceConfigUri, emfCreator, dateTimePatterns);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    public PersistenceContextWithUpdateListener(EntityManagerUpdateListener emUpdateListener, PersistenceMetaData metaData, EntityManagerFactoryCreator emfCreator, SQLDateTimePatterns dateTimePatterns) {
        super(metaData, emfCreator, dateTimePatterns);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    @Override
    public PersistenceUnitContext createPersistenceUnitContext(String persistenceUnit, EntityManagerFactoryCreator emfCreator, SQLDateTimePatterns sqlDateTimePatterns) {

            return new PersistenceUnitContextWithUpdateListener(
                    this, persistenceUnit, 
                    this.getMetaData(false).getMetaData(persistenceUnit), 
                    emfCreator, sqlDateTimePatterns, this.emUpdateListener);
    }
}
