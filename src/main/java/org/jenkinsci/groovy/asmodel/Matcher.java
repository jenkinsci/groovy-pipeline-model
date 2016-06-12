package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import javax.annotation.Nullable;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Matcher<T> {
    /**
     * Attempts to pattern-match the given AST node to deconstruct objects
     * into properties.
     *
     * @return null
     *      if the given AST doesn't match the expected pattern.
     */
    public abstract @Nullable T match(ASTNode n);

    /**
     * Attempts to match a method call of the form {@code foo(...)} and
     * return 'foo' as a string.
     */
    protected @Nullable String matchMethodName(MethodCallExpression exp) {
        Expression lhs = exp.getObjectExpression();
        if (lhs instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) lhs;
            if (ve.getName().equals("this")) {
                return exp.getMethodAsString(); // getMethodAsString() returns null if the method isn't a constant
            }
        }
        return null;
    }

    /**
     * Works like a regular Java cast, except if the value doesn't match the type, return null
     * instead of throwing an exception.
     */
    protected <X> X castOrNull(Class<X> type, Object value) {
        if (type.isInstance(value))
            return type.cast(value);
        return null;
    }
}
