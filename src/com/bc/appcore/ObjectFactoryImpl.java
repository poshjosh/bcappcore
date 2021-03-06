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

import com.authsvc.client.AppAuthenticationSession;
import com.bc.appcore.typeprovider.TypeProvider;
import com.bc.appcore.exceptions.ObjectCreationException;
import com.bc.appcore.exceptions.ObjectFactoryException;
import com.bc.appcore.exceptions.ObjectNotSupportedException;
import com.bc.appcore.functions.BuildEntityStructure;
import com.bc.appcore.functions.BuildEntityStructureImpl;
import com.bc.appcore.functions.GetRelatedTypes;
import com.bc.appcore.functions.GetRelatedTypesImpl;
import com.bc.appcore.functions.ReplaceNonWordCharsWithUnderscore;
import com.bc.appcore.jpa.model.ColumnLabelProvider;
import com.bc.appcore.jpa.model.ColumnLabelProviderImpl;
import com.bc.appcore.parameter.ParameterExtractor;
import com.bc.appcore.parameter.ParameterExtractorImpl;
import com.bc.appcore.parameter.ParametersBuilder;
import com.bc.appcore.parameter.ParametersBuilderImpl;
import com.bc.appcore.typeprovider.EntityMemberTypeProvider;
import com.bc.appcore.typeprovider.MemberTypeProvider;
import com.bc.appcore.typeprovider.TypeProviderImpl;
import com.bc.appcore.util.LoggingConfigManagerImpl;
import com.bc.appcore.util.TextHandlerImpl;
import com.bc.appcore.util.RelationAccess;
import com.bc.appcore.util.RelationAccessImpl;
import com.bc.appcore.util.Settings;
import com.bc.appcore.util.SettingsImpl;
import com.bc.appcore.util.TextHandler;
import com.bc.jpa.util.EntityFromMapBuilder;
import com.bc.jpa.util.EntityFromMapBuilderImpl;
import com.bc.jpa.util.MapBuilderForEntity;
import com.bc.util.MapBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import com.bc.appcore.util.LoggingConfigManager;
import com.bc.jpa.metadata.PersistenceMetaData;
import com.bc.jpa.metadata.PersistenceUnitMetaData;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 29, 2017 4:24:26 PM
 */
public class ObjectFactoryImpl 
        implements ObjectFactory {
    
    private final AppCore app;
    
    private final Map<Class, Supplier> defaultSuppliers;

    public ObjectFactoryImpl(AppCore app) {
        this.app = Objects.requireNonNull(app);
        this.defaultSuppliers = new HashMap<>();
    }
    
    @Override
    public <T> void registerDefault(Class<T> type, Supplier<T> typeSupplier) {
        defaultSuppliers.put(type, typeSupplier);
    }
    
    @Override
    public void deregisterDefault(Class type) {
        defaultSuppliers.remove(type);
    }
    
    @Override
    public <T> T getOrDefault(Class<T> type, T outputIfNone) {
        try{
            return this.getOrException(type);
        }catch(ObjectFactoryException ignored) {
            return outputIfNone;
        }
    }

    @Override
    public <T> T getOrException(Class<T> type) throws ObjectFactoryException {
        
        Object output;
        try{
            if(type.isEnum() || type.isPrimitive()) {
                
                throw new ObjectNotSupportedException("Instiantiation not supported for type: " + type);
                
            }else if(defaultSuppliers.get(type) != null) {    
                
                output = defaultSuppliers.get(type).get();
                
            }else {

                output = this.doGetOrException(type);
            }
        }catch(Exception e) {
            
            throw new ObjectCreationException(e);
        }
                
        if(output == null) {
            throw new ObjectNotSupportedException(type.getName());
        }
        
        return (T)output;
    }
    
    public <T> T doGetOrException(Class<T> type) throws Exception {
        
        final Object output;
        
        if(type.equals(EntityFromMapBuilder.class)){

            output = new EntityFromMapBuilderImpl(
                    app.getPersistenceContext(), 
                    Collections.singleton(app.getActivePersistenceUnitContext().getName())
            );

        }else if(type.equals(MapBuilder.class)){

            output = new MapBuilderForEntity().nullsAllowed(true);

        }else if(type.equals(ObjectFactory.class)){

            output = new ObjectFactoryImpl(app);

        }else if(type.equals(ResultHandler.class)){

            output = new ResultHandlerImpl();

        }else if(type.equals(User.class)) {    

            final AppAuthenticationSession authSession = app.getAuthenticationSession();
            if(authSession != null) {
//@todo there should be ConfigNames.DEFAULT_EMAIL_HOST 
                final String defaultEmailHost = app.getConfig().getString("defaultEmailHost", "gmail.com");
                final Function<String, String> nameToEmail = 
                        new ReplaceNonWordCharsWithUnderscore().andThen(
                                (formattedName) -> formattedName + '@' + defaultEmailHost
                        );
                output = new UserImpl(authSession, nameToEmail);
            }else{
//                output = new LocalUser();
                throw new UnsupportedOperationException(
                        "Cannot create user without instance of: " + 
                                AppAuthenticationSession.class.getName());
            }
        }else if(type.equals(BuildEntityStructure.class)){

            output = new BuildEntityStructureImpl(app);

        }else if(type.equals(GetRelatedTypes.class)){

            output = new GetRelatedTypesImpl(this);

        }else if(type.equals(ColumnLabelProvider.class)){

            output = new ColumnLabelProviderImpl(app.getConfig(), this.getOrException(TypeProvider.class));

        }else if(type.equals(MemberTypeProvider.class)){

            final boolean columnNamesOnly = false;

            final Map<Class, Set<String>> typeColumnNames = this.getEntityTypeColumnNames();

            output = new EntityMemberTypeProvider(
                    app.getActivePersistenceUnitContext(), typeColumnNames, columnNamesOnly);

        }else if(type.equals(TypeProvider.class)){

            final Set<Class> entityTypes = app.getActivePersistenceUnitContext().getMetaData().getEntityClasses();

            output = new TypeProviderImpl(entityTypes, this.getOrException(MemberTypeProvider.class));

        }else if(type.equals(ParameterExtractor.class)){

            output = new ParameterExtractorImpl();

        }else if(type.equals(ParametersBuilder.class)){

            output = new ParametersBuilderImpl();

        }else if(type.equals(LoggingConfigManager.class)){

// @todo there should be ConfigNames.CHARSET_NAME etc             
            output = new LoggingConfigManagerImpl(this.getApp().getConfig().getString("charsetName", "utf-8"));

        }else if(type.equals(TextHandler.class)){

            output = new TextHandlerImpl();

        }else if(type.equals(RelationAccess.class)){

            output = new RelationAccessImpl();

        }else if(type.equals(Settings.class)){

            output = new SettingsImpl(app);

        }else{

            throw new ObjectNotSupportedException(type.getName());
        }
        
        return (T)output;
    }

    public Map<Class, Set<String>> getEntityTypeColumnNames() {
        
        final Map<Class, Set<String>> output = new LinkedHashMap();
        
        final PersistenceMetaData metaData = app.getPersistenceContext().getMetaData();
        
        final String puName = app.getActivePersistenceUnitContext().getName();
            
        final Set<Class> puTypes = metaData.getEntityClasses(puName);

        final PersistenceUnitMetaData puMeta = metaData.getMetaData(puName);

        for(Class puType : puTypes) {

            final Set<String> columnNames = new HashSet(Arrays.asList(puMeta.getColumnNames(puType)));

            output.put(puType, columnNames);
        }
        
        return output.isEmpty() ? Collections.EMPTY_MAP : Collections.unmodifiableMap(output);
    }
    
    public Map<Class, Supplier> getDefaultSuppliers() {
        return defaultSuppliers;
    }

    public AppCore getApp() {
        return app;
    }
}
