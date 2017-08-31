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

package com.bc.appcore.table.model;

import com.bc.appcore.AppCore;
import com.bc.appcore.jpa.model.ResultModel;
import com.bc.appcore.util.RelationAccess;
import com.bc.jpa.JpaContext;
import com.bc.jpa.search.SearchResults;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 20, 2017 3:34:37 PM
 */
public class XYCountTableModelBuilderImpl<X, Y> 
        implements XYCountTableModelBuilder, XYCountTableMetaData<X, Y> {

    private static final Logger logger = Logger.getLogger(XYCountTableModelBuilderImpl.class.getName());

    private AppCore app; 
    private SearchResults searchResults; 
    private ResultModel resultModel;
    private Class xEntityType;
    private Class yEntityType;
    private String sumRowName;
    private String sumColumnName;
    private boolean useCache = true;
    
    private TableModelDisplayFormat tableModelDisplayFormat;
    
    private XYValues<X, Y, Integer> xyCountValues;
    private List<X> xValues;
    private List<Y> yValues;
            
    private boolean built;

    public XYCountTableModelBuilderImpl() {
        this.sumColumnName = this.sumRowName = "Sum";
        this.tableModelDisplayFormat = TableModelDisplayFormat.NUMBER_INSTANCE;
    }

    @Override
    public XYCountTableModel build() {
        
        this.requireBuildNotAttempted(true);
        
        Objects.requireNonNull(this.app);
        Objects.requireNonNull(this.sumColumnName);
        Objects.requireNonNull(this.sumRowName);
        Objects.requireNonNull(this.xEntityType);
        Objects.requireNonNull(this.yEntityType);
        Objects.requireNonNull(this.resultModel);
        Objects.requireNonNull(this.searchResults);
        Objects.requireNonNull(this.tableModelDisplayFormat);
        
        final JpaContext jpaContext = this.app.getJpaContext();
        this.xValues = jpaContext.getBuilderForSelect(this.xEntityType).getResultsAndClose();
        this.yValues = jpaContext.getBuilderForSelect(this.yEntityType).getResultsAndClose();
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "\nX-axis type {0}, values: {1}\nY-axis type: {2}, values: {3}", 
                    new Object[]{xEntityType.getName(), xValues, yEntityType.getName(), yValues}); 
        }

        this.xyCountValues = new EntityXYCountValues(
                this.searchResults.getPages(), this.app.getOrException(RelationAccess.class)
        );

        final XYCountTableModel tableModel = new XYCountTableModel(
                this, this.tableModelDisplayFormat, this.useCache
        ){
            @Override
            public String getColumnName(int column) {
                if(column == 0) {
                    return yEntityType.getSimpleName() + " vs " + xEntityType.getSimpleName();
                }else{
                    return super.getColumnName(column);
                }
            }
        };

        return tableModel;
    }
    
    public List<Object[]> ensure(List<Object[]> list) {
        final List<Object[]> output = new ArrayList(list.size());
        for(Object e : list) {
            if(e instanceof Object[]) {
                output.add((Object[])e);
            }else{
                output.add(new Object[]{e});
            }
        }
        return output;
    }
    
    public void requireBuildNotAttempted(boolean update) {
        if(this.isBuilt()) {
            throw new UnsupportedOperationException("build() method may only be called once");
        }
        this.built = update;
    }
    
    @Override
    public boolean isBuilt() {
        return this.built;
    }

    @Override
    public XYCountTableModelBuilder displayFormat(TableModelDisplayFormat displayFormat) {
        this.tableModelDisplayFormat = displayFormat;
        return this;
    }

    @Override
    public XYCountTableModelBuilder app(AppCore app) {
        this.app = app;
        return this;
    }

    @Override
    public XYCountTableModelBuilder searchResults(SearchResults searchResults) {
        this.searchResults = searchResults;
        return this;
    }

    @Override
    public XYCountTableModelBuilder resultModel(ResultModel resultModel) {
        this.resultModel = resultModel;
        return this;
    }

    @Override
    public XYCountTableModelBuilder useCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    @Override
    public XYCountTableModelBuilder xEntityType(Class xEntityType) {
        this.xEntityType = xEntityType;
        return this;
    }

    @Override
    public XYCountTableModelBuilder yEntityType(Class yEntityType) {
        this.yEntityType = yEntityType;
        return this;
    }

    @Override
    public XYCountTableModelBuilder sumRowName(String name) {
        this.sumRowName = name;
        return this;
    }

    @Override
    public XYCountTableModelBuilder sumColumnName(String name) {
        this.sumColumnName = name;
        return this;
    }

    @Override
    public XYCountTableMetaData<X, Y> getMetaData() {
        return this;
    }

    public AppCore getApp() {
        return app;
    }

    @Override
    public SearchResults getSearchResults() {
        return searchResults;
    }

    @Override
    public ResultModel getResultModel() {
        return resultModel;
    }

    @Override
    public Class getxEntityType() {
        return xEntityType;
    }

    @Override
    public Class getyEntityType() {
        return yEntityType;
    }

    public boolean isUseCache() {
        return useCache;
    }

    @Override
    public XYValues<X, Y, Integer> getXyValues() {
        return xyCountValues;
    }

    @Override
    public List<X> getXValues() {
        return xValues;
    }

    @Override
    public List<Y> getYValues() {
        return yValues;
    }

    @Override
    public String getSumRowName() {
        return this.sumRowName;
    }

    @Override
    public String getSumColumnName() {
        return this.sumColumnName;
    }

    @Override
    public int getSumRowIndex() {
        return this.yValues.size();
    }

    @Override
    public int getSumColumnIndex() {
        return this.xValues.size() + 1;
    }

    @Override
    public int getRowCount() {
        return this.yValues.size() + 1;
    }

    @Override
    public int getColumnCount() {
        return xValues.size() + 2;
    }
}
