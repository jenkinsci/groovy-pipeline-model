package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.jenkinsci.groovy.asmodel.model.PipelineDef;
import org.jenkinsci.groovy.asmodel.model.Stage;

import javax.annotation.Nullable;

/**
 * @author Kohsuke Kawaguchi
 */
public class ModelParser {
    /**
     * Given a Groovy AST that represents a parsed source code, parses
     * that into {@link PipelineDef}
     */
    public PipelineDef parse(ModuleNode src) throws NotParseableException {
        PipelineDef r = new PipelineDef();
        for (Statement stmt : src.getStatementBlock().getStatements()) {
            r.stages.add(parseStage(stmt));
        }
        return r;
    }

    public Stage parseStage(Statement stmt) throws NotParseableException {
        BlockStatementMatch m = blockStatement(stmt);
        if (m==null || !m.methodName.equals("stage"))
            throw new NotParseableException("Expected a stage",stmt);

        Expression nameExp = m.getArgument(0);
        if (nameExp==null)
            throw new NotParseableException("Expected a stage name but didn't find any",stmt);

        String name = stringLiteral(nameExp);
        if (name==null)
            throw new NotParseableException("Expected to find a constant stage name",nameExp);

        Stage stage = new Stage(name);
        // TODO: body matching
        return stage;
    }

    protected @Nullable String stringLiteral(Expression exp) {
        if (exp instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) exp;
            return castOrNull(String.class,ce.getValue());
        }
        return null;
    }

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
     * Attempts to match AST node as {@link BlockStatementMatch} or
     * return null.
     */
    public @Nullable BlockStatementMatch blockStatement(ASTNode n) {
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
