package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;

import javax.annotation.Nullable;

/**
 * @author Kohsuke Kawaguchi
 */
public class Matchers {
    /**
     * If the given expression is a string literal, return that value.
     */
    public static final Matcher<String> STRING_LITERAL = new Matcher<String>() {
        @Override
        public @Nullable String match(ASTNode n) {
            if (n instanceof ConstantExpression) {
                ConstantExpression ce = (ConstantExpression) n;
                return castOrNull(String.class,ce.getValue());
            }
            return null;
        }
    };
}
