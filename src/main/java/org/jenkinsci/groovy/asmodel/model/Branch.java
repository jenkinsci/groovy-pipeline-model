package org.jenkinsci.groovy.asmodel.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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

    public JSONObject toJSON() {
        JSONArray a = new JSONArray();
        for (Step s : steps) {
            a.add(s.toJSON());
        }
        return new JSONObject()
                .accumulate("name",name)
                .accumulate("steps",a);
    }
}
