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
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2017 6:03:41 AM
 */
public class EntityManagerUpdateListenerImpl implements EntityManagerUpdateListener{

    @Override
    public void preMerge(Object entity) { }

    @Override
    public void postMerge(Object entity) { }

    @Override
    public void onMergeException(Object entity, Exception e) { }

    @Override
    public void preRemove(Object entity) { }

    @Override
    public void postRemove(Object entity) { }

    @Override
    public void onRemoveException(Object entity, Exception e) { }

    @Override
    public void prePersist(Object entity) { }

    @Override
    public void postPersist(Object entity) { }

    @Override
    public void onPersistException(Object entity, Exception e) { }

    @Override
    public void preCommit() { }

    @Override
    public void postCommit() { }

    @Override
    public void onCommitException(Exception e) { }
}
