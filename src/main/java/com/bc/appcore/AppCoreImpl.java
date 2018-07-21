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

import com.bc.appcore.jpa.SearchContext;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 18, 2017 3:03:28 PM
 */
public class AppCoreImpl extends AbstractAppCore {

    private final Function<AppCore, ObjectFactory> objectFactoryProvider;
    private final BiFunction<AppCore, Class, SearchContext> searchContextProvider;
    private final List<Class> entityTypeOrder;
    private final Class defaultEntityType; 
    private final Class userEntityType;
    
    public AppCoreImpl(
            AppContext appContext, 
            Function<AppCore, ObjectFactory> objectFactoryProvider,
            BiFunction<AppCore, Class, SearchContext> searchContextProvider,
            List<Class> entityTypeOrder,
            Class defaultEntityType, 
            Class userEntityType) {
        super(appContext);
        this.objectFactoryProvider = Objects.requireNonNull(objectFactoryProvider);
        this.searchContextProvider = Objects.requireNonNull(searchContextProvider);
        this.entityTypeOrder = Objects.requireNonNull(entityTypeOrder);
        this.defaultEntityType = Objects.requireNonNull(defaultEntityType);
        this.userEntityType = Objects.requireNonNull(userEntityType);
    }
    
    @Override
    protected ObjectFactory createObjectFactory() {
        return this.objectFactoryProvider.apply(this);
    }

    @Override
    public <T> SearchContext<T> getSearchContext(Class<T> entityType) {
        return this.searchContextProvider.apply(this, entityType);
    }

    @Override
    public Class getUserEntityType() {
        return this.userEntityType;
    }

    @Override
    public List<Class> getEntityTypeOrderList() {
        return this.entityTypeOrder;
    }

    @Override
    public Class getDefaultEntityType() {
        return this.defaultEntityType;
    }
}
