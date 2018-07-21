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
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.parameter.ParameterException;
import com.bc.appcore.parameter.ParameterExtractor;
import com.bc.jpa.search.SearchResults;
import com.bc.jpa.search.SingleSearchResult;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 1, 2017 2:38:14 PM
 */
public class Search implements Action<AppCore, SearchResults> {

    private static final Logger logger = Logger.getLogger(Search.class.getName());
    
    @Override
    public SearchResults execute(AppCore app, Map<String, Object> params) 
            throws ParameterException, TaskExecutionException {
        
        logger.log(Level.FINE, "Params: {0}", params);

        final SearchContext searchContext = this.getSearchContext(app, params);
        
        final Class entityType = searchContext.getResultType();
        
        final String textToFind = this.getTextToFind(params, null);
        
        logger.log(Level.FINE, () -> "Result type: "+entityType.getName()+", query: " + textToFind);
        
        final boolean hasQuery = textToFind != null && !textToFind.isEmpty();
        
        SearchResults searchResults;
        
        if(hasQuery) {
            
            try{
                
                final Integer ID = Integer.parseInt(textToFind);
                final Object entity = searchContext.getSelectDao().find(searchContext.getResultType(), ID);
                searchResults = entity == null ? SearchResults.EMPTY_INSTANCE : new SingleSearchResult<>(entity);
                
            }catch(NumberFormatException ignored) {
                
                logger.finer(() -> "Beginning search for " + textToFind);
                searchResults = searchContext.searchAll(textToFind);
                logger.finer(() -> "Completed search for " + textToFind);
            }
        }else{
            logger.finer("Beginning search");
            searchResults = searchContext.searchAll();
            logger.finer("Completed search");
        } 
        
        return searchResults;
    }

    public SearchContext getSearchContext(AppCore app, Map<String, Object> params) {
        final ParameterExtractor pe = app.getOrException(ParameterExtractor.class);
        SearchContext searchContext = pe.getFirstValue(params, SearchContext.class, null);
        if(searchContext == null) {
            final Class entityType = pe.getFirstValue(params, Class.class, app.getDefaultEntityType());
            searchContext = app.getSearchContext(Objects.requireNonNull(entityType));
        }
        Objects.requireNonNull(searchContext);
        return searchContext;
    }
    
//    public String getQuery(Map<String, Object> params, String outputIfNone) {
//        final String textToFind = this.getTextToFind(params, null);
//        return textToFind == null ? outputIfNone : '%' + textToFind + '%';
//    }
    
    public String getTextToFind(Map<String, Object> params, String outputIfNone) {
        final String textToFind = (String)params.get("query");
        return textToFind;
    }
}
