package org.jenkinsci.groovy.asmodel;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.jenkinsci.groovy.asmodel.model.PipelineDef;
import org.jenkinsci.groovy.asmodel.model.Stage;

import static org.jenkinsci.groovy.asmodel.Matchers.STRING_LITERAL;

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
        BlockStatementMatch m = BlockStatementMatch.MATCHER.match(stmt);
        if (m==null || !m.methodName.equals("stage"))
            throw new NotParseableException("Expected a stage",stmt);

        Expression nameExp = m.arguments.getExpression(0);
        String name = STRING_LITERAL.match(nameExp);
        if (name==null)
            throw new NotParseableException("Expected to find a constant stage name",nameExp);

        Stage stage = new Stage(name);
        // TODO: body matching
        return stage;
    }
}
