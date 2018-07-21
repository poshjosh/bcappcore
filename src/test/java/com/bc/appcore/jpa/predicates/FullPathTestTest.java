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
package com.bc.appcore.jpa.predicates;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class FullPathTestTest {
    
    public FullPathTestTest() {
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
     * Test of test method, of class FullPathTest.
     */
    @Test
    public void testTest() {
        testTest(false, "META-INF\\bctasktracker\\configs\\logging.properties");
        testTest(true, "C:\\Users\\Josh\\dhqtasktrackerweb\\configs\\logging.properties");
        testTest(true, "C:\\Users\\Josh\\Documents\\NetBeansProjects\\dhqtasktrackerweb\\build\\web\\META-INF\\dhqtasktrackerweb\\configs\\app_devmode.properties");
//        testTest(true, "jar:file:C:/Users/Josh/Documents");
//        testTest(true, "file:///C:/Users");
//        testTest(true, "http://");
    }
    
    public void testTest(boolean expResult, String str) {
        System.out.println("#test");
        final Path path = Paths.get(str);
        final FullPathTest instance = new FullPathTest();
        final boolean result = instance.test(path);
System.out.println("Full: "+result+", path: "+path);        
        assertEquals(expResult, result);
    }
}
