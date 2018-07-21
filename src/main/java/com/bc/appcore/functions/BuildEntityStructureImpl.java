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

import com.bc.appcore.AppContext;
import com.bc.appcore.jpa.EntityStructureFactory;
import com.bc.appcore.ObjectFactory;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.jpa.EntityMemberAccess;
import com.bc.util.JsonFormat;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 8, 2017 7:11:50 PM
 */
public class BuildEntityStructureImpl implements BuildEntityStructure {

    private static final Logger logger = Logger.getLogger(BuildEntityStructureImpl.class.getName());

    private final AppContext context;
    private final ObjectFactory objFactory;

    public BuildEntityStructureImpl(AppContext context, ObjectFactory objectFactory) {
        this.context = Objects.requireNonNull(context);
        this.objFactory = Objects.requireNonNull(objectFactory);
    }
    
    @Override
    public Map apply(Class entityType, Object entity) {
        
        if(Map.class.isAssignableFrom(entityType)) {
            throw new IllegalArgumentException();
        }
        
        final Map structure;
        
        final EntityStructureFactory esf = this.objFactory.getOrException(EntityStructureFactory.class);
        
        if(entity == null) {
            
            structure = esf.getNested(entityType);
            
        }else{

            final EntityMemberAccess updater = this.context
                    .getPersistenceContextSwitch()
                    .getActive().getEntityMemberAccess(entityType);

            final boolean existingEntity = updater.getId(entity) != null;

            final boolean nullsAllowed = !existingEntity;

            structure = esf.get(entity, nullsAllowed, nullsAllowed);
        }
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Entity type: {0}, Structure:\n{1}", 
                    new Object[]{entityType, new JsonFormat(true, true, "  ").toJSONString(structure)});
        }
        
        return structure;
    }
}
