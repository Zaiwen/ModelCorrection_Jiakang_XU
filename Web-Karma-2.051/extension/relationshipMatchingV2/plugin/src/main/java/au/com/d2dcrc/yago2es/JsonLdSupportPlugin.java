package au.com.d2dcrc.yago2es;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;

/**
 * Provides support for indexing JSON-LD documents
 */
public class JsonLdSupportPlugin extends Plugin {

    @Override
    public String name() {
        return "jsonld";
    }

    @Override
    public String description() {
        return "Provides some support for indexing facts as JSON-LD documents.";
    }

    /**
     * Registers the Upsert script
     *
     * @param module The script module to register with.
     */
    public void onModule(final ScriptModule module) {
        module.registerScript("upsert_jsonld", UpsertJsonLdScriptFactory.class);
    }
}