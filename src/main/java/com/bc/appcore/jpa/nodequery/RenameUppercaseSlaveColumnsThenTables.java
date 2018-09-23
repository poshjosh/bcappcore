package com.bc.appcore.jpa.nodequery;

import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.metadata.PersistenceNode;
import com.bc.node.Node;
import com.bc.node.NodeFormat;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 17, 2017 2:22:06 PM
 */
public class RenameUppercaseSlaveColumnsThenTables {

    private static final Logger logger = Logger.getLogger(RenameUppercaseSlaveColumnsThenTables.class.getName());

    public RenameUppercaseSlaveColumnsThenTables() { }

    public void execute(PersistenceUnitContext masterPuContext, PersistenceUnitContext slavePuContext) {
        
        logger.fine(() -> "execute(PersistenceUnitContext, PersistenceUnitContext)");
        
        try{
            
            final Predicate<Node<String>> nodeValueIsUpperCaseTest = (node) -> {
                try{
                    return node.getValueOrDefault(null).toUpperCase().equals(node.getValueOrDefault(null));
                }catch(RuntimeException e) {
                    logger.log(Level.WARNING, "Unexpected Exception", e);
                    throw new RuntimeException(e.getLocalizedMessage());
                }    
            };
            
            final Node<String> masterPuNode = masterPuContext.getMetaData();
            logger.finer(() -> "MASTER PERSISTENCE UNIT NODE\n" + new NodeFormat().format(masterPuNode));            

            final Node<String> slavePuNode = slavePuContext.getMetaData();
            logger.finer(() -> "SLAVE PERSISTENCE UNIT NODE\n" + new NodeFormat().format(slavePuNode));

            logger.fine(() -> "Persistence Unit Node to visit: " + slavePuNode);

            final QueryBuilder queryBuilder = new QueryBuilder(
                    slavePuContext.getPersistenceContext(), 
                    Objects.requireNonNull(slavePuNode.getValueOrDefault(null)), 
                    false);

            final Predicate<Node<String>> tableFilter = new NodeLevelTest<String>(PersistenceNode.table.getLevel())
                    .and(nodeValueIsUpperCaseTest)
                    .and((node) -> !node.getValueOrDefault(null).startsWith("SYS"));
            
            final Predicate<Node<String>> columnFilter = new NodeLevelTest<String>(PersistenceNode.column.getLevel())
                    .and(nodeValueIsUpperCaseTest).and((node) -> {
                        final Node<String> parent = node.getParentOrDefault(null);
                        return parent != null && tableFilter.test(parent);
                    });
            
            new ExecuteQueryForNodes(
                    slavePuContext, 
                    columnFilter,
                    new QueryRenameColumn(queryBuilder, masterPuNode)
            ).run();

            new ExecuteQueryForNodes(
                    slavePuContext, 
                    tableFilter,
                    new QueryRenameTable(queryBuilder, masterPuNode)
            ).run();
            
        }catch(Exception e) {
            logger.log(Level.WARNING, "", e);
        }
    }
}
