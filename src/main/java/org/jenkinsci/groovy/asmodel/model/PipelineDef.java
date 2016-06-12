package org.jenkinsci.groovy.asmodel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the parsed pipeline definition for visual pipeline editor.
 *
 * @author Kohsuke Kawaguchi
 */
public final class PipelineDef {
    public final List<Stage> stages = new ArrayList<Stage>();
}
