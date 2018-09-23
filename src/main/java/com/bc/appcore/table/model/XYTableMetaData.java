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

import com.bc.jpa.dao.search.SearchResults;
import java.util.List;
import com.bc.appcore.jpa.model.EntityResultModel;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 8, 2017 3:17:56 PM
 */
public interface XYTableMetaData<X, Y, Z> {

    EntityResultModel getResultModel();

    SearchResults getSearchResults();

    XYValues<X, Y, Z> getXyValues();

    List<X> getXValues();

    Class getxEntityType();

    List<Y> getYValues();

    Class getyEntityType();
    
    int getRowCount();
    
    int getColumnCount();
}
