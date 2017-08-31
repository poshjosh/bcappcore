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

import com.bc.jpa.EntityController;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.JpaContext;
import com.bc.jpa.JpaMetaData;
import com.bc.jpa.dao.BuilderForDelete;
import com.bc.jpa.dao.BuilderForSelect;
import com.bc.jpa.dao.BuilderForUpdate;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.dao.DatabaseFormat;
import com.bc.jpa.fk.EnumReferences;
import com.bc.jpa.search.TextSearch;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinColumn;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:38:06 AM
 */
public class JpaContextDecorator implements JpaContext {

    private final JpaContext jpa;

    public JpaContextDecorator(JpaContext jpa) {
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
    public EntityManagerFactory getEntityManagerFactory(Class entityClass) {
        return jpa.getEntityManagerFactory(entityClass);
    }

    @Override
    public EntityManagerFactory removeEntityManagerFactory(String persistenceUnit, boolean close) {
        return jpa.removeEntityManagerFactory(persistenceUnit, close);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        return jpa.getEntityManagerFactory(persistenceUnit);
    }

    @Override
    public Dao getDao(Class entityType) {
        return jpa.getDao(entityType);
    }

    @Override
    public <T> BuilderForSelect<T> getBuilderForSelect(Class<T> entityAndResultType) {
        return jpa.getBuilderForSelect(entityAndResultType);
    }

    @Override
    public <T> BuilderForSelect<T> getBuilderForSelect(Class entityType, Class<T> resultType) {
        return jpa.getBuilderForSelect(entityType, resultType);
    }

    @Override
    public <T> BuilderForDelete<T> getBuilderForDelete(Class<T> entityType) {
        return jpa.getBuilderForDelete(entityType);
    }

    @Override
    public <T> BuilderForUpdate<T> getBuilderForUpdate(Class<T> entityType) {
        return jpa.getBuilderForUpdate(entityType);
    }

    @Override
    public TextSearch getTextSearch() {
        return jpa.getTextSearch();
    }

    @Override
    public Object getReference(Class referencingClass, String col, Object val) {
        return jpa.getReference(referencingClass, col, val);
    }

    @Override
    public Object getReference(EntityManager em, Class referencingType, Map<JoinColumn, Field> joinCols, String col, Object val) {
        return jpa.getReference(em, referencingType, joinCols, col, val);
    }

    @Override
    public DatabaseFormat getDatabaseFormat() {
        return jpa.getDatabaseFormat();
    }

    @Override
    public Map getDatabaseParameters(Class entityClass, Map params) {
        return jpa.getDatabaseParameters(entityClass, params);
    }

    @Override
    public EntityController getEntityController(String database, String table) {
        return jpa.getEntityController(database, table);
    }

    @Override
    public <E> EntityController<E, Object> getEntityController(Class<E> entityClass) {
        return jpa.getEntityController(entityClass);
    }

    @Override
    public <E, e> EntityController<E, e> getEntityController(Class<E> entityClass, Class<e> idClass) {
        return jpa.getEntityController(entityClass, idClass);
    }

    @Override
    public EntityManager getEntityManager(String database) {
        return jpa.getEntityManager(database);
    }

    @Override
    public EntityManager getEntityManager(Class entityClass) {
        return jpa.getEntityManager(entityClass);
    }

    @Override
    public JpaMetaData getMetaData() {
        return jpa.getMetaData();
    }

    @Override
    public EnumReferences getEnumReferences() {
        return jpa.getEnumReferences();
    }

    @Override
    public <E> EntityUpdater<E, Object> getEntityUpdater(Class<E> entityClass) {
        return jpa.getEntityUpdater(entityClass);
    }

    public JpaContext getJpaContext() {
        return jpa;
    }
}
