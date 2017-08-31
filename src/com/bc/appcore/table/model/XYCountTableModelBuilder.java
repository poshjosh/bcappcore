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

import com.bc.appcore.jpa.model.ResultModel;
import com.bc.jpa.search.SearchResults;
import com.bc.appcore.AppCore;

/**
 * @author Chinomso Bassey Ikwuagwu on May 20, 2017 3:56:07 PM
 */
public interface XYCountTableModelBuilder {

    XYCountTableModelBuilder app(AppCore app);

    XYCountTableModel build();

    boolean isBuilt();
    
    XYCountTableModelBuilder displayFormat(TableModelDisplayFormat displayFormat);

    XYCountTableModelBuilder resultModel(ResultModel resultModel);

    XYCountTableModelBuilder searchResults(SearchResults searchResults);

    XYCountTableModelBuilder useCache(boolean useCache);
    
    XYCountTableModelBuilder xEntityType(Class xEntityType);
    
    XYCountTableModelBuilder yEntityType(Class yEntityType);
    
    XYCountTableModelBuilder sumRowName(String name);
    
    XYCountTableModelBuilder sumColumnName(String name);
    
    XYCountTableMetaData getMetaData();
}
