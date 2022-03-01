package au.com.d2dcrc.yago2es;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Map;

/**
 * Creates instances of the native upsert script.
 */
public class UpsertJsonLdScriptFactory implements NativeScriptFactory {

    @Override
    public ExecutableScript newScript(@Nullable Map<String, Object> params) {
        return new UpsertJsonLdScript(params);
    }

    @Override
    public boolean needsScores() {
        return false;
    }
}
