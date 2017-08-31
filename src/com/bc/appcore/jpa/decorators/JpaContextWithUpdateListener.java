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

import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.DaoImpl;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:42:43 AM
 */
public class JpaContextWithUpdateListener extends JpaContextDecorator {

    private final EntityManagerUpdateListener emUpdateListener;
    
    public JpaContextWithUpdateListener(JpaContext jpa, EntityManagerUpdateListener emUpdateListener) {
        super(jpa);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    @Override
    public Dao getDao(Class entityType) {
        return new DaoImpl(this.getEntityManager(entityType), this.getDatabaseFormat());
    }

    @Override
    public EntityManager getEntityManager(Class entityClass) {
        return this.getEntityManagerFactory(entityClass).createEntityManager();
    }

    @Override
    public EntityManager getEntityManager(String database) {
        final String persistenceUnit = this.getMetaData().getPersistenceUnitName(database);
        return this.getEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        return new EntityManagerFactoryWithUpdateListener(
                super.getEntityManagerFactory(persistenceUnit), emUpdateListener
        ); 
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(Class entityClass) {
        return new EntityManagerFactoryWithUpdateListener(
                super.getEntityManagerFactory(entityClass), emUpdateListener
        ); 
    }
}
