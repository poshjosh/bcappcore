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

import java.util.Objects;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 10:28:47 AM
 */
public class EntityManagerWithUpdateListener extends EntityManagerDecorator {

    private final EntityManagerUpdateListener emUpdateListener;
    
    public EntityManagerWithUpdateListener(EntityManager em, EntityManagerUpdateListener emUpdateListener) {
        super(em);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    @Override
    public void remove(Object entity) {
        
        this.emUpdateListener.preRemove(entity);
        super.remove(entity);
        this.emUpdateListener.postRemove(entity);
        
    }

    @Override
    public <T> T merge(T entity) {
        
        this.emUpdateListener.preMerge(entity);
        final T output = super.merge(entity);
        this.emUpdateListener.postMerge(entity);
        
        return output;
    }

    @Override
    public void persist(Object entity) {
        
        this.emUpdateListener.prePersist(entity);
        super.persist(entity);
        this.emUpdateListener.postPersist(entity);
    }

    @Override
    public EntityTransactionWithUpdateListener getTransaction() {
        
        return new EntityTransactionWithUpdateListener(super.getEntityManager().getTransaction(), emUpdateListener);
    }
}
