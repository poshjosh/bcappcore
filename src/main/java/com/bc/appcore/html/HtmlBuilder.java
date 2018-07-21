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

package com.bc.appcore.html;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 11, 2017 2:16:22 AM
 */
public interface HtmlBuilder<T> {
    
    HtmlBuilder NO_OP = new HtmlBuilder() {
        @Override
        public HtmlBuilder with(Object t) { return this; }
        @Override
        public String build() { return ""; }
    };
    
    HtmlBuilder<T> with(T t);
    
    String build();
}
