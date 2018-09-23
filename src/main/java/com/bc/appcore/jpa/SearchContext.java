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

import com.bc.jpa.dao.SelectDao;
import com.bc.jpa.dao.search.SearchResults;
import java.util.function.Function;
import javax.persistence.Query;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 14, 2017 8:36:55 PM
 * @param <T> The Type of entity
 */
public interface SearchContext<T> {
    
    Class<T> getResultType();
           
    default String getPaginationMessage(SearchResults<T> searchResults, int numberOfPages) {
        return this.getPaginationMessage(searchResults, numberOfPages, true, false);
    }
    
    String getPaginationMessage(SearchResults<T> searchResults, int numberOfPages, 
            boolean forward, boolean firstElementZero);
    
    default SearchResults<T> searchAll() {
        return this.searchAll((query) -> query);
    }

    default SearchResults<T> searchAll(Function<Query, Query> queryFormatter) {
        return this.getSearchResults(this.getSelectDao(), queryFormatter);
    }

    default SearchResults<T> searchAll(String textToFind) {
        return this.searchAll(textToFind, (query) -> query);
    }
    
    SearchResults<T> searchAll(String textToFind, Function<Query, Query> queryFormatter);

    default SearchResults<T> executeNativeQuery(String sql) {
        return this.executeNativeQuery(sql, (query) -> query);
    }
    
    SearchResults<T> executeNativeQuery(String sql, Function<Query, Query> queryFormatter);

    SelectDao<T> getSelectDao();
    
    default SearchResults<T> getSearchResults(SelectDao<T> dao) {
        return this.getSearchResults(dao, (query) -> query);
    }

    SearchResults<T> getSearchResults(SelectDao<T> dao, Function<Query, Query> queryFormatter);
}
