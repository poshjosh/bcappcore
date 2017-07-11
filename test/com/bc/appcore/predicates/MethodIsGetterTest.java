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
package com.bc.appcore.predicates;

import java.lang.reflect.Method;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class MethodIsGetterTest {
    
    public MethodIsGetterTest() {
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

    @Test
    public void testSomeMethod() {
System.out.println(System.getProperties());
System.out.println(System.getProperty("java.io.tmpdir"));

        final Method [] methods = MethodIsGetter.class.getMethods();
        for(Method method : methods) {
System.out.println(method);
System.out.println("Declaring class: " + method.getDeclaringClass());
System.out.println("Parameter count: " + method.getParameterCount());
System.out.println("Return type: " + method.getReturnType());
        }
    }
    
}
