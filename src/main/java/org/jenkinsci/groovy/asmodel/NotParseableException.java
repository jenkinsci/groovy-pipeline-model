package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ASTNode;

/**
 * @author Kohsuke Kawaguchi
 */
public class NotParseableException extends Exception {
    public final ASTNode node;

    public NotParseableException(String message, ASTNode node) {
        super(message);
        this.node = node;
    }
}
