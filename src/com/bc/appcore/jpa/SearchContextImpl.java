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
import com.bc.jpa.search.BaseSearchResults;
import com.bc.jpa.search.SearchResults;
import com.bc.util.BatchUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.bc.jpa.dao.SelectImpl;
import com.bc.jpa.search.QuerySearchResults;
import java.util.Objects;
import com.bc.appcore.AppCore;
import com.bc.appcore.jpa.model.EntityResultModel;
import com.bc.jpa.search.ListSearchResults;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import com.bc.jpa.dao.Select;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 20, 2017 8:05:21 PM
 */
public class SearchContextImpl<T> implements SearchContext<T>, Serializable {

    private transient static final Logger logger = Logger.getLogger(SearchContextImpl.class.getName());
    
    private static class AutoCloseableQuerySearchResults 
            extends QuerySearchResults implements AutoCloseable {
        private final EntityManager em;
        public AutoCloseableQuerySearchResults(EntityManager em, Query query, int batchSize, boolean useCache) {
            super(query, batchSize, useCache);
            this.em = Objects.requireNonNull(em);
        }
        @Override
        public void close() {
            if(em.isOpen()) {
                em.close();
            }
        }
    }

    private final AppCore app;
    
    private final EntityResultModel<T> resultModel;
    
    private final int pageSize;
    
    private final boolean useCache;

    public SearchContextImpl(AppCore app, EntityResultModel<T> resultModel, int pageSize, boolean useCache) {
        this.app = Objects.requireNonNull(app);
        this.resultModel = Objects.requireNonNull(resultModel);
        this.pageSize = pageSize;
        this.useCache = useCache;
    }

    @Override
    public Class<T> getResultType() {
        return this.resultModel.getEntityType();
    }

    @Override
    public String getPaginationMessage(SearchResults<T> searchResults, int numberOfPages, boolean forward, boolean firstElementZero) {
        final int PAGESIZE = searchResults.getPageSize();
        final int SIZE = searchResults.getSize();
        final int currentPage = searchResults.getPageNumber();
        final int start = BatchUtils.getStart(currentPage, PAGESIZE, SIZE, forward, firstElementZero);
        final int end = BatchUtils.getEnd((currentPage + numberOfPages) - 1, PAGESIZE, SIZE, forward, firstElementZero);
        final StringBuilder builder = new StringBuilder();
        final String RESULTS = SIZE == 1 ? " result" : " results";
        if(SIZE <= PAGESIZE) {
            builder.append(end).append(RESULTS);
        }else{
            builder.append(start).append(" to ").append(end).append(" of ").append(SIZE).append(RESULTS);
        }
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Pagination Message: {0}", builder);
        return builder.toString();
    }

    @Override
    public EntityResultModel<T> getResultModel() {
        return resultModel;
    }

    @Override
    public SearchResults<T> searchAll(String textToFind, Function<Query, Query> queryFormatter) {
        final List<T> found = app.getActivePersistenceUnitContext().getTextSearch().search(
                this.getResultType(), textToFind, queryFormatter);
        return new ListSearchResults(found, this.pageSize, this.useCache);
    }
    
    @Override
    public SearchResults<T> executeNativeQuery(String sql, Function<Query, Query> queryFormatter) {
        final Class<T> resultType = this.getResultType();
        final EntityManager em = app.getActivePersistenceUnitContext().getEntityManager();
        final Query query = resultType == null ? em.createNativeQuery(sql) : em.createNativeQuery(sql, resultType);
        queryFormatter.apply(query);
        final SearchResults searchResults = new AutoCloseableQuerySearchResults(
                em, query, this.pageSize, this.useCache);
        return searchResults;
    }

    @Override
    public SelectDao<T> getSelectDao() {
        final Class resultType = this.getResultType();
        Objects.requireNonNull(resultType);
        final Select dao = new SelectImpl(app.getActivePersistenceUnitContext().getEntityManager(), resultType);
        return dao;
    }
    
    @Override
    public SearchResults<T> getSearchResults(SelectDao<T> dao, 
            Function<Query, Query> queryFormatter) {
        final BaseSearchResults searchResults = new BaseSearchResults(dao, this.pageSize, this.useCache);
        logger.log(Level.FINE, "Formatting Query with: {0}", queryFormatter);
        queryFormatter.apply(searchResults.getQuery());
        return searchResults;
    }

    public AppCore getApp() {
        return app;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isUseCache() {
        return useCache;
    }
}
