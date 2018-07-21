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

import com.bc.jpa.metadata.PersistenceNodeBuilder;
import com.bc.node.Node;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 15, 2017 3:41:10 PM
 */
public class QueryRenameTable extends QueryRename {

    public QueryRenameTable(QueryBuilder queryBuilder, Node<String> searchRoot) {
        super(queryBuilder, searchRoot, "TABLE");
    }

    @Override
    public void validate(Node<String> tableNode) {
        if(tableNode.getLevel() != PersistenceNodeBuilder.NODE_LEVEL_TABLE) {
            throw new IllegalArgumentException("Expected table node but found: " + tableNode);
        }
    }

    @Override
    public String getParentNodeValue(Node<String> tableNode) {
        final Node<String> schemaNode = Objects.requireNonNull(tableNode.getParentOrDefault(null));
        final Node<String> catalogNode = Objects.requireNonNull(schemaNode.getParentOrDefault(null));
        final String parentNodeValue = catalogNode.getValueOrDefault(null);
        return parentNodeValue;
    }
}