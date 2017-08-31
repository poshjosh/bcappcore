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
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 10, 2017 10:15:18 PM
 */
public class TableModelFromView extends AbstractTableModel {

    private final JTable table;
    
    public TableModelFromView(JTable table) {
        this.table = Objects.requireNonNull(table);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        table.setValueAt(aValue, rowIndex, columnIndex); 
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return table.isCellEditable(rowIndex, columnIndex); 
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return table.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return table.getColumnName(column); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getRowCount() {
        return table.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return table.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return table.getValueAt(rowIndex, columnIndex);
    }
}
