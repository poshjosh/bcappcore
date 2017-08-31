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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import com.bc.appcore.util.Selection;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 3, 2017 12:57:02 PM
 */
public class EntityTableModel <T> extends AbstractTableModel {
    
    private transient final Logger logger = Logger.getLogger(EntityTableModel.class.getName());
    
    private final int serialColumnIndex;
    
    private final ResultModel<T> resultModel;
    
    private final List<T> resultsToDisplay;
    
    public EntityTableModel(List<T> resultsToDisplay, ResultModel<T> resultModel) {
        this.resultsToDisplay = Collections.unmodifiableList(resultsToDisplay);
        this.resultModel = Objects.requireNonNull(resultModel);
        this.serialColumnIndex = resultModel.getSerialColumnIndex();
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.resultModel.getColumnClass(columnIndex);
    }
    
    @Override
    public int getRowCount() {
        return this.resultsToDisplay.size();
    }

    @Override
    public int getColumnCount() {
        try{
            return this.resultModel.getColumnNames().size();
        }catch(RuntimeException e) {
            log(e, "Error getting table column count");
            return 0;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try{
            final Object value;
            if(columnIndex == this.serialColumnIndex) {
                value = (rowIndex + 1);
            }else{
                final T entity = resultsToDisplay.get(rowIndex);
                
                value = this.resultModel.get(entity, rowIndex, columnIndex);
            }
            return value;
        }catch(RuntimeException e) {
            String columnName;
            try{
                columnName = this.getColumnName(columnIndex);
            }catch(Exception ignored) {
                columnName = "(error accessing column)";
            }
            log(e, "Error accessing value at "+this.getResultModel().getEntityType().getSimpleName()
                    +"["+rowIndex+':'+columnIndex+"] with column name: "+columnName);
            return "Error";
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != this.serialColumnIndex;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Setting [{0}:{1}] = {2}", new Object[]{rowIndex, columnIndex, value});
        }
        try{
            
            final T entity = resultsToDisplay.get(rowIndex);
            
            logger.log(Level.FINER, "Entity: {0}", entity);
            
            if(value instanceof Selection) {
                value = ((Selection)value).getValue();
                logger.log(Level.FINE, "Value from selection: {0}", value);
            }
            
            this.resultModel.set(entity, rowIndex, columnIndex, value);

            this.fireTableCellUpdated(rowIndex, columnIndex);
            
        }catch(RuntimeException e) {
            log(e, "Error setting value at ["+rowIndex+':'+columnIndex+"] to: "+value);
        }
    }

    @Override
    public String getColumnName(int column) {
        try{
            return resultModel.getColumnLabel(column);
        }catch(RuntimeException e) {
            log(e, "Error getting name for column at index: "+column);
            return "Error";
        }
    }
    
//    private int logCount = 0;
    protected void log(final Throwable t, final String message) {
//        if(++logCount > 20) {
//            return;
//        }
        logger.log(Level.WARNING, message, t);
    }

    public final ResultModel<T> getResultModel() {
        return resultModel;
    }

    public final List<T> getResultsToDisplay() {
        return resultsToDisplay;
    }
}
