package au.com.d2dcrc.yago2es;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for converting facts to JSON-LD documents.
 */
public abstract class JsonLdBuilder {

    /**
     * Builds a document describing the object, with the fact represented in reverse.
     *
     * @param fact The fact which links to the object.
     * @return A JSON-LD document, represented as a Map.
     */
    public static Map<String, Object> buildObjectDocument(final MutableYagoFact fact) {
        final Map<String, Object> document = new HashMap<>(4);
        putContextReference(document);
        putObjectId(fact, document);
        putReversePredicateProperty(fact, document);
        putObjectInFact(fact, document);
        return document;
    }

    /**
     * Builds a document describing the subject.
     * @param fact The fact about the subject.
     * @return A JSON-LD document, represented as a Map.
     */
    public static Map<String, Object> buildSubjectDocument(final MutableYagoFact fact) {
        final Map<String, Object> document = new HashMap<>(4);
        putContextReference(document);
        putSubjectId(fact, document);
        putPredicateProperty(fact, document);
        putSubjectInFact(fact, document);
        return document;
    }

    /**
     * Gets the resolved id of the object of a fact.
     *
     * @param fact The fact referring to the object.
     * @return A single resolved identifier.
     */
    public static String resolvedObjectId(final MutableYagoFact fact) {
        return namespaceReference(fact.objectNamespace, fact.object);
    }

    /**
     * Gets the resolved id of the subject of a fact.
     *
     * @param fact The fact referring to the subject.
     * @return A single resovled identifier.
     */
    public static String resolvedSubjectId(final MutableYagoFact fact) {
        return namespaceReference(fact.subjectNamespace, fact.subject);
    }

    private static Map<String, Object> buildLiteralValue(final MutableYagoFact fact) {
        final Map<String, Object> document = new HashMap<>(4);
        document.put("@value", fact.value.toString());
        document.put("@type", prefixReference(fact.valueTypePrefix, fact.valueType));
        if (fact.valueLanguage != null) {
            document.put("@language", fact.valueLanguage.toString());
        }
        putObjectInFact(fact, document);
        return document;
    }

    private static Map<String, Object> buildObjectValue(final MutableYagoFact fact) {
        final Map<String, Object> document = new HashMap<>(2);
        putObjectId(fact, document);
        putObjectInFact(fact, document);
        return document;
    }

    private static Map<String, Object> buildSubjectValue(final MutableYagoFact fact) {
        final Map<String, Object> document = new HashMap<>(2);
        putSubjectId(fact, document);
        putSubjectInFact(fact, document);
        return document;
    }

    private static String namespaceReference(final CharSequence namespace, final CharSequence id) {
        return namespace != null ? namespace.toString() + id : id.toString();
    }

    private static String prefixReference(final CharSequence prefix, final CharSequence id) {
        return prefix != null ? prefix + ":" + id : id.toString();
    }

    private static Map<String, Object> putContextReference(final Map<String, Object> parent) {
        parent.put("@context", "http://kg.d2dcrc.net/default.jsonld");
        return parent;
    }

    private static Object putObjectId(MutableYagoFact fact, Map<String, Object> document) {
        return document.put("@id", prefixReference(fact.objectPrefix, fact.object));
    }

    private static void putObjectInFact(final MutableYagoFact fact, final Map<String, Object> parent) {
        if (fact.factId != null) {
            final List<Map<String, Object>> valueObjects = new ArrayList<>(1);
            final Map<String, Object> valueObject = new HashMap<>(1);
            valueObject.put("@id", prefixReference(fact.factPrefix, fact.factId));
            valueObjects.add(valueObject);
            parent.put("objectInFact", valueObjects);
        }
    }

    private static void putPredicateProperty(final MutableYagoFact fact, final Map<String, Object> parent) {
        final List<Map<String, Object>> valueObjects = new ArrayList<>(1);
        valueObjects.add(fact.object != null ?
                         buildObjectValue(fact) :
                         buildLiteralValue(fact));
        parent.put(replaceColon(prefixReference(fact.predicatePrefix, fact.predicate)), valueObjects);
    }

    private static void putReversePredicateProperty(final MutableYagoFact fact, final Map<String, Object> parent) {
        final Map<String, Object> reverseLinks = new HashMap<>(1);
        final List<Map<String, Object>> valueObjects = new ArrayList<>(1);
        valueObjects.add(buildSubjectValue(fact));
        reverseLinks.put(replaceColon(prefixReference(fact.predicatePrefix, fact.predicate)), valueObjects);
        parent.put("@reverse", reverseLinks);
    }

    private static void putSubjectId(MutableYagoFact fact, Map<String, Object> document) {
        document.put("@id", prefixReference(fact.subjectPrefix, fact.subject));
    }

    private static void putSubjectInFact(final MutableYagoFact fact, final Map<String, Object> parent) {
        if (fact.factId != null) {
            final List<Map<String, Object>> valueObjects = new ArrayList<>(1);
            final Map<String, Object> valueObject = new HashMap<>(1);
            valueObject.put("@id", prefixReference(fact.factPrefix, fact.factId));
            valueObjects.add(valueObject);
            parent.put("subjectInFact", valueObjects);
        }
    }

    private static String replaceColon(final String reference) {
        // HACK: Replacing colons in field names with dashes, because the colons break Lucene's QueryParser (although
        // they index fine).
        // Note that this technically changes the namespace (and therefore definition) of the facts.
        // TODO: Fix Lucene's QueryParser, push it upstream, and get it included into Elasticsearch.
        return reference.replace(':', '-');
    }

    private JsonLdBuilder() {
    }
}
