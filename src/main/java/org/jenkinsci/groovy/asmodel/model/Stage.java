package org.jenkinsci.groovy.asmodel.model;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

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

    public JSONObject toJSON() {
        JSONArray a = new JSONArray();
        for (Branch br : branches) {
            a.add(br.toJSON());
        }
        return new JSONObject()
                .accumulate("name",name)
                .accumulate("branches",a);
    }
}
