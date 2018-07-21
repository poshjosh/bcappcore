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

import com.bc.node.NodeVisitor;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.node.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 17, 2017 1:52:20 PM
 */
public class ExecuteQueryForNodes {

    private static final Logger logger = Logger.getLogger(ExecuteQueryForNodes.class.getName());

    private final PersistenceUnitContext puContext;
    
    private final Predicate<Node<String>> filter;
    
    private final Function<Node<String>, Optional<String>> queryFromNodes;

    public ExecuteQueryForNodes(
            PersistenceUnitContext puContext,
            Predicate<Node<String>> filter,
            Function<Node<String>, Optional<String>> queryFromNodes) { 
        this.puContext = Objects.requireNonNull(puContext);
        this.filter = Objects.requireNonNull(filter);
        this.queryFromNodes = Objects.requireNonNull(queryFromNodes);
    }

    public void run() {
        
        final Node<String> puNode = this.puContext.getMetaData().getNode();
        
        final Function<EntityManager, List<Integer>> action = (entityManager) -> {

            final List<Integer> resultList = new ArrayList<>();

            final Consumer<Node<String>> consumer = (node) -> {
                try{

                    final Optional<String> optQuery = queryFromNodes.apply(node);

                    if(optQuery.isPresent()) {

                        final String query = optQuery.get();

                        final int result = entityManager.createNativeQuery(query).executeUpdate();

                        resultList.add(result);

                        logger.fine(() -> "\nResult: " + 1 + ", node: " + node + "\nQuery: " + query);

                    }else{

                        logger.warning(() -> "Query could not be created for: " + node);
                    }

                }catch(RuntimeException e) {
                    
                    logger.log(Level.WARNING, "", e);
                }
            };

            logger.fine(() -> "Visiting nodes starting at " + puNode + "\nAction=Execute query: " + this.queryFromNodes);

            new NodeVisitor(filter, consumer).accept(puNode);
            
            return resultList;
        };
        
        this.puContext.executeTransaction(action);
    }
}
