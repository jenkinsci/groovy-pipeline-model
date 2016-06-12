package org.jenkinsci.groovy.asmodel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @see PipelineDef
 */
public final class Stage {
    public final String name;
    public final List<Branch> branches = new ArrayList<Branch>();

    public Stage(String name) {
        this.name = name;
    }
}
