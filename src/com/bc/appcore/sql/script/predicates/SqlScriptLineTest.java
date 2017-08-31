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

package com.bc.appcore.sql.script.predicates;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 22, 2017 12:52:18 PM
 */
public class SqlScriptLineTest implements Predicate<String> {

    private final String delimiter;
  
    private final boolean fullLineDelimiter;

    public SqlScriptLineTest(boolean fullLineDelimiter) {
        this(";", fullLineDelimiter);
    }
    
    public SqlScriptLineTest(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = Objects.requireNonNull(delimiter);
        this.fullLineDelimiter = fullLineDelimiter;
    }

    @Override
    public boolean test(String line) {
        return !fullLineDelimiter && line.contains(delimiter) || fullLineDelimiter && line.equals(delimiter);
    }
}
