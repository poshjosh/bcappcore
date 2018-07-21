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

import com.bc.node.Node;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 15, 2017 6:07:06 PM
 */
public class NodeValuesNotEqualTest implements BiPredicate<Node, Node> {

    @Override
    public boolean test(Node lhs, Node rhs) {
        
        final boolean passed = lhs.getLevel() == rhs.getLevel() && 
        !Objects.equals(lhs.getValueOrDefault(null), rhs.getValueOrDefault(null));
        
//        if(passed) System.out.println("Passed: " + passed + ", lhs: " + lhs.getValueOrDefault(null) + ", rhs: " + rhs.getValueOrDefault(null));
        
        return passed;
    }
}
