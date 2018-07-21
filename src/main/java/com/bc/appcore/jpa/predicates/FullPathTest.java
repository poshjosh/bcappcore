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

package com.bc.appcore.jpa.predicates;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 23, 2017 12:09:15 PM
 */
public class FullPathTest implements Predicate<Path> {

    @Override
    public boolean test(Path path) {
        final boolean output;
        final String uriStr = path.toString().replace('\\', '/');
        int n;
        if((n = uriStr.indexOf("/")) > 1) {
            final String regex = "\\w{1,}:";
            final String toSearch = uriStr.substring(0, n);
            output = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(toSearch).find();
        }else{
            output = false;
        }    
        return output;
    }
}
