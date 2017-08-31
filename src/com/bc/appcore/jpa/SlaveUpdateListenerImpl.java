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

package com.bc.appcore.jpa;

import com.bc.jpa.sync.impl.SlaveUpdateListener;
import java.util.Collections;
import com.bc.appcore.AppCore;
import com.bc.jpa.sync.MasterSlaveTypes;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 29, 2017 11:55:06 AM
 */
public class SlaveUpdateListenerImpl extends SlaveUpdateListener {

    public static AppCore app;

    public SlaveUpdateListenerImpl() {
        super(
                app == null ? null : app.getPendingSlaveUpdatesManager(), 
                app == null ? Collections.EMPTY_LIST : app.getOrException(MasterSlaveTypes.class).getMasterTypes());
    }
}
