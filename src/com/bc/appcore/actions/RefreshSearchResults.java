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

import com.bc.appcore.AppCore;
import com.bc.appcore.exceptions.TaskExecutionException;
import com.bc.appcore.parameter.ParameterException;
import com.bc.appcore.parameter.ParameterExtractor;
import com.bc.jpa.JpaContext;
import com.bc.jpa.search.SearchResults;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 19, 2017 4:39:27 PM
 */
public class RefreshSearchResults implements Action<AppCore, Boolean> {

    @Override
    public Boolean execute(AppCore app, Map<String, Object> params) 
            throws TaskExecutionException, ParameterException {

        final JpaContext jpaContext = app.getJpaContext();
        
        final ParameterExtractor pe = app.getOrException(ParameterExtractor.class);
        
        final SearchResults searchResults = pe.getFirstValue(params, SearchResults.class);
        
        String persistenceUnit = pe.getFirstValue(params, String.class, null);
        
        if(persistenceUnit == null) {
            
            final Class entityType = pe.getFirstValue(params, Class.class, null);
            
            if(entityType != null) {
                
                persistenceUnit = jpaContext.getMetaData().getPersistenceUnitName(entityType);
            }
        }
        
        return this.execute(jpaContext, persistenceUnit, searchResults);
    }
    
    public Boolean execute(JpaContext jpaContext, String target, SearchResults searchResults) {

        final int pageNum = searchResults.getPageNumber();
        try{
            searchResults.reset();
        }finally{
            searchResults.setPageNumber(pageNum);
        }
        
        final String [] puNames = target != null ? new String[]{target} : 
                jpaContext.getMetaData().getPersistenceUnitNames();
        
        for(String persistenceUnit : puNames) {
            
            this.refresh(jpaContext, persistenceUnit);
        }
        
        return Boolean.TRUE;
    }
    
    public void refresh(JpaContext jpaContext, String persistenceUnit) {
        jpaContext.getEntityManagerFactory(persistenceUnit).getCache().evictAll();
    }
}

