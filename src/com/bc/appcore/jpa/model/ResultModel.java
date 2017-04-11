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

package com.bc.appcore.jpa.model;

import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 14, 2017 8:20:41 PM
 */
public interface ResultModel<T> {
    
    Class<T> getType();
    
    int getSerialColumnIndex();
    
    Object get(T entity, int rowIndex, int columnIndex);
    
    Object get(T entity, int rowIndex, String columnName);
    
    Object set(T entity, int rowIndex, int columnIndex, Object value);
    
    Object set(T entity, int rowIndex, String columnName, Object value);
    
    boolean isCellEditable(int rowIndex, int columnIndex);
    
    Class getColumnClass(int columnIndex);
    
    Set<String> getColumnNames();
    
    String getColumnName(int columnIndex);
    
    Set<String> getColumnLabels();
    
    String getColumnLabel(int columnIndex);
}
