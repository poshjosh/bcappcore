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

package com.bc.appcore.util;

import com.bc.appcore.actions.Action;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 8, 2017 2:58:58 AM
 */
public interface Settings extends Map<String, Object> {
    
    Map<String, Object> getByLabels();
    
    /**
     * As against {@link #putAll(java.util.Map)} this method persists the
     * added data across sessions and application re-launches.
     * @param m 
     * @throws java.io.IOException 
     */
    void updateAll(Map<? extends String, ? extends Object> m) throws IOException;

    /**
     * As against {@link #put(java.lang.Object, java.lang.Object)} this method
     * persists the added data across sessions and application re-launches.
     * @param name
     * @param newValue
     * @return 
     * @throws java.io.IOException 
     */
    Object update(String name, Object newValue) throws IOException;
    
    Class getValueType(String name, Class outputIfNone);
    String getLabel(String name, String outputIfNone);
    String getName(String label, String outputIfNone);
    String getDescription(String name, String outputIfNone);
    String getTypeName(String name, String outputIfNone);
    String getAlias(String name, String outputIfNone);
    List getOptions(String name);
    List<Action> getActions(String name);
}
