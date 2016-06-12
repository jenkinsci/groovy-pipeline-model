package org.jenkinsci.groovy.asmodel;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.CompilationUnit.SourceUnitOperation;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.jenkinsci.groovy.asmodel.model.PipelineDef;

import java.io.File;
import java.security.CodeSource;
import java.security.cert.Certificate;

/**
 * Hello world!
 *
 */
public class App {
    private static final int phase = Phases.CANONICALIZATION;

    public static void main(String[] args) throws Exception {
        File src = new File("test.groovy");
        CompilationUnit cu = new CompilationUnit(
                CompilerConfiguration.DEFAULT,
                new CodeSource(src.toURL(),new Certificate[0]),
                new GroovyClassLoader());
        cu.addSource(src);

        cu.addPhaseOperation(new SourceUnitOperation() {
            @Override
            public void call(SourceUnit source) throws CompilationFailedException {
                try {
                    PipelineDef def = new ModelParser(source.getSource()).parse(source.getAST());
                    System.out.println(def.toJSON().toString(2));
                } catch (NotParseableException e) {
                    e.printStackTrace();
                }
            }
        }, phase);

        try {
            cu.compile(phase);
        } catch (CompilationFailedException cfe) {
            throw cfe;
        }
    }
}
