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

import com.bc.appcore.user.UserLoginWithUsername;
import com.bc.appcore.user.User;
import com.authsvc.client.AppAuthenticationSession;
import com.authsvc.client.JsonResponseIsErrorTest;
import com.authsvc.client.JsonResponseIsErrorTestImpl;
import com.bc.reflection.TypeProvider;
import com.bc.appcore.exceptions.ObjectCreationException;
import com.bc.appcore.exceptions.ObjectFactoryException;
import com.bc.appcore.exceptions.ObjectNotSupportedException;
import com.bc.appcore.functions.BuildEntityStructure;
import com.bc.appcore.functions.BuildEntityStructureImpl;
import com.bc.appcore.functions.GetNamedGetter;
import com.bc.appcore.functions.GetRelatedTypes;
import com.bc.appcore.functions.GetRelatedTypesImpl;
import com.bc.appcore.functions.ReplaceNonWordCharsWithUnderscore;
import com.bc.appcore.jpa.model.ColumnLabelProvider;
import com.bc.appcore.jpa.model.ColumnLabelProviderImpl;
import com.bc.appcore.parameter.ParameterExtractor;
import com.bc.appcore.parameter.ParameterExtractorImpl;
import com.bc.appcore.typeprovider.ColumnNamesProvider;
import com.bc.reflection.MemberTypeProvider;
import com.bc.reflection.MemberTypeProviderImpl;
import com.bc.reflection.TypeProviderImpl;
import com.bc.appcore.user.UserImpl;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import com.bc.appcore.util.LoggingConfigManager;
import com.bc.jpa.predicates.DatabaseCommunicationsFailureTest;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.reflection.MemberNamesProvider;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 29, 2017 4:24:26 PM
 */
public class ObjectFactoryImpl implements ObjectFactory {

    private static final Logger LOG = Logger.getLogger(ObjectFactoryImpl.class.getName());
    
    private final ObjectFactory parent;
    
    private final AppContext context;
    
    private final Map<Class, Supplier> defaultSuppliers;

    public ObjectFactoryImpl(AppContext app) {
        this(null, app);
    }
    
    public ObjectFactoryImpl(ObjectFactory parent, AppContext app) {
        if(this.equals(parent)) {
            throw new IllegalArgumentException("An instance of '"+ObjectFactory.class.getSimpleName()+"' cannot be parent to itself");
        }
        this.parent = parent;
        this.context = Objects.requireNonNull(app);
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

        if(type.isEnum() || type.isPrimitive()) {

            throw new ObjectNotSupportedException("Instiantiation not supported for type: " + type);
        }    
                
        Object output;
        try{
            if(defaultSuppliers.get(type) != null) {    
                
                output = defaultSuppliers.get(type).get();
                
            }else {

                output = this.doGetOrException(type);
            }
        }catch(Exception e) {
            
            if(this.parent != null) {
                try{
                    output = this.parent.getOrException(type);
                }catch(Exception ignored) {
                    output = null;
                }
            }else{
                output = null;
            }
            
            if(output == null) {
                throw new ObjectCreationException(e);
            }
        }
                
        if(output == null) {
            throw new ObjectNotSupportedException(type.toGenericString());
        }

        if(LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Type: {0}, output: {1}", new Object[]{type, output});
        }
        
        return (T)output;
    }
    
    public <T> T doGetOrException(Class<T> type) throws Exception {
        
        final Object output;

        if(type.equals(DatabaseCommunicationsFailureTest.class)){

            output = new DatabaseCommunicationsFailureTest();

        }else if(type.equals(JsonResponseIsErrorTest.class)){

            output = new JsonResponseIsErrorTestImpl();

        }else if(type.equals(EntityFromMapBuilder.class)){

            output = new EntityFromMapBuilderImpl(
                    context.getPersistenceContext(), 
                    Collections.singleton(context.getPersistenceContextSwitch().getActive().getName())
            );

        }else if(type.equals(MapBuilder.class)){

            output = new MapBuilderForEntity().nullsAllowed(true);

        }else if(type.equals(ObjectFactory.class)){

            output = new ObjectFactoryImpl(context);

        }else if(type.equals(ResultHandler.class)){

            output = new ResultHandlerImpl();

        }else if(type.equals(User.class)) {    

            output = this.createUser();
            
        }else if(type.equals(BuildEntityStructure.class)){

            output = new BuildEntityStructureImpl(context, this);

        }else if(type.equals(GetRelatedTypes.class)){

            output = new GetRelatedTypesImpl(this);

        }else if(type.equals(ColumnLabelProvider.class)){

            output = new ColumnLabelProviderImpl(context.getConfig(), this.getOrException(TypeProvider.class));

        }else if(type.equals(MemberNamesProvider.class)){

            output = new ColumnNamesProvider(context.getActivePersistenceUnitContext());

        }else if(type.equals(MemberTypeProvider.class)){

            final boolean columnNamesOnly = false;

            final BiFunction<Class, String, Method> getNamedGetter = new GetNamedGetter(
                    context.getActivePersistenceUnitContext()
            );
            
            output = new MemberTypeProviderImpl(
                    this.getOrException(MemberNamesProvider.class), columnNamesOnly, getNamedGetter
            );

        }else if(type.equals(TypeProvider.class)){

            final Set<Class> entityTypes = context.getPersistenceContextSwitch().getActive().getMetaData(false).getEntityClasses();

            output = new TypeProviderImpl(entityTypes, this.getOrException(MemberTypeProvider.class));

        }else if(type.equals(ParameterExtractor.class)){

            output = new ParameterExtractorImpl();

        }else if(type.equals(LoggingConfigManager.class)){

// @todo there should be ConfigNames.CHARSET_NAME etc             
            output = new LoggingConfigManagerImpl(this.getContext().getConfig().getString("charsetName", "utf-8"));

        }else if(type.equals(TextHandler.class)){

            output = new TextHandlerImpl();

        }else if(type.equals(RelationAccess.class)){

            output = new RelationAccessImpl();

        }else if(type.equals(Settings.class)){

            output = new SettingsImpl(context);

        }else{

            throw new ObjectNotSupportedException(type.getName());
        }
        
        return (T)output;
    }

    public User createUser() {
        return createUser(null);
    }
    
    public User createUserLoginWithUsername() {
//@todo there should be ConfigNames.DEFAULT_EMAIL_HOST 
        final String defaultEmailHost = context.getConfig().getString("defaultEmailHost", "gmail.com");
        final Function<String, String> nameToEmail = 
                new ReplaceNonWordCharsWithUnderscore().andThen(
                        (formattedName) -> formattedName + '@' + defaultEmailHost
                );
        return createUser(nameToEmail);
    }
    
    private User createUser(Function<String, String> nameToEmail) {
        final Optional<AppAuthenticationSession> optAuthSession = context.getAuthenticationSession();
        if(optAuthSession.isPresent()) {
            
            return nameToEmail == null 
                    
                    ? 
                    
                    new UserImpl(
                            optAuthSession.get(), 
                            this.getOrException(JsonResponseIsErrorTest.class)
                    ) 
                    
                    : 
                    
                    new UserLoginWithUsername(
                            optAuthSession.get(), 
                            this.getOrException(JsonResponseIsErrorTest.class),
                            nameToEmail
                    );
        }else{
//                output = new LocalUser();
            throw new UnsupportedOperationException(
                    "Cannot create user without instance of: " + 
                            AppAuthenticationSession.class.getName());
        }
    }
    
    public Map<Class, Supplier> getDefaultSuppliers() {
        return defaultSuppliers;
    }

    public AppContext getContext() {
        return context;
    }

    @Override
    public ObjectFactory getParent() {
        return parent;
    }
}
