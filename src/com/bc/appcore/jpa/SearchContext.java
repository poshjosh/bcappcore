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

package com.bc.appcore.jpa;

import com.bc.appcore.jpa.model.ResultModel;
import com.bc.jpa.dao.SelectDao;
import com.bc.jpa.search.SearchResults;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 14, 2017 8:36:55 PM
 * @param <T> The Type of entity
 */
public interface SearchContext<T> {
    
    String getPaginationMessage(SearchResults<T> searchResults, int numberOfPages, 
            boolean forward, boolean firstElementZero);
    
    ResultModel<T> getResultModel();

    SearchResults<T> getSearchResults(String sql, Class<T> resultType);
    
    SelectDao<T> getSelectDao(Class<T> resultType);
    
    SearchResults<T> getSearchResults(Class<T> resultType);

    SearchResults<T> getSearchResults(SelectDao<T> dao);
}
