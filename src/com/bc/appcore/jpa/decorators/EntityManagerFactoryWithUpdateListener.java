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

import com.bc.jpa.sync.PendingUpdatesManager;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 11:43:49 AM
 */
public class EntityManagerFactoryWithUpdateListener extends EntityManagerFactoryDecorator {

    private final EntityManagerUpdateListener emUpdateListener;
    
    public EntityManagerFactoryWithUpdateListener(EntityManagerFactory emf, EntityManagerUpdateListener emUpdateListener) {
        super(emf);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return new EntityManagerWithUpdateListener(
                super.createEntityManager(synchronizationType, map), emUpdateListener
        ); 
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return new EntityManagerWithUpdateListener(
                super.createEntityManager(synchronizationType), emUpdateListener
        ); 
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return new EntityManagerWithUpdateListener(
                super.createEntityManager(map), emUpdateListener
        ); 
    }

    @Override
    public EntityManager createEntityManager() {
        return new EntityManagerWithUpdateListener(
                super.createEntityManager(), emUpdateListener
        ); 
    }
}
