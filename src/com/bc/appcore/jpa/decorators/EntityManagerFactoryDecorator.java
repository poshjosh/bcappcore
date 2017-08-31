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

import java.util.Map;
import java.util.Objects;
import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:40:03 AM
 */
public class EntityManagerFactoryDecorator implements EntityManagerFactory {

    private final EntityManagerFactory emf;

    public EntityManagerFactoryDecorator(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf);
    }

    @Override
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return emf.createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return emf.createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return emf.createEntityManager(synchronizationType, map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return emf.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return emf.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return emf.isOpen();
    }

    @Override
    public void close() {
        emf.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return emf.getProperties();
    }

    @Override
    public Cache getCache() {
        return emf.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return emf.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        emf.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return emf.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        emf.addNamedEntityGraph(graphName, entityGraph);
    }
    
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
