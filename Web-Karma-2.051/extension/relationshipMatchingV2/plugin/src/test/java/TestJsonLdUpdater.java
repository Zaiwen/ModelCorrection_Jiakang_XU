import au.com.d2dcrc.yago2es.JsonLdUpdater;
import au.com.d2dcrc.yago2es.UpsertJsonLdScript;

import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestJsonLdUpdater {

    @Test
    public void testUpdate() throws IOException {
        final Map<String, Object> target = getOriginal();
        final Map<String, Object> update = getUpdate();

        final JsonLdUpdater updater = new JsonLdUpdater();
        final int changes = updater.update(target, update);

        final Map<String, Object> expected = getUpdateExpected();
        Assert.assertEquals(expected, target);
        Assert.assertEquals(1, changes);
    }

    @Test
    public void testUpsert() throws IOException {
        final Map<String, Object> target = new HashMap<>();
        final Map<String, Object> update = getUpdate();

        final JsonLdUpdater updater = new JsonLdUpdater();
        final int changes = updater.update(target, update);

        final Map<String, Object> expected = getUpsertExpected();
        Assert.assertEquals(expected, target);
        Assert.assertEquals(2, changes);
    }

    @Test
    public void testNoChange() throws IOException {
        final Map<String, Object> target = getOriginal();
        final Map<String, Object> update = new HashMap<>(0);

        final JsonLdUpdater updater = new JsonLdUpdater();
        final int changes = updater.update(target,update);

        final Map<String, Object> expected = getOriginal();
        Assert.assertEquals(expected, target);
        Assert.assertEquals(0, changes);
    }

    private Map<String, Object> getOriginal() throws IOException {
        return XContentFactory.xContent(XContentType.JSON).createParser(
            "{\n"
            + "  \"@context\": \"test.jsonld\",\n"
            + "  \"name\": \"The Empire State Building\",\n"
            + "  \"description\": \"The Empire State Building is a 102-story landmark in New York City.\",\n"
            + "  \"image\": { \"@id\" : \"http://www.civil.usherbrooke.ca/cours/gci215a/empire-state-building.jpg\", \"@type\" : \"http://schema.org/image\" },\n"
            + "  \"geo\": {\n"
            + "    \"latitude\": \"40.75\",\n"
            + "    \"longitude\": \"73.98\"\n"
            + "  }\n"
            + "}"
        ).map();
    }

    private Map<String, Object> getUpdate() throws IOException {
        return XContentFactory.xContent(XContentType.JSON).createParser(
            "{\n"
            + "  \"@context\": \"test.jsonld\",\n"
            + "  \"image\": { \"@id\" : \"https://upload.wikimedia.org/wikipedia/commons/d/df/NYC_Empire_State_Building.jpg\", \"@type\" : \"http://schema.org/image\" }\n"
            + "}"
        ).map();
    }

    private Map<String, Object> getUpdateExpected() throws IOException {
        return XContentFactory.xContent(XContentType.JSON).createParser(
            "{\n"
            + "  \"@context\": \"test.jsonld\",\n"
            + "  \"name\": \"The Empire State Building\",\n"
            + "  \"description\": \"The Empire State Building is a 102-story landmark in New York City.\",\n"
            + "  \"image\": ["
            + "    { \"@id\" : \"http://www.civil.usherbrooke.ca/cours/gci215a/empire-state-building.jpg\", \"@type\" : \"http://schema.org/image\" },\n"
            + "    { \"@id\" : \"https://upload.wikimedia.org/wikipedia/commons/d/df/NYC_Empire_State_Building.jpg\", \"@type\" : \"http://schema.org/image\" }\n"
            + "  ],\n"
            + "  \"geo\": {\n"
            + "    \"latitude\": \"40.75\",\n"
            + "    \"longitude\": \"73.98\"\n"
            + "  }\n"
            + "}"
        ).map();
    }

    private Map<String, Object> getUpsertExpected() throws IOException {
        return getUpdate();
    }
}
