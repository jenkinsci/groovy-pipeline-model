package org.jenkinsci.groovy.asmodel.model;

/**
 * @author Kohsuke Kawaguchi
 */
public class Step {
    public final String line;

    public Step(String line) {
        this.line = line;
    }

    public Object toJSON() {
        // TODO: further data binding
        return line;
    }
}
