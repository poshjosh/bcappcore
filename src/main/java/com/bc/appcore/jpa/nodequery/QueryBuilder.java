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

package com.bc.appcore.jpa.nodequery;

import com.bc.jpa.functions.GetCatalogSeparatorForPersistenceUnit;
import com.bc.jpa.functions.GetIdentifierQuoteStringForPersistenceUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 15, 2017 2:52:49 PM
 */
public class QueryBuilder<V> {

    private static final Logger logger = Logger.getLogger(QueryBuilder.class.getName());
    
    private final boolean quoteAll;

    private final String identifierQuoteString;
    
    private final String catalogSeparator;
    
    public QueryBuilder(
            Function<String, EntityManagerFactory> emfProvider, 
            String persistenceUnit, boolean quoteAll) {
        
        this(
                new GetIdentifierQuoteStringForPersistenceUnit(emfProvider).apply(persistenceUnit),
                new GetCatalogSeparatorForPersistenceUnit(emfProvider).apply(persistenceUnit), 
                quoteAll);
    }
    
    public QueryBuilder(
            String identifierQuoteString, String catalogSeparator, boolean quoteAll) {

        this.quoteAll = quoteAll;
        this.identifierQuoteString = identifierQuoteString;
        this.catalogSeparator = catalogSeparator;
        
        logger.finer(() -> "Initialized:: " + this);
    }
    
    public StringBuilder prependAndQuote(StringBuilder appendTo, String prefix, Object identifier) {
        if(this.isNullOrEmpty(prefix)) {
            this.quote(appendTo, identifier);
        }else{
            this.link(appendTo, prefix, identifier);
        }
        return appendTo;
    }
    
    public StringBuilder link(StringBuilder appendTo, String prefix, Object identifier) {
        this.quote(appendTo, prefix);
        this.appendSeparator(appendTo);
        this.quote(appendTo, identifier);
        return appendTo;
    }

    public StringBuilder appendSeparator(StringBuilder appendTo) {
        if(this.isNullOrEmpty(this.catalogSeparator)) {
            appendTo.append('.');
        }else{
            appendTo.append(this.catalogSeparator);
        }
        return appendTo;
    }

    public StringBuilder quote(StringBuilder appendTo, Object identifier) {
        if(this.isNullOrEmpty(this.identifierQuoteString)) {
            appendTo.append(identifier);
        }else{
            appendTo.append(this.identifierQuoteString).append(identifier).append(this.identifierQuoteString);
        }
        return appendTo;
    }
    
    public boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public boolean isQuoteAll() {
        return quoteAll;
    }

    public String getIdentifierQuoteString() {
        return identifierQuoteString;
    }

    public String getCatalogSeparator() {
        return catalogSeparator;
    }
}
