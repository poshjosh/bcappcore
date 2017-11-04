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

import com.bc.appcore.ObjectFactory;
import com.bc.appcore.jpa.SelectionContext;
import com.bc.appcore.util.RelationAccess;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.persistence.Entity;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 8, 2017 7:56:54 PM
 */
public class GetRelatedTypesImpl implements GetRelatedTypes {

    private final ObjectFactory objectFactory;

    public GetRelatedTypesImpl(ObjectFactory objFactory) {
        this.objectFactory = Objects.requireNonNull(objFactory);
    }
    
    @Override
    public Set<Class> apply(Class entityType) {
        
        final Predicate<Class> isEntityType = (cls) -> cls.getAnnotation(Entity.class) != null;
        final SelectionContext selectionContext = this.objectFactory.getOrException(SelectionContext.class);
        final Predicate<Class> isNotSelectionType = (cls) -> !selectionContext.isSelectionType(cls);
        
        final RelationAccess relationAccess = objectFactory.getOrException(RelationAccess.class);
        
        final Set<Class> relatedTypes = relationAccess.getChildTypes(entityType, isEntityType.and(isNotSelectionType));
        
        return relatedTypes;
    }
}
