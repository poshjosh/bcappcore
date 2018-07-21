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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 8, 2017 2:02:05 PM
 */
public class XYTableModel<X, Y, Z> extends AbstractTableModel {

    private static final Logger logger = Logger.getLogger(XYTableModel.class.getName());
    
    private final XYTableMetaData<X, Y, Z> tableMetaData;
    
    private final Class<Z> valuesType;
    
    private final boolean useCache;
    
    private final Object[][] cache;
    
    private final TableModelDisplayFormat displayFormat;

    public XYTableModel(final XYTableMetaData<X, Y, Z> tableMetaData, Class<Z> valuesType, 
            TableModelDisplayFormat cellDisplayFormat, boolean useCache) {
        this.tableMetaData = Objects.requireNonNull(tableMetaData);
        this.valuesType = Objects.requireNonNull(valuesType);
        this.useCache = useCache;
        this.cache = new Object[this.getRowCount()][this.getColumnCount()];
        this.displayFormat = Objects.requireNonNull(cellDisplayFormat);
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if(columnIndex == 0) {
            return Object.class;
        }else{
            return valuesType;
        }
    }

    @Override
    public String getColumnName(int column) {
        final String columnName;
        if(column == 0) {
            columnName = "";
        }else{
            final X value = this.tableMetaData.getXValues().get(column-1);
            columnName = this.displayFormat.toDisplayValue(String.class, value, -1, column).toString();
        }
        return columnName;
    }
    
    @Override
    public int getRowCount() {
        return this.tableMetaData.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return this.tableMetaData.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        final Object value;
        
        if(this.useCache) {
            final Object [] cacheRow = this.cache[rowIndex];
            final Object cachedValue = cacheRow[columnIndex];
            if(cachedValue != null) {
                value = cachedValue;
            }else{
                value = this.loadValueAt(rowIndex, columnIndex);
                cacheRow[columnIndex] = value;
            }
        }else{
            value = this.loadValueAt(rowIndex, columnIndex);
        }
        
        return value;
    }

    public Object loadValueAt(int rowIndex, int columnIndex) {
        
        final Object value;
        
        if(columnIndex == 0) {
            
            final Y yValue = this.tableMetaData.getYValues().get(rowIndex);
            value = this.displayFormat.toDisplayValue(
                    this.getColumnClass(columnIndex), yValue, rowIndex, columnIndex);
            
        }else{
            
            final X xValue = this.tableMetaData.getXValues().get(columnIndex-1);
            final Y yValue = this.tableMetaData.getYValues().get(rowIndex);
            
            if(logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "xAxis value: {0}, yAxis value: {1}", 
                        new Object[]{xValue, yValue});
            }
            
            value = this.getValue(xValue, yValue);
        }
        
        return value;
    }
    
    public Z getValue(X xValue, Y yValue) {
        return this.tableMetaData.getXyValues().getValue(xValue, yValue);
    }

    public XYTableMetaData<X, Y, Z> getTableMetaData() {
        return tableMetaData;
    }

    public Class<Z> getValuesType() {
        return valuesType;
    }

    public boolean isUseCache() {
        return useCache;
    }
}
