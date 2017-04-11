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

import com.bc.appcore.AppCore;
import java.util.Collections;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 9, 2017 12:09:03 AM
 * @param <S> The type this class uses to build a Map of parameters
 */
public interface ParametersBuilder<S> {
    
    public static final ParametersBuilder NO_OP = new ParametersBuilder() {
        @Override
        public ParametersBuilder context(AppCore app) {
            return this;
        }
        @Override
        public ParametersBuilder with(Object source) {
            return this;
        }
        @Override
        public Map build() {
            return Collections.EMPTY_MAP;
        }
    };
    
    ParametersBuilder<S> context(AppCore context);
    
    ParametersBuilder<S> with(S source);

    Map<String, Object> build() throws ParameterException;
}
