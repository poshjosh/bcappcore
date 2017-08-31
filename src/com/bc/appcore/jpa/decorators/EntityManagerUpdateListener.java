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

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2017 5:53:10 AM
 */
public interface EntityManagerUpdateListener {

    void preMerge(Object entity);
    void postMerge(Object entity);
    void onMergeException(Object entity, Exception e);
    
    void preRemove(Object entity);
    void postRemove(Object entity);
    void onRemoveException(Object entity, Exception e);
    
    void prePersist(Object entity);
    void postPersist(Object entity);
    void onPersistException(Object entity, Exception e);
    
    void preCommit();
    void postCommit();
    void onCommitException(Exception e);
}
