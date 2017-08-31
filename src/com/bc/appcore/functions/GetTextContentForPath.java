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

package com.bc.appcore.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 24, 2017 9:04:53 AM
 */
public class GetTextContentForPath {

    public StringBuilder getSingle(Path path, String charsetName) throws IOException {
        final InputStream in = new GetInputStreamsForPath().getSingle(path);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(in, charsetName))) {
            final String NL = System.getProperty("line.separator");
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                builder.append(line).append(NL);
            }
            return builder;
        }
    }
}
