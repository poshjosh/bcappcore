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

package com.bc.appcore.actions;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2017 10:36:57 AM
 */
public interface ActionCommandsCore {
    
    String SEARCH = Search.class.getName();
    
    String REFRESH_SEARCHRESULTS = RefreshSearchResults.class.getName();

    String DELETE_TEMP_FILES_IN_DIR = DeleteAllTempFilesInDir.class.getName();
    
    String EXIT = Exit.class.getName();

    String SYNC_DATABASE = SyncDatabase.class.getName();
    
    String SYNC_IF_SLAVE_DATABASE_EMPTY = SyncIfSlaveDatabaseEmpty.class.getName();
    
    String WAIT_TILL_ACTION_COMPLETES = com.bc.appcore.actions.WaitTillActionCompletes.class.getName();
}
