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

import com.bc.jpa.metadata.PersistenceNode;
import com.bc.node.Node;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 15, 2017 3:47:04 PM
 */
public class QueryRenameColumn extends QueryRename {

    public QueryRenameColumn(QueryBuilder queryBuilder, Node<String> searchRoot) {
        super(queryBuilder, searchRoot, "COLUMN");
    }

    @Override
    public void validate(Node<String> columnNode) {
        if(columnNode.getLevel() != PersistenceNode.column.getLevel()) {
            throw new IllegalArgumentException("Expected column node but found: " + columnNode);
        }
    }
}
