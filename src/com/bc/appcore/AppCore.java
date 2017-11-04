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
import com.bc.appcore.actions.ActionQueue;
import com.bc.appcore.exceptions.TargetNotFoundException;
import com.bc.appcore.html.HtmlBuilder;
import com.bc.appcore.parameter.ParametersBuilder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.bc.appcore.jpa.SearchContext;
import com.bc.appcore.util.Settings;
import com.bc.util.JsonFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import com.bc.appcore.jpa.model.EntityResultModel;
import com.bc.appcore.jpa.model.EntityResultModelImpl;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.sync.JpaSync;
import com.bc.jpa.sync.MasterSlavePersistenceContext;
import com.bc.jpa.sync.PendingUpdatesManager;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 7, 2017 11:10:58 PM
 */
public interface AppCore extends AppContext, ObjectFactory {

    default String getRandomId() {
        return UUID.randomUUID().toString();
    }
    
    String getName();
    
    void init();
    
    boolean isShutdown();
    
    void shutdown();
    
    ActionQueue getActionQueue();
    
    default ResultHandler getResultHandler(String name) {
        return new ResultHandlerImpl(Objects.requireNonNull(name));
    }
    
    Map<String, Object> getAttributes();
    
    PersistenceUnitContext getActivePersistenceUnitContext();
    
    MasterSlavePersistenceContext getMasterSlavePersistenceContext();
    
    JpaSync getJpaSync();
    
    boolean isUsingMasterDatabase();
    
    void switchToMasterDatabase();
    
    void switchToBackupDatabase();
    
    <T> T removeExpirable(Class<T> type, Object key) throws TargetNotFoundException;
    
    <T> T getExpirable(Class<T> type, Object key) throws TargetNotFoundException;
    
    User getUser();
    
    Class getUserEntityType();
    
    Comparator getEntityOrderComparator();
    
    <T> HtmlBuilder<T> getHtmlBuilder(Class<T> entityType);
    
    Class getDefaultEntityType();
    
    /**
     * @param <T> The type of the entity.
     * @param entityType The entity type. <b>May be null</b>.
     * @param outputIfNone Default output if none. <b>May be null</b>.
     * @return {@link com.bc.appcore.jpa.model.EntityResultModel EntityResultModel}
     */
    default <T> EntityResultModel<T> getResultModel(
            Class<T> entityType, EntityResultModel<T> outputIfNone) {
        
        if(entityType == null) {
            entityType = this.getDefaultEntityType();
        }
        
        return this.createResultModel(entityType, this.getTableColumnNames(entityType));
    }
    
    default EntityResultModel createResultModel(Class entityType, String [] columnNames) {
        
        if(entityType == null) {
            entityType = this.getDefaultEntityType();
        }
        
        return new EntityResultModelImpl(this, entityType, 
                Arrays.asList(columnNames), 
                (col, val) -> true, (col, exception) -> Logger.getLogger(getClass().getName()).log(
                        Level.WARNING, "Exception updating column: " + col, exception));
    }
    
    default String[] getTableColumnNames(Class entityType) {
        
        if(entityType == null) {
            entityType = this.getDefaultEntityType();
        }
        
        String key = "columnNames."+entityType.getName();
        String [] columnNames = this.getConfig().getArray(key, (String[])null);
        if(columnNames == null) {
            key = "columnNames."+entityType.getSimpleName();
            columnNames = this.getConfig().getArray(key, (String[])null);
            if(columnNames == null) {
                final String [] names = this.getActivePersistenceUnitContext().getMetaData().getColumnNames(entityType);
                final String scn = this.getSerialColumnName();
                if(scn == null) {
                    columnNames = names;
                }else{
                    columnNames = new String[names.length + 1];
                    columnNames[0] = scn;
                    System.arraycopy(names, 0, columnNames, 1, names.length);
                }
            }
        }
        Objects.requireNonNull(columnNames);
        return columnNames;
    }
    
    String getSerialColumnName();
    
    <T> SearchContext<T> getSearchContext(Class<T> resultType);
    
    <A extends AppCore> Action<A, ?> getAction(String actionCommand);
    
    <S> ParametersBuilder<S> getParametersBuilder(S source, String actionCommand);
    
    Settings getSettings();
    
    PendingUpdatesManager getPendingSlaveUpdatesManager();

    default DateFormat getDateTimeFormat() {
        return this.getDateTimeFormat(this.getDateTimePattern());
    }
    
    String getDateTimePattern();
    
    default DateFormat getDateFormat() {
        return this.getDateTimeFormat(this.getDatePattern());
    }
    
    String getDatePattern();
    
    default DateFormat getDateTimeFormat(String pattern) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(this.getTimeZone());
        dateFormat.setCalendar(this.getCalendar());
        dateFormat.applyLocalizedPattern(pattern);
        return dateFormat;
    }
    
    Calendar getCalendar();
    
    TimeZone getTimeZone();
    
    Locale getLocale();
    
    JsonFormat getJsonFormat();
}
