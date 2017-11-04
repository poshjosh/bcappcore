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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 14, 2017 11:57:05 AM
 */
public class MapTableModel extends AbstractTableModel {

    private static final Logger logger = Logger.getLogger(MapTableModel.class.getName());

    private final List<String> columnNames;
    private final List<Class> columnClasses;
    private final List<Object[]> rows;
    private final String serialColumnName;
    
    public MapTableModel(Collection<Map> rows, String serialColumnName) {
        
        this.serialColumnName = serialColumnName;
        
        final Set<String> columns = new LinkedHashSet();
        if(serialColumnName != null) {
            columns.add(serialColumnName);
        }
        
        for(Map rowMap : rows) {
            final Set rowMapKeys = rowMap.keySet();
            for(Object key : rowMapKeys) {
                columns.add(String.valueOf(key));
            }
        }
        
        this.columnNames = Collections.unmodifiableList(new ArrayList(columns));

        logger.log(Level.FINE, "Column names: {0}", columnNames);
        
        this.rows = new ArrayList(rows.size());
        
        final Function<Map, Object[]> mapValuesToArray = (map) -> {
            
                final Object [] rowData = new Object[columnNames.size()];
                for(int column=0; column<rowData.length; column++) {
                    final String columnName = columnNames.get(column);
                    rowData[column] = map.get(columnName);
                }
                
                return rowData;
        };        

        final Class [] classes = new Class[this.columnNames.size()];
        
        int updatedClasses = 0;
        
        if(this.serialColumnName != null) {
            classes[0] = String.class;
            ++updatedClasses;
        }
        
        for(Map rowMap : rows) {
            
            final Object [] row = mapValuesToArray.apply(rowMap);
            
            logger.finer(() -> "\tRow" + Arrays.toString(row)); 

            this.rows.add(row);
            
            if(updatedClasses < this.columnNames.size()) {

                for(int i=0; i<row.length; i++) {
                
                    final Object val = row[i];
                    if(classes[i] == null && val != null) {
                        classes[i] = val.getClass();
                        ++updatedClasses;
                    }
                }
            }
        }
        for(int i=0; i<classes.length; i++) {
            if(classes[i] == null) {
                classes[i] = Object.class;
            }
        }
        this.columnClasses = Arrays.asList(classes);
        
        logger.log(Level.FINE, "Column classes: {0}", this.columnClasses);
        
        logger.log(Level.FINE, "Row count: {0}", rows.size());
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return this.rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final Object value;
        if(this.serialColumnName != null) {
            if(columnIndex == 0) {
                value = rowIndex + 1;
            }else{
                final Object[] row = this.rows.get(rowIndex);
                value = row[columnIndex-1];
            }
        }else{
            final Object[] row = this.rows.get(rowIndex);
            value = row[columnIndex];
        }
        return value;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(this.serialColumnName != null) {
            if(columnIndex == 0) {
                
            }else{
                final Object[] row = this.rows.get(rowIndex);
                row[columnIndex-1] = aValue;
                this.onSetValueAt(aValue, rowIndex, columnIndex);
            }
        }else{
            final Object[] row = this.rows.get(rowIndex);
            row[columnIndex] = aValue;
            this.onSetValueAt(aValue, rowIndex, columnIndex);
        }
    }
    
    protected void onSetValueAt(Object aValue, int rowIndex, int columnIndex) { }
    
    public final int getColumnIndex(String columnName) {
        return columnNames.indexOf(columnName);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return this.serialColumnName == null || columnIndex != 0;
    }
}
