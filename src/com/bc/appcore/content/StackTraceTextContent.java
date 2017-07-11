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

package com.bc.appcore.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on May 5, 2017 12:31:28 PM
 */
public class StackTraceTextContent implements TextContent {
    
    private final String contentType;
    private final String content;

    public StackTraceTextContent(String message, Throwable t) {
        
        this.contentType = "text/plain";
        
        if(t == null) {
            
            this.content = message == null ? "" : message;
            
        }else{
            final StackTraceElement [] steArr = t.getStackTrace();
            final List<StackTraceElement> steList = Arrays.asList(steArr);
            final List<String> list = new ArrayList(steArr.length);
            steList.stream().map(Object::toString).forEach((s) -> list.add(s));
            final Collector<String, ?, Integer> sizeComputer = Collectors.summingInt(String::length);
            final Integer stackTraceSize = list.stream().collect(sizeComputer); 
            final int msgSize = message == null ? 0 : message.length();
            final String sval = t.toString();
            final int tSize = sval.length();
            final String NEW_LINE = System.getProperty("line.separator");
            final int newLineSize = (steArr.length + 2) * NEW_LINE.length();
            final int extra = 20;
            final int totalSize = stackTraceSize + msgSize + tSize + newLineSize + extra; 
            final StringBuilder builder = new StringBuilder(totalSize);
            if(message != null) {
                builder.append(message).append(NEW_LINE);
            }
            builder.append(sval).append(NEW_LINE);
            list.forEach((s) -> builder.append(s).append(NEW_LINE));
            this.content = builder.toString();
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getContent() {
        return content;
    }
    
    @Override
    public String toString() {
        return this.getContent();
    }
}
