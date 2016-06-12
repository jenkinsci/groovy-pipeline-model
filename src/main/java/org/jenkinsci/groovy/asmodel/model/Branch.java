package org.jenkinsci.groovy.asmodel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @see Stage#branches
 */
public final class Branch {
    public final String name;
    public final List<Step> steps = new ArrayList<Step>();

    public Branch(String name) {
        this.name = name;
    }
}
