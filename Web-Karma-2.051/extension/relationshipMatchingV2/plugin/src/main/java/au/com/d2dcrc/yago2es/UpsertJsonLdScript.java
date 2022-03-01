package au.com.d2dcrc.yago2es;

import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.script.AbstractSearchScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A script that will take a JSON-LD view of a subject and update the existing view to include any new facts.
 */
public class UpsertJsonLdScript extends AbstractSearchScript {

    private final Map<String, Object> params;
    private Map<String, Object> source = new HashMap<>(0);

    private final JsonLdUpdater updater = new JsonLdUpdater();

    /**
     * Constructs a new instance.
     *
     * @param params The document to upsert, provided via the params script field. May be empty, but must not be null.
     */
    public UpsertJsonLdScript(final Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public Integer run() {
        return updater.update(source, params);
    }

    @Override
    public void setNextVar(final String name, final Object value) {
        switch (name) {
            case "ctx":
                final Map<String, Object> context = XContentMapValues.nodeMapValue(value, "ctx");
                source = XContentMapValues.nodeMapValue(XContentMapValues.extractValue("_source", context), "_source");
                break;
            default:
                break;
        }
    }
}
