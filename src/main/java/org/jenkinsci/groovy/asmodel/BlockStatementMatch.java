package org.jenkinsci.groovy.asmodel;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;

import javax.annotation.Nullable;

/**
 * Pattern matcher for the following Groovy construct:
 *
 * <pre><xmp>
 * foo(...) {
 *
 * }
 * </xmp></pre>
 *
 * @author Kohsuke Kawaguchi
 */
public class BlockStatementMatch {
    /**
     * ASTNode that matches the whole thing, which is a method invocation
     */
    public final MethodCallExpression whole;

    /**
     * Name of the method. In the above example, 'foo'
     */
    public final String methodName;

    /**
     * Method invocation arguments, including the last one that's a closure.
     */
    public final TupleExpression arguments;

    /**
     * Body of the block.
     *
     * @see ClosureExpression#getCode()
     */
    public final ClosureExpression body;

    public BlockStatementMatch(MethodCallExpression whole, String methodName, ClosureExpression body) {
        this.whole = whole;
        this.methodName = methodName;
        this.arguments = (TupleExpression)whole.getArguments(); // see MethodCallExpression.setArguments() that guarantee the success of this cast
        this.body = body;
    }

    public static final Matcher<BlockStatementMatch> MATCHER = new Matcher<BlockStatementMatch>() {
        @Override
        public BlockStatementMatch match(ASTNode n) {
            if (n instanceof MethodCallExpression) {
                MethodCallExpression whole = (MethodCallExpression) n;
                String methodName = matchMethodName(whole);
                TupleExpression args = (TupleExpression)whole.getArguments();
                int sz = args.getExpressions().size();
                if (sz>0) {
                    Expression last = args.getExpression(sz - 1);
                    if (last instanceof ClosureExpression) {
                        ClosureExpression body = (ClosureExpression) last;
                        return new BlockStatementMatch(whole,methodName,body);
                    }
                }
            }

            return null;
        }
    };
}
