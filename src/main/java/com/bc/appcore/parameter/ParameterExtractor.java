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

package com.bc.appcore.parameter;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 5:36:27 PM
 */
public interface ParameterExtractor {

    <T> T getFirstValue(Map<String, Object> params, Class<T> type) throws ParameterException;
    
    <T> T getFirstValue(Map<String, Object> params, Class<T> type, T outputIfNone);
    
    String getFirstKey(Map<String, Object> params, Predicate<String> keyTest) throws ParameterException;
    
    String getFirstKey(Map<String, Object> params, Predicate<String> keyTest, String outputIfNone);
    
    Object getFirstValue(Map<String, Object> params, Predicate valueTest) throws ParameterException;
    
    Object getFirstValue(Map<String, Object> params, Predicate valueTest, Object outputIfNone);
}
