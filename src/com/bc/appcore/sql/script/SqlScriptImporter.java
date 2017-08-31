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

package com.bc.appcore.sql.script;

import com.bc.jpa.JpaContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 10, 2017 9:00:31 PM
 */
public class SqlScriptImporter {

    private static final Logger logger = Logger.getLogger(SqlScriptImporter.class.getName());
    
    private final String charsetName;
    
    private final Level logLevel;

    public SqlScriptImporter() {
        this("utf-8", Level.INFO);
    }
    
    public SqlScriptImporter(String charsetName, Level logLevel) {
        this.charsetName = Objects.requireNonNull(charsetName);
        this.logLevel = Objects.requireNonNull(logLevel);
    }
    
    public Map<URL, List<Integer>> executeSqlScripts(
            JpaContext jpaContext, Class entityType, Enumeration<URL> urls) {

        final Set<URL> target = new LinkedHashSet();
        
        while(urls.hasMoreElements()) {
            
            final URL url = urls.nextElement();

            target.add(url);
        }
        
        logger.info(() -> "URLs: " + target);
        
        return this.executeSqlScripts(jpaContext, entityType, target);
    }
    
    public Map<URL, List<Integer>> executeSqlScripts(
            JpaContext jpaContext, Class entityType, Set<URL> urls) {

        final Map<URL, List<Integer>> output = new LinkedHashMap(urls.size(), 1.0f);
        
        for(URL url : urls) {
            
            final EntityManager em = jpaContext.getEntityManager(entityType);
            
            try{
                
                final List<Integer> updateCounts = this.executeSqlScript(em, url);
                
                output.put(url, updateCounts);
                
            }finally{
                
                if(em.isOpen()) {
                    em.close();
                }
            }
        }
        
        return output.isEmpty() ? Collections.EMPTY_MAP : Collections.unmodifiableMap(output);
    }        

    /**
     * @param em The {@link javax.persistence.EntityManager EntityManager} to use
     * in creating the native query from the read contents
     * @param oval Either {@link java.net.URL URL}, {@link java.lang.String String}
     * {@link java.nio.file.Path Path} or {@link java.io.File File}
     * @return The list of update counts
     */
    public List<Integer> executeSqlScript(EntityManager em, Object oval) {
        
        logger.log(this.logLevel, () -> "Reading: " + oval);
        
        final List<Integer> updateCounts = new ArrayList();
        
        try(Reader reader = new BufferedReader(new InputStreamReader(this.getInputStream(oval), this.charsetName))) {
            
            final List<String> sqlLines = new SqlLineParser().parse(reader);
            
            logger.log(this.logLevel, () -> "Found " + sqlLines.size() + " lines in: " + oval);
            
            em.getTransaction().begin();
            
            for(String sqlLine : sqlLines) {
                
                final Query query = em.createNativeQuery(sqlLine);
                
                final int updateCount = query.executeUpdate();
                
                logger.log(this.logLevel, () -> "Update count: " + updateCount + ", query: " + sqlLine);
                
                updateCounts.add(updateCount);
            }
            
            em.getTransaction().commit();
            
        }catch(IOException e) {
            
            logger.log(Level.WARNING, "Failed to read from: " + oval, e);
        }finally{
            em.close();
        }
        
        return updateCounts.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(updateCounts);
    } 
    
    public InputStream getInputStream(Object oval) throws IOException {
        final InputStream in;
        if(oval instanceof URL) {
            in = ((URL)oval).openStream();
        }else if(oval instanceof String) {
            in = new FileInputStream((String)oval);
        }else if(oval instanceof Path) {
            in = new FileInputStream(((Path)oval).toFile());
        }else if(oval instanceof File) {
            in = new FileInputStream((File)oval);
        }else{
            throw new IllegalArgumentException();
        }
        return in;
    }
}
