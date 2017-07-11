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

import com.bc.appcore.actions.Action;
import com.bc.appcore.exceptions.TargetNotFoundException;
import com.bc.config.Config;
import com.bc.config.ConfigService;
import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.Dao;
import com.bc.jpa.sync.JpaSync;
import com.bc.jpa.sync.SlaveUpdates;
import com.bc.appcore.html.HtmlBuilder;
import com.bc.appcore.parameter.ParametersBuilder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.jpa.model.ResultModel;
import com.bc.appcore.util.ExpirableCache;
import com.bc.appcore.util.Settings;
import com.bc.util.JsonFormat;
import java.util.Properties;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:10:58 PM
 */
public interface AppCore extends ObjectFactory {
    
    String getName();
    
    void init();
    
    boolean isShutdown();
    
    void shutdown();
    
    SlaveUpdates getSlaveUpdates();
    
    JpaSync getJpaSync();
    
    Filenames getFilenames();
    
    Map<String, Object> getAttributes();
    
    ExpirableCache<Object> getExpirableAttributes();
    
    <T> T removeExpirable(Class<T> type, Object key) throws TargetNotFoundException;
    
    <T> T getExpirable(Class<T> type, Object key) throws TargetNotFoundException;
    
    ConfigService getConfigService();
    
    UserBase getUser();
    
    <T> HtmlBuilder<T> getHtmlBuilder(Class<T> entityType);
    
    <T> ResultModel<T> getResultModel(Class<T> entityType, ResultModel<T> outputIfNone);
    
    <T> SearchContext<T> getSearchContext(Class<T> resultType);
    
    <A extends AppCore> Action<A, ?> getAction(String actionCommand);
    
    <S> ParametersBuilder<S> getParametersBuilder(S source, String actionCommand);
    
    Config getConfig();
    
    Properties getSettingsConfig();
    
    Settings getSettings();
    
    EntityManager getEntityManager(Class entityType);
    
    Dao getDao(Class entityType);
    
    JpaContext getJpaContext();
    
    /**
     * This returns the actual persistence unit names used by the application. And it is
     * typically a subset of those returned by {@link #getJpaContext()#getPersistenceUnitNames()}.
     * @return The names of the persistence units used by the application
     * @see #getJpaContext() 
     */
    Set<String> getPersistenceUnitNames();
    
    DateFormat getDateTimeFormat();
    
    DateFormat getDateFormat();
    
    Calendar getCalendar();
    
    TimeZone getTimeZone();
    
    Locale getLocale();
    
    JsonFormat getJsonFormat();
}
