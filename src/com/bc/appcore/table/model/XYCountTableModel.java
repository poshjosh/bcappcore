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
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on May 20, 2017 12:29:29 PM
 */
public class XYCountTableModel<X, Y> extends XYTableModel<X, Y, Integer> {
    
    private final ToIntFunction<Object> toInt;
    
    private final Collector<Object, ?, Integer> summingInt;
    
    public XYCountTableModel(final XYCountTableMetaData<X, Y> tableMetaData, 
            TableModelDisplayFormat displayFormat, boolean useCache) {
        super(tableMetaData, Integer.class, displayFormat, useCache);
        this.toInt = (e) -> e instanceof Integer ? (Integer)e : Integer.parseInt(e.toString());
        this.summingInt = Collectors.summingInt(toInt); 
    }

    @Override
    public XYCountTableMetaData<X, Y> getTableMetaData() {
        return (XYCountTableMetaData<X, Y>)super.getTableMetaData();
    }

    @Override
    public String getColumnName(int column) {
        final String columnName;
        if(this.isSumColumn(column)) {
            columnName = this.getTableMetaData().getSumColumnName();
        }else{
            columnName = super.getColumnName(column);
        }
        return columnName;
    }

    @Override
    public Object loadValueAt(int rowIndex, int columnIndex) {
        
        final Object value;
        
        if(columnIndex == 0) {
            
            if(this.isSumRow(rowIndex)) {
                
                value = this.getTableMetaData().getSumRowName();
                
            }else{
                
                value = super.loadValueAt(rowIndex, columnIndex);
            }
        }else if(this.isSumColumn(columnIndex)) { 
            
            if(this.isSumRow(rowIndex)) {
                
                value = null;
                
            }else{
                
                final List rowValues = this.getRowValues(rowIndex, 1, this.getTableMetaData().getSumColumnIndex());
                
                value = this.sum(rowValues);
            }
        }else{
            
            if(this.isSumRow(rowIndex)) {
                
                final List columnValues = this.getColumnValues(columnIndex, 0, this.getTableMetaData().getSumRowIndex());
                
                value = this.sum(columnValues);
                
            }else{
                
                value = super.loadValueAt(rowIndex, columnIndex);
            }
        }
        return value;
    }
    
    public Object sum(List values) {
        final Integer sum = (Integer)values.stream().collect(summingInt);
        return sum;
    }

    public boolean isSumRow(int row) {
        return row == this.getTableMetaData().getSumRowIndex();
    }
    
    public boolean isSumColumn(int col) {
        return col == this.getTableMetaData().getSumColumnIndex();
    }

    public List getRowValues(int row, int startCol, int endCol) {
        final List values = new ArrayList();
        for(int col=startCol; col<endCol; col++) {
            final Object value = this.getValueAt(row, col);
            values.add(value);
        }
        return values;
    }
    
    public List getColumnValues(int column, int startRow, int endRow) {
        final List values = new ArrayList();
        for(int row=startRow; row<endRow; row++) {
            final Object value = this.getValueAt(row, column);
            values.add(value);
        }
        return values;
    }
}
