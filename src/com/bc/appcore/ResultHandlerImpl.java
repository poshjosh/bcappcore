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

import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 12, 2017 11:42:32 AM
 */
public class ResultHandlerImpl<R> implements ResultHandler<R> {

    private static final Logger logger = Logger.getLogger(ResultHandlerImpl.class.getName());
    
    private final String actionName;

    public ResultHandlerImpl() {
        this("Requested Operation");
    }
    
    public ResultHandlerImpl(String actionName) {
        this.actionName = Objects.requireNonNull(actionName);
    }

    @Override
    public void handleSuccess(R result) {
        logMessage(actionName + " successful");
    }

    @Override
    public void handleException(Exception e) {
        logException(e, actionName + " failed");
    }
    
    public void logMessage(String msg) {
        logger.fine(() -> msg);
    }
    
    public void logException(Exception e, String msg) {
        logger.warning(() -> msg);
    }
}
