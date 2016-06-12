package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.jenkinsci.groovy.asmodel.model.Branch;
import org.jenkinsci.groovy.asmodel.model.PipelineDef;
import org.jenkinsci.groovy.asmodel.model.Stage;
import org.jenkinsci.groovy.asmodel.model.Step;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map.Entry;

/**
 * @author Kohsuke Kawaguchi
 */
public class ModelParser {
    /**
     * Given a Groovy AST that represents a parsed source code, parses
     * that into {@link PipelineDef}
     */
    public @Nonnull PipelineDef parse(ModuleNode src) throws NotParseableException {
        PipelineDef r = new PipelineDef();
        for (Statement stmt : src.getStatementBlock().getStatements()) {
            r.stages.add(parseStage(stmt));
        }
        return r;
    }

    public @Nonnull Stage parseStage(Statement stmt) throws NotParseableException {
        BlockStatementMatch m = matchBlockStatement(stmt);
        if (m==null || !m.methodName.equals("stage"))
            throw new NotParseableException("Expected a stage",stmt);

        Expression nameExp = m.getArgument(0);
        if (nameExp==null)
            throw new NotParseableException("Expected a stage name but didn't find any",stmt);

        String name = stringLiteral(nameExp);
        if (name==null)
            throw new NotParseableException("Expected to find a constant stage name",nameExp);

        Stage stage = new Stage(name);
        return parseStageBody(stage, asBlock(m.body.getCode()));
    }

    /**
     * Given the body of a stage block, attempts to fill in {@link Stage#branches}.
     *
     * <p>
     * If the body's sole statement is {@code parallel(...)} then it's treated as
     * branches of the stage, or else
     */
    protected Stage parseStageBody(Stage stage, BlockStatement block) throws NotParseableException {
        if (block.getStatements().size()==1) {
            ParallelMatch parallel = matchParallel(block.getStatements().get(0));
            if (parallel!=null) {
                for (Entry<String, ClosureExpression> e : parallel.args.entrySet()) {
                    stage.branches.add(parseBranch(e.getKey(), asBlock(e.getValue().getCode())));
                }
                return stage;
            }
        }

        // otherwise it's a single line of execution
        stage.branches.add(parseBranch("default",block));

        return stage;
    }

    /**
     * Parses a block of code into {@link Branch}
     */
    public Branch parseBranch(String name, BlockStatement body) throws NotParseableException {
        Branch b = new Branch(name);
        for (Statement st : body.getStatements()) {
            b.steps.add(parseStep(st));
        }
        return b;
    }

    /**
     * Parses a statement into a {@link Step}
     */
    public Step parseStep(Statement st) throws NotParseableException {
        XXX
        return null;
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
    public @Nullable BlockStatementMatch matchBlockStatement(Statement st) {
        MethodCallExpression whole = matchMethodCall(st);
        if (whole!=null) {
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
     * Attempts to match a statement as {@link ParallelMatch} or return null.
     */
    public @Nullable ParallelMatch matchParallel(Statement st) throws NotParseableException {
        MethodCallExpression whole = matchMethodCall(st);
        if (whole!=null) {
            String methodName = matchMethodName(whole);
            if ("parallel".equals(methodName)) {
                // beyond this point, if there's mismatch from the expectation we'll throw a problem, instead of returning null

                TupleExpression args = (TupleExpression)whole.getArguments(); // list of arguments. in this case it should be just one
                int sz = args.getExpressions().size();
                if (sz==1) {
                    NamedArgumentListExpression branches = castOrNull(NamedArgumentListExpression.class, args.getExpression(sz - 1));
                    if (branches!=null) {
                        ParallelMatch r = new ParallelMatch(whole);
                        for (MapEntryExpression e : branches.getMapEntryExpressions()) {
                            String key = stringLiteral(e.getKeyExpression());
                            if (key==null)
                                throw new NotParseableException("Expected string literal",e.getKeyExpression());
                            ClosureExpression value = castOrNull(ClosureExpression.class, e.getValueExpression());
                            if (value==null)
                                throw new NotParseableException("Expected closure",e.getKeyExpression());
                            r.args.put(key,value);
                        }
                        return r;
                    }
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

    /**
     * Normalizes a statement to a block of statement by creating a wrapper if need be.
     */
    protected BlockStatement asBlock(Statement st) {
        if (st instanceof BlockStatement) {
            return (BlockStatement) st;
        } else {
            BlockStatement bs = new BlockStatement();
            bs.addStatement(st);
            return bs;
        }
    }

    /**
     * Attempts to match a given statement as a method call, or return null
     */
    protected @Nullable MethodCallExpression matchMethodCall(Statement st) {
        if (st instanceof ExpressionStatement) {
            ExpressionStatement es = (ExpressionStatement) st;
            Expression exp = es.getExpression();
            if (exp instanceof MethodCallExpression) {
                return (MethodCallExpression) exp;
            }
        }
        return null;
    }
}
