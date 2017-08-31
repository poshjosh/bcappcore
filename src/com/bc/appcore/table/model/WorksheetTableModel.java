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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import jxl.Cell;
import jxl.Sheet;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 27, 2017 8:35:16 AM
 */
public class WorksheetTableModel extends AbstractTableModel {

    private static final Logger logger = Logger.getLogger(WorksheetTableModel.class.getName());
    
    private int columnCount;
    
    private final List<Cell[]> rows;

    public WorksheetTableModel(Sheet sheet, int offset, int limit) {
        rows = new ArrayList(limit);
        final int end = offset + limit;
        for(int row=offset; row < end; row++) {
            if(row < 0 || row >= sheet.getRows()) {
                break;
            }
            Cell [] cells = sheet.getRow(row);
            if(cells.length < 1) {
                continue;
            }
            if(row == offset) {
                columnCount = cells.length;
            }else{
                columnCount = Math.max(columnCount, cells.length);
            }
            rows.add(cells);
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        final Cell [] cells = rows.get(rowIndex);
        
        final Object value;
        
        if(columnIndex >= cells.length) {

            if(logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "@[{0}:{1}] column is >= row cells length: {1}", 
                        new Object[]{rowIndex, columnIndex, cells.length});
            }
            
            value = null;
            
        }else{
            
            value = cells[columnIndex].getContents();
        }
        
        return value;
    }
}
