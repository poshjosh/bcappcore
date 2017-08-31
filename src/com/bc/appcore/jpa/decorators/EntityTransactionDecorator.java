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
import javax.persistence.EntityTransaction;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 10:05:38 AM
 */
public class EntityTransactionDecorator implements EntityTransaction {

    private final EntityTransaction t;
    
    public EntityTransactionDecorator(EntityTransaction t) {
        this.t = Objects.requireNonNull(t);
    }

    @Override
    public void begin() {
        t.begin();
    }

    @Override
    public void commit() {
        t.commit();
    }

    @Override
    public void rollback() {
        t.rollback();
    }

    @Override
    public void setRollbackOnly() {
        t.setRollbackOnly();
    }

    @Override
    public boolean getRollbackOnly() {
        return t.getRollbackOnly();
    }

    @Override
    public boolean isActive() {
        return t.isActive();
    }
    
    public EntityTransaction getEntityTransaction() {
        return t;
    }
}
