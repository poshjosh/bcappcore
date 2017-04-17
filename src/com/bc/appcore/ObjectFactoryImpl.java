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

import com.bc.appcore.exceptions.ObjectFactoryException;
import com.bc.appcore.exceptions.ObjectNotSupportedException;
import com.bc.appcore.jpa.EntityMapBuilder;
import com.bc.appcore.jpa.JpaTypeProvider;
import com.bc.appcore.jpa.RecursionFilterImpl;
import com.bc.appcore.jpa.predicates.MasterPersistenceUnitTest;
import com.bc.appcore.parameter.ParametersBuilder;
import com.bc.appcore.parameter.ParametersBuilderImpl;
import com.bc.appcore.predicates.AcceptAll;
import com.bc.appcore.util.LoggingConfigManager;
import com.bc.appcore.util.LoggingConfigManagerImpl;
import com.bc.appcore.util.RawTextHandler;
import com.bc.appcore.util.Settings;
import com.bc.appcore.util.SettingsImpl;
import com.bc.appcore.util.TextHandler;
import com.bc.jpa.JpaContext;
import com.bc.jpa.sync.JpaSync;
import com.bc.jpa.util.EntityFromMapBuilder;
import com.bc.jpa.util.EntityFromMapBuilderImpl;
import com.bc.util.MapBuilder;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 29, 2017 4:24:26 PM
 */
public class ObjectFactoryImpl implements ObjectFactory {
    
    private final AppCore app;

    public ObjectFactoryImpl(AppCore app) {
        this.app = Objects.requireNonNull(app);
    }

    @Override
    public <T> T get(Class<T> type) throws ObjectFactoryException {
        Object output;
        try{
            if(type.isEnum() || type.isPrimitive()) {
                throw new UnsupportedOperationException("Instiantiation not supported for type: " + type);
            }else if(type.equals(EntityFromMapBuilder.class)){
                output = new EntityFromMapBuilderImpl(app.getJpaContext(), this.getPersistenceUnitNames());
            }else if(type.equals(MapBuilder.class)){
                output = new EntityMapBuilder(app);
            }else if(type.equals(ObjectFactory.class)){
                output = new ObjectFactoryImpl(app);
            }else if(type.equals(ResourceContext.class)){
                output = new ResourceContextImpl();
            }else if(type.equals(MapBuilder.RecursionFilter.class)){
                output = new RecursionFilterImpl(app);
            }else if(type.equals(TypeProvider.class)){
                output = new JpaTypeProvider(app, this.getPersistenceUnitNameTest());
            }else if(type.equals(ParametersBuilder.class)){
                output = new ParametersBuilderImpl();
            }else if(type.equals(LoggingConfigManager.class)){
                output = new LoggingConfigManagerImpl(this.get(ResourceContext.class));
            }else if(type.equals(TextHandler.class)){
                output = new RawTextHandler();
            }else if(type.equals(Settings.class)){
                output = new SettingsImpl(app.getConfigService(), app.getConfig(), app.getSettingsConfig());
            }else{
                throw new ObjectNotSupportedException(type.getName());
            }
        }catch(UnsupportedOperationException | IOException e) {
            throw new ObjectFactoryException(e);
        }
        return (T)output;
    }
    
    public Set<String> getPersistenceUnitNames() {
        final JpaContext jpaContext = app.getJpaContext();
        final Predicate<String> puNameTest = this.getPersistenceUnitNameTest();
        final String [] puNames = jpaContext.getMetaData().getPersistenceUnitNames();
        final Set<String> accepted = new HashSet();
        for(String puName : puNames) {
            if(!puNameTest.test(puName)) {
                continue;
            }
            accepted.add(puName);
        }
        return accepted;
    }
    
    public Predicate<String> getPersistenceUnitNameTest() {
        final Predicate<String> puNameTest = 
                app.getJpaSync().equals(JpaSync.NO_OP) ? new AcceptAll() : new MasterPersistenceUnitTest();
        return puNameTest;
    }

    public AppCore getApp() {
        return app;
    }
}
