package com.bc.appcore.jpa.nodequery;

import com.bc.node.Node;
import com.bc.node.NodeTest;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2017 8:31:17 PM
 */
public class QueryRename implements Function<Node<String>, Optional<String>> {

    private static final Logger logger = Logger.getLogger(QueryRenameColumn.class.getName());

    private final QueryBuilder queryBuilder; 
    private final Node<String> searchRoot;
    
    private final String keyword;
    
    public QueryRename(QueryBuilder queryBuilder, Node<String> searchRoot, String keyword) {
        this.queryBuilder = Objects.requireNonNull(queryBuilder);
        this.searchRoot = Objects.requireNonNull(searchRoot);
        this.keyword = Objects.requireNonNull(keyword);
    }
    
    public void validate(Node<String> columnNode) { }

    @Override
    public Optional<String> apply(Node<String> node) {
        
        this.validate(node);

        final Optional<Node<String>> optMatchingNode = searchRoot.findFirstChild(new NodeTest(node, true));

        if(optMatchingNode.isPresent()) {

            final StringBuilder appendTo = new StringBuilder();
            
            final String parentNodeValue = this.getParentNodeValue(node);
            
            final String nodeValue = Objects.requireNonNull(node.getValueOrDefault(null));
            
            final String matchingNodeValue = Objects.requireNonNull(optMatchingNode.get().getValueOrDefault(null));
            
            this.appendRenameQuery(appendTo, parentNodeValue, nodeValue, matchingNodeValue);
            
            return Optional.of(appendTo.toString());

        }else{
            
            return Optional.empty();
        }
    }
    
    public String getParentNodeValue(Node<String> node) {
        final Node<String> parentNode = Objects.requireNonNull(node.getParentOrDefault(null));
        final String parentNodeValue = parentNode.getValueOrDefault(null);
        return parentNodeValue;
    }
    
    public void appendRenameQuery(
            StringBuilder appendTo, String parentNodeValue, 
            String nodeValue, String matchingNodeValue) {
        
        Objects.requireNonNull(nodeValue);
        Objects.requireNonNull(matchingNodeValue);
        
        appendTo.append("RENAME ").append(this.keyword.toUpperCase()).append(' ');
        if(queryBuilder.isQuoteAll()) {
            queryBuilder.prependAndQuote(appendTo, parentNodeValue, nodeValue);
        }else{
            if(!queryBuilder.isNullOrEmpty(parentNodeValue)) {
                appendTo.append(parentNodeValue);
                queryBuilder.appendSeparator(appendTo);
            }
            appendTo.append(nodeValue);
        }
        appendTo.append(" TO ");
        queryBuilder.quote(appendTo, matchingNodeValue);
    }

    public final QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public final Node<String> getSearchRoot() {
        return searchRoot;
    }
}
