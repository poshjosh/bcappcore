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

import com.bc.appcore.jpa.model.ColumnLabelProvider;
import com.bc.appcore.jpa.model.ResultModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.table.TableModel;

/**
 * @author Chinomso Bassey Ikwuagwu on May 21, 2017 12:05:39 AM
 */
public class TableModelResultModel<T> implements ResultModel<T> {

    private final TableModel tableModel;
    
    private final Class<T> entityType;
    
    private final int serialColumnIndex;
    
    private final List<String> columnNames;
    
    private final List<String> columnLabels;

    public TableModelResultModel(
            TableModel tableModel, Class<T> entityType) {
        this(tableModel, entityType, ColumnLabelProvider.RETURN_COLUMN_NAME, 0);
    }
    
    public TableModelResultModel(
            TableModel tableModel, Class<T> entityType, 
            ColumnLabelProvider columnLabelProvider, int serialColumnIndex) {
        this.tableModel = Objects.requireNonNull(tableModel);
        this.entityType = Objects.requireNonNull(entityType);
        this.serialColumnIndex = serialColumnIndex;
        this.columnNames = new ArrayList(tableModel.getColumnCount());
        this.columnLabels = new ArrayList(tableModel.getColumnCount());
        for(int col=0; col<tableModel.getColumnCount(); col++) {
            final String name = tableModel.getColumnName(col);
            this.columnNames.add(name);
            this.columnLabels.add(columnLabelProvider.getColumnLabel(entityType, name));
        }
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public int getSerialColumnIndex() {
        return this.serialColumnIndex;
    }

    @Override
    public Object get(T entity, int rowIndex, int columnIndex) {
        return this.tableModel.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public Object get(T entity, int rowIndex, String columnName) {
        return this.get(entity, rowIndex, this.columnNames.indexOf(columnName));
    }

    @Override
    public Object set(T entity, int rowIndex, int columnIndex, Object value) {
        final Object oldValue = this.get(entity, rowIndex, columnIndex);
        this.tableModel.setValueAt(value, rowIndex, columnIndex);
        return oldValue;
    }

    @Override
    public Object set(T entity, int rowIndex, String columnName, Object value) {
        final int columnIndex = this.columnNames.indexOf(columnName);
        final Object oldValue = this.get(entity, rowIndex, columnIndex);
        this.tableModel.setValueAt(value, rowIndex, columnIndex);
        return oldValue;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return this.tableModel.getColumnClass(columnIndex);
    }

    @Override
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(this.columnNames);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public List<String> getColumnLabels() {
        return Collections.unmodifiableList(this.columnLabels);
    }

    @Override
    public String getColumnLabel(int columnIndex) {
        return this.columnLabels.get(columnIndex);
    }
}
