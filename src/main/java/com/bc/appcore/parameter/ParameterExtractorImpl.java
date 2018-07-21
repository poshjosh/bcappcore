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

import com.bc.appcore.predicates.IsSubClass;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 5:33:01 PM
 */
public class ParameterExtractorImpl implements ParameterExtractor {

    public ParameterExtractorImpl() { }
    
    @Override
    public <T> T getFirstValue(Map<String, Object> params, Class<T> type) 
            throws ParameterException {
        
        final T value = this.getFirstValue(params, type, null);
        
        if(value == null) {
            throw new ParameterNotFoundException("Parameter with value of type: "+type);
        }
        
        return value;
    }

    @Override
    public <T> T getFirstValue(Map<String, Object> params, Class<T> type, T outputIfNone) {
        
        final IsSubClass isSubClass = new IsSubClass(type);
        
        final Optional optional = params.values().stream().filter((value) -> value != null && isSubClass.test(value.getClass())).findFirst();
        
        final T t;
        if(!optional.isPresent()) {
            t = outputIfNone;
        }else{
            t = (T)optional.get();
        }
        
        return t;
    }

    @Override
    public String getFirstKey(Map<String, Object> params, Predicate<String> keyTest) throws ParameterException{
        
        final String value = this.getFirstKey(params, keyTest, null);
        
        if(value == null) {
            throw new ParameterNotFoundException();
        }
        
        return value;
    }

    @Override
    public String getFirstKey(Map<String, Object> params, Predicate<String> keyTest, String outputIfNone) {
        
        final Optional<String> optional = params.keySet().stream().filter(keyTest).findFirst();
        
        if(!optional.isPresent()) {
            return outputIfNone;
        }else{
            return optional.get();
        }
    }

    @Override
    public Object getFirstValue(Map<String, Object> params, Predicate valueTest) throws ParameterException{
        
        final Object value = this.getFirstValue(params, valueTest, null);
        
        if(value == null) {
            throw new ParameterNotFoundException();
        }
        
        return value;
    }

    @Override
    public Object getFirstValue(Map<String, Object> params, Predicate valueTest, Object outputIfNone) {
        
        final Optional<String> optional = params.values().stream().filter(valueTest).findFirst();
        
        if(!optional.isPresent()) {
            return outputIfNone;
        }else{
            return optional.get();
        }
    }
}
