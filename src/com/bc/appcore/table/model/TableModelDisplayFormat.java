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

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 8, 2017 9:38:36 AM
 */
public interface TableModelDisplayFormat<T> {
    
    TableModelDisplayFormat<String> STRING_INSTANCE = new TableModelDisplayFormat<String>() {
        @Override
        public Object fromDisplayValue(Class columnClass, String displayValue, int row, int column) {
            return displayValue;
        }

        @Override
        public String toDisplayValue(Class columnClass, Object value, int row, int column) {
            return value == null ? null : value.toString();
        }
    };
    
    TableModelDisplayFormat<Number> NUMBER_INSTANCE = new TableModelDisplayFormat<Number>() {
        @Override
        public Object fromDisplayValue(Class columnClass, Number displayValue, int row, int column) {
            return displayValue;
        }
        @Override
        public Number toDisplayValue(Class columnClass, Object value, int row, int column) {
            return value instanceof Number ? (Number)value : value == null ? null : Double.parseDouble(value.toString());
        }
    };
    
    TableModelDisplayFormat<Boolean> BOOLEAN_INSTANCE = new TableModelDisplayFormat<Boolean>() {
        @Override
        public Object fromDisplayValue(Class columnClass, Boolean displayValue, int row, int column) {
            return displayValue;
        }

        @Override
        public Boolean toDisplayValue(Class columnClass, Object value, int row, int column) {
            return value instanceof Boolean ? (Boolean)value : value == null ? null : Boolean.parseBoolean(value.toString());
        }
    };
    
    Object fromDisplayValue(Class columnClass, T displayValue, int row, int column);
    
    T toDisplayValue(Class columnClass, Object value, int row, int column);
}
