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

import com.bc.appcore.functions.GetTextContentForPath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 20, 2017 6:50:42 PM
 */
public class Copy {

    private static final Logger logger = Logger.getLogger(Copy.class.getName());
    
    public Copy() { }

    public Path copy(Path source, Path target, String charsetName, boolean append) 
            throws IOException {
        
        logger.fine(() -> "Copying: " + source + ", to: " + target);

        final StandardOpenOption append_option = append ? 
                StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
        
        final StringBuilder sourceContents = new GetTextContentForPath().getSingle(source, charsetName);
        
        return Files.write(target, sourceContents.toString().getBytes(), 
                StandardOpenOption.CREATE, 
                append_option, 
                StandardOpenOption.WRITE);
    }
}
