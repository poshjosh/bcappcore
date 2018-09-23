/*
 * Copyright 2018 NUROX Ltd.
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

import com.bc.appcore.WindowHandler;
import com.bc.jpa.EntityManagerFactoryCreatorImpl;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.eclipselink.PersistenceContextEclipselinkOptimized;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class RenameUppercaseSlaveColumnsThenTablesTest {
    
    public RenameUppercaseSlaveColumnsThenTablesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class RenameUppercaseSlaveColumnsThenTables.
     */
    @Test
    public void testExecute() throws IOException, URISyntaxException {
        
        System.out.println("execute");
        
        try{
            
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            
//            try(final InputStream in = classLoader
//                            .getResourceAsStream("META-INF/rust_logging.properties")) {
//                LogManager.getLogManager().readConfiguration(in);
//            }
            
            final Logger logger = Logger.getLogger(RenameUppercaseSlaveColumnsThenTables.class.getPackage().getName());
            
            logger.addHandler(WindowHandler.getInstance());

            final URL persistenceUrl = classLoader.getResource("META-INF/persistence.xml");
            
            if(persistenceUrl == null) {
                return;
            }
            
            logger.fine(() -> "Persistence URL: " + persistenceUrl);
            
            final Function<String, Properties> getProps = (persistenceUnit) -> {
                logger.finer(() -> "\n\tFetching properties for persistence unit: " + persistenceUnit);                
                    final Properties properties = new Properties();
                    if("bctasktrackerPUmaster".equals(persistenceUnit)) {
                        properties.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://europa.ignitionserver.net:3306/loosebox_naftasktrackerweb");
                        properties.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
                        properties.setProperty("javax.persistence.jdbc.user", "loosebox_nafuser");
                        properties.setProperty("javax.persistence.jdbc.password", "7ApacheLaunchers");
                    }
                    return properties;
            };
            final PersistenceContext persistenceContext = new PersistenceContextEclipselinkOptimized(
                    persistenceUrl.toURI(), 
                    new EntityManagerFactoryCreatorImpl(
                            persistenceUrl.toURI(),
                            getProps
                    ), 
                    new MySQLDateTimePatterns()
            );
            
            logger.fine(() -> "Initialized: " + PersistenceContext.class.getSimpleName());
            
//            new RenameUppercaseSlaveColumnsThenTables().execute(masterPuContext, slavePuContext);

        }catch(Exception e) {
            
            e.printStackTrace();
        }
    }
}
