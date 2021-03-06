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
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2017 7:07:37 PM
 */
public class EntityTransactionWithUpdateListener extends EntityTransactionDecorator {

    private final EntityManagerUpdateListener emUpdateListener;
    
    public EntityTransactionWithUpdateListener(EntityTransaction t, EntityManagerUpdateListener emUpdateListener) {
        super(t);
        this.emUpdateListener = Objects.requireNonNull(emUpdateListener);
    }

    @Override
    public void commit() {
        try{
            
            this.emUpdateListener.preCommit();
            
            super.commit(); 
            
            this.emUpdateListener.postCommit();
            
        }catch(Exception e) {
            
            this.emUpdateListener.onCommitException(e);
        }
    }
}
