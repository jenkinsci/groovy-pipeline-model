package org.jenkinsci.groovy.asmodel.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the parsed pipeline definition for visual pipeline editor.
 *
 * @author Kohsuke Kawaguchi
 */
public final class PipelineDef {
    public final List<Stage> stages = new ArrayList<Stage>();

    public JSONObject toJSON() {
        JSONArray a = new JSONArray();
        for (Stage s : stages) {
            a.add(s.toJSON());
        }
        return new JSONObject()
                .accumulate("pipeline", a);
    }
}
