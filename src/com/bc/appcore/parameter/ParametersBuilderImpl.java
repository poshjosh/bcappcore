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

import java.util.Collections;
import java.util.Map;
import com.bc.appcore.AppCore;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 29, 2017 3:58:41 PM
 */
public class ParametersBuilderImpl<S> implements ParametersBuilder<S> {

    private AppCore app;
    
    private S parametersSource;
    
    @Override
    public ParametersBuilder<S> context(AppCore app) {
        this.app = app;
        return this;
    }

    @Override
    public ParametersBuilder<S> with(S parametersSource) {
        this.parametersSource = parametersSource;
        return this;
    }

    @Override
    public Map<String, Object> build() {
        Map<String, Object> params = Collections.singletonMap(this.parametersSource.getClass().getName(), this.parametersSource);
        return params;
    }
}
