package au.com.d2dcrc.yago2es;

import com.google.common.collect.ImmutableMap;

import au.id.ajlane.iostreams.AbstractIOStreamTransform;
import au.id.ajlane.iostreams.FileLine;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the TSV files published by YAGO. <p> These files are <em>like</em> N-triples format files, but have
 * non-standard columns. </p>
 */
public class YagoTsvParser extends AbstractIOStreamTransform<FileLine, MutableYagoFact>{

    private static final class ValueCache {

        public CharSequence lang;
        public CharSequence namespace;
        public CharSequence prefix;
        public CharSequence type;
        public CharSequence value;

        public void reset() {
            namespace = null;
            prefix = null;
            type = null;
            value = null;
            lang = null;
        }
    }

    /**
     * Creates a parser with all of the known YAGO namespaces preconfigured.
     *
     * @return A ready parser.
     */
    public static YagoTsvParser withDefaultNamespaces() {
        return withNamespaces("http://yago-knowledge.org/resource/", "yago", ImmutableMap.<String, String>builder()
            .put("dbp", "http://dbpedia.org/ontology/")
            .put("owl", "http://www.w3.org/2002/07/owl#")
            .put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
            .put("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
            .put("skos", "http://www.w3.org/2004/02/skos/core#")
            .put("xsd", "http://www.w3.org/2001/XMLSchema#")
            .build());
    }

    /**
     * Creates a parser which will detect and normalise the given namespaces.
     *
     * @param baseNamespace           The base namespace for un-prefixed facts. E.g. {@code http://yago-knowledge.org/resource/}.
     *                                Must not be null or empty.
     * @param baseNamespacePrefix     The prefix to use for the base namespace. E.g. {@code yago}. Must not be null or
     *                                empty.
     * @param otherNamespacesByPrefix A set of prefix-namespace pairs. May be empty, if the files to be parsed contain
     *                                no prefixes. Must not be null.
     * @return A ready parser.
     */
    public static YagoTsvParser withNamespaces(final String baseNamespace, final String baseNamespacePrefix,
                                               final Map<String, String> otherNamespacesByPrefix) {
        return new YagoTsvParser(baseNamespace, baseNamespacePrefix, otherNamespacesByPrefix);
    }

    private final String base;
    private final String basePrefix;
    private final Matcher escapedCharacterMatcher =
        Pattern.compile(
            "(?<backslash>\\\\\\\\)|(?<lf>\\\\n)|(?<cr>\\\\r)|(?<tab>\\\\t)|(?<unicode>(\\\\u[0-9A-Fa-f]{4})|(\\\\U[0-9A-Fa-f]{8}))")
            .matcher("");
    private final Map<String, String> prefixes;
    private final Matcher referenceMatcher = Pattern.compile("^(?<prefix>\\w+?):(?<id>.+)$").matcher("");
    private final Matcher uriMatcher = Pattern.compile("^<(?<uri>.+)>$").matcher("");
    private final Matcher valueMatcher =
        Pattern.compile("^\"(?<value>.+)\"(@(?<lang>\\w+))?(\\^\\^(?<type>.+))?$").matcher("");

    private YagoTsvParser(final String base, final String basePrefix, final Map<String, String> prefixes) {
        this.base = base;
        this.basePrefix = basePrefix;
        this.prefixes = prefixes;
    }

    private ParseException newParseException(final FileLine line, final int offset, final String message){
        return new ParseException("Encountered an invalid row on line " + line.number + ", position " + offset + " of " + line.path + ":" + message, offset);
    }

    @Override
    public MutableYagoFact transform(final FileLine line) throws ParseException {

        final MutableYagoFact fact = new MutableYagoFact();
        final ValueCache cache = new ValueCache();


        final String[] row = line.text.split("\t", 5);

        if (row.length < 4) {
            throw newParseException(line, 0, "The row has only " + row.length + " tab-delimited columns. Expected at least 4.");
        }

        scanningTokens:
        for (int k = 0; k < 4; k++) {
            // We're deliberately ignoring the 5th value column, because it contains no new information.

            final String token = row[k];
            cache.reset();

            int offset = k;
            for(int t = 0; t < k; t++) offset += row[t].length();

            if (k == 0 && token.isEmpty()) {
                // Fact ids are optional
                continue scanningTokens;
            }

            if (k == 3) {
                valueMatcher.reset(token);
                if (valueMatcher.matches()) {
                    cache.value = unescapeLiteral(valueMatcher.group("value"));
                    cache.lang = unescapeLiteral(valueMatcher.group("lang"));
                    final CharSequence rawType = unescapeLiteral(valueMatcher.group("type"));
                    if (rawType != null) {
                        parseReference(rawType, cache);
                        if (cache.namespace == null && cache.prefix != null) {
                            throw newParseException(line, offset, "The prefix \"" + cache.prefix + "\" is not recognised. Recognised prefixes are \"" + String.join("\", \"", prefixes.keySet()) + "\".");
                        }
                        if (cache.type == null) {
                            throw newParseException(line, offset, "Encountered \"" + rawType + "\", as the type of \"" + token + "\", which is not a valid type reference.");
                        }
                    } else {
                        cache.prefix = "xsd";
                        cache.namespace = prefixes.get("xsd");
                        cache.type = "string";
                    }

                    fact.valueTypeNamespace = cache.namespace;
                    fact.valueTypePrefix = cache.prefix;
                    fact.valueType = cache.type;
                    fact.valueLanguage = cache.lang;
                    fact.value = cache.value;

                    continue scanningTokens;
                }
            }

            parseReference(token, cache);
            if (cache.namespace == null && cache.prefix != null) {
                throw newParseException(line, offset, "The prefix \"" + cache.prefix + "\" is not recognised. Recognised prefixes are \"" + String.join("\", \"", prefixes.keySet())                                                +                                              "\".");
            }
            if (cache.type == null) {
                throw newParseException(line, offset, "Encountered \"" + token + "\", which is not a valid reference.");
            }

            switch (k) {
                case 0:
                    fact.factNamespace = cache.namespace;
                    fact.factPrefix = cache.prefix;
                    fact.factId = cache.type;
                    break;
                case 1:
                    fact.subjectNamespace = cache.namespace;
                    fact.subjectPrefix = cache.prefix;
                    fact.subject = cache.type;
                    break;
                case 2:
                    fact.predicateNamespace = cache.namespace;
                    fact.predicatePrefix = cache.prefix;
                    fact.predicate = cache.type;
                    break;
                case 3:
                    fact.objectNamespace = cache.namespace;
                    fact.objectPrefix = cache.prefix;
                    fact.object = cache.type;
                    break;
                default:
                    throw new IllegalArgumentException("Index " + k);
            }
        }

        return fact;
    }

    private void parseReference(final CharSequence token, final ValueCache cache) {
        uriMatcher.reset(token);
        if (uriMatcher.matches()) {
            final String uri = unescapeLiteral(uriMatcher.group("uri")).toString();
            if (uri.indexOf(':') >= 0) {
                if (uri.startsWith(base)) {
                    cache.prefix = basePrefix;
                    cache.namespace = base;
                    cache.type = uri.substring(base.length());
                } else {
                    boolean known = false;
                    for (String prefix : prefixes.keySet()) {
                        final String namespace = prefixes.get(prefix);
                        if (uri.startsWith(namespace)) {
                            cache.prefix = prefix;
                            cache.namespace = namespace;
                            cache.type = uri.substring(namespace.length());
                            known = true;
                            break;
                        }
                    }
                    if (!known) {
                        cache.prefix = null;
                        cache.namespace = null;
                        cache.type = uri;
                    }
                }
            } else {
                cache.prefix = basePrefix;
                cache.namespace = base;
                cache.type = uri;
            }
        } else {
            referenceMatcher.reset(token);
            if (referenceMatcher.matches()) {
                final String prefix = unescapeLiteral(referenceMatcher.group("prefix")).toString();
                cache.prefix = prefix;
                cache.namespace = prefixes.get(prefix);
                cache.type = unescapeLiteral(referenceMatcher.group("id"));
            } else {
                cache.prefix = null;
                cache.namespace = null;
                cache.type = null;
            }
        }
    }

    private CharSequence unescapeLiteral(final CharSequence escaped) {

        if (escaped == null) {
            return null;
        }

        // Following the escaping in this draft https://www.w3.org/TR/2001/WD-rdf-testcases-20010912/#ntrip_strings
        // The current recommendations for N-triples and RDF don't seem to actually specify this encoding. (?)

        // Note that this doesn't fix cases in YAGO where items have been incorrectly, inconsistently, or repetitively
        // escaped. We'll consider those 'upstream' problems, and fix them with a separate process.
        // See https://d2dcrc.atlassian.net/wiki/display/AP/Indexing+YAGO3

        escapedCharacterMatcher.reset(escaped);
        final StringBuffer result = new StringBuffer(escaped.length());
        while (escapedCharacterMatcher.find()) {
            final String backslashGroup = escapedCharacterMatcher.group("backslash");
            if (backslashGroup != null) {
                escapedCharacterMatcher.appendReplacement(result, Matcher.quoteReplacement("\\"));
            } else {
                final String lfGroup = escapedCharacterMatcher.group("lf");
                if (lfGroup != null) {
                    escapedCharacterMatcher.appendReplacement(result, Matcher.quoteReplacement("\n"));
                } else {
                    final String crGroup = escapedCharacterMatcher.group("cr");
                    if (crGroup != null) {
                        escapedCharacterMatcher.appendReplacement(result, Matcher.quoteReplacement("\r"));
                    } else {
                        final String tabGroup = escapedCharacterMatcher.group("tab");
                        if (tabGroup != null) {
                            escapedCharacterMatcher.appendReplacement(result, Matcher.quoteReplacement("\t"));
                        } else {
                            final String unicodeGroup = escapedCharacterMatcher.group("unicode");
                            if (unicodeGroup != null) {
                                escapedCharacterMatcher.appendReplacement(result, "");
                                result.append(Character.toChars(Integer.parseInt(unicodeGroup.substring(2), 16)));
                            } else {
                                // Should never happen
                                throw new RuntimeException(
                                    "Unknown escape pattern matched: " + escapedCharacterMatcher.group());
                            }
                        }
                    }
                }
            }
        }
        escapedCharacterMatcher.appendTail(result);
        return result;
    }
}
