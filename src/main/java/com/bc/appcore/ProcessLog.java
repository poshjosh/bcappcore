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

package com.bc.appcore;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 10, 2017 11:16:33 AM
 */
public interface ProcessLog {
    
    void destroy();

    void init();

    default void log(Object msg) {
        if(msg instanceof Throwable) {
            log((Throwable)msg, "Problem Encountered");
        }else{
            log(String.valueOf(msg));
        }
    }
    
    void log(Throwable t, String msg);
    
    void log(String msg);
}
