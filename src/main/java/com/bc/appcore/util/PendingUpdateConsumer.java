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

package com.bc.appcore.util;

import com.bc.jpa.context.PersistenceUnitContext;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 8, 2017 7:54:29 PM
 */
public class PendingUpdateConsumer implements Consumer<PendingDatabaseUpdate> {
    
    private final PersistenceUnitContext puContext;

    public PendingUpdateConsumer(PersistenceUnitContext puContext) {
        this.puContext = Objects.requireNonNull(puContext);
    }

    @Override
    public void accept(PendingDatabaseUpdate pendingUpdate) {
        
        final PendingDatabaseUpdate.UpdateType updateType = pendingUpdate.getUpdateType();
        switch(updateType) {
            case PERSIST: 
                puContext.getDao().begin().persistAndClose(pendingUpdate.getEntity());
                break;
            case MERGE:
                puContext.getDao().begin().mergeAndClose(pendingUpdate.getEntity());
                break;
            case REMOVE:
                puContext.getDao().begin().removeAndClose(pendingUpdate.getEntity());
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
