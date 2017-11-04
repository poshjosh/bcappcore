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

import com.bc.jpa.EntityReference;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.DatabaseFormat;
import com.bc.jpa.metadata.PersistenceMetaData;
import com.bc.jpa.search.TextSearch;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:38:06 AM
 */
public class PersistenceContextDecorator implements PersistenceContext {

    private final PersistenceContext jpa;

    public PersistenceContextDecorator(PersistenceContext jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    public boolean isOpen() {
        return jpa.isOpen();
    }

    @Override
    public void close() {
        jpa.close();
    }

    @Override
    public URI getPersistenceConfigURI() {
        return jpa.getPersistenceConfigURI();
    }

    @Override
    public PersistenceUnitContext getContext(String persistenceUnit) {
        return jpa.getContext(persistenceUnit);
    }

    @Override
    public <R> R executeTransaction(String persistenceUnit, Function<EntityManager, R> transaction) {
        return jpa.executeTransaction(persistenceUnit, transaction);
    }

    @Override
    public <R> R executeTransaction(EntityManager em, Function<EntityManager, R> transaction) {
        return jpa.executeTransaction(em, transaction);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        return jpa.getEntityManagerFactory(persistenceUnit);
    }

    @Override
    public EntityManagerFactory removeEntityManagerFactory(String persistenceUnit, boolean close) {
        return jpa.removeEntityManagerFactory(persistenceUnit, close);
    }

    @Override
    public Dao getDao(String persistenceUnit) {
        return jpa.getDao(persistenceUnit);
    }

    @Override
    public TextSearch getTextSearch(String persistenceUnit) {
        return jpa.getTextSearch(persistenceUnit);
    }

    @Override
    public DatabaseFormat getDatabaseFormat(String persistenceUnit) {
        return jpa.getDatabaseFormat(persistenceUnit);
    }

    @Override
    public EntityManager getEntityManager(String persistenceUnit) {
        return jpa.getEntityManager(persistenceUnit);
    }

    @Override
    public PersistenceMetaData getMetaData(boolean load) {
        return jpa.getMetaData(load);
    }

    @Override
    public EntityReference getEntityReference() {
        return jpa.getEntityReference();
    }

    @Override
    public <E> EntityUpdater<E, Object> getEntityUpdater(String persistenceUnit, Class<E> entityClass) {
        return jpa.getEntityUpdater(persistenceUnit, entityClass);
    }
}
