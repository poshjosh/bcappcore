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
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2017 9:39:42 PM
 */
public class DisplayTableModelFromModel implements TableModel {

    private final TableModel delegate;
    
    private final TableModelDisplayFormat displayFormat;

    public DisplayTableModelFromModel(TableModel delegate, TableModelDisplayFormat displayFormat) {
        this.delegate = Objects.requireNonNull(delegate);
        this.displayFormat = Objects.requireNonNull(displayFormat);
    }

    @Override
    public int getRowCount() {
        return delegate.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return delegate.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return delegate.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return delegate.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return delegate.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final Class columnClass = this.getColumnClass(columnIndex);
        final Object value = delegate.getValueAt(rowIndex, columnIndex);
        return this.displayFormat.toDisplayValue(columnClass, value, rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        final Class columnClass = this.getColumnClass(columnIndex);
        aValue = this.displayFormat.fromDisplayValue(columnClass, aValue, rowIndex, columnIndex);
        delegate.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        delegate.addTableModelListener(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        delegate.removeTableModelListener(l);
    }
}
