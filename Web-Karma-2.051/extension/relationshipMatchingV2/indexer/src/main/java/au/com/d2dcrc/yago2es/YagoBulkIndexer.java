package au.com.d2dcrc.yago2es;

import com.google.common.io.CharStreams;

import au.id.ajlane.iostreams.FileLineIOStream;
import au.id.ajlane.iostreams.FilterDecision;
import au.id.ajlane.iostreams.IOStream;
import au.id.ajlane.iostreams.IOStreamCloseException;
import au.id.ajlane.iostreams.IOStreamReadException;
import au.id.ajlane.iostreams.IOStreams;
import au.id.ajlane.iostreams.PeekableIOStream;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Indexes the subjects in a set of YAGO-dump .tsv files with Elasticsearch.
 */
public class YagoBulkIndexer {

    private enum Mode {
        FORWARD,
        REVERSE
    }

    /**
     * Summarises the indexer's progress.
     */
    public static class IndexingSummary {

        /**
         * The number of actions that the indexer has included in its bulk requests.
         */
        public long actionCount = 0;
        /**
         * The number of facts that the indexer has encountered.
         */
        public long factCount = 0;
        /**
         * The number of files that the indexer has encountered.
         */
        public long fileCount = 0;
        /**
         * The number of actions that the server refused to perform.
         */
        public long refusalCount = 0;
        /**
         * The number of bulk requests that the indexer sent to the server.
         */
        public long requestCount = 0;
    }

    private static class NamedDocument {

        public Map<String, Object> document;
        public String id;

        public NamedDocument() {
        }

        public void clear() {
            id = null;
            document = null;
        }
    }

    private static final String ELASTIC_SEARCH_MERGE_SCRIPT = "upsert_jsonld";
    private static final Logger LOGGER = LoggerFactory.getLogger(YagoBulkIndexer.class);

    /**
     * Entry-point for the indexer. <p>Parsing errors will be ignored and printed to standard error.</p>
     *
     * @param args The application arguments.
     * @throws Exception If the indexer fails.
     */
    public static void main(final String... args) throws Exception {

        if (args.length < 7) {
            printUsageAndExit();
        }

        final Settings clientSettings = Settings.builder()
            .put("cluster.name", args[2])
            .build();

        final InetSocketTransportAddress clientAddress =
            new InetSocketTransportAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));

        final String indexName = args[3];
        final String documentType = args[4];

        final int batchSize = Integer.parseInt(args[5]);

        final Mode mode;
        if (args[6].equals("@reverse")) {
            mode = Mode.REVERSE;
        } else {
            mode = Mode.FORWARD;
        }

        if (mode == Mode.REVERSE && args.length < 8) {
            printUsageAndExit();
        }

        LOGGER.info("Connecting to {} on {}.", args[2], clientAddress);

        final YagoBulkIndexer indexer = new YagoBulkIndexer(mode, indexName, documentType, batchSize);
        try (
            final Client client = TransportClient.builder()
                .settings(clientSettings)
                .build()
                .addTransportAddress(clientAddress)
        ) {
            final int fileNameArgsStart = mode == Mode.REVERSE ? 7 : 6;
            final int fileNameArgsEnd = args.length;


            IOStream<Path> files = IOStreams.fromArray(Arrays.copyOfRange(args, fileNameArgsStart, fileNameArgsEnd))
                    .map((String arg) -> Paths.get(arg));
            final IndexingSummary summary = indexer.index(client, files);

            LOGGER.info("Processed {} facts from {} files with {} actions ({} failed) over {} requests.",
                        summary.factCount, summary.fileCount, summary.actionCount, summary.refusalCount,
                        summary.requestCount);
        }
    }

    private static void createSubjectsIndex(Client client, String indexName, String documentType) throws IOException {
        final String mapping;
        try(final InputStreamReader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream("mapping.json"))){
            mapping = CharStreams.toString(reader);
        }
        try {
            client.admin().indices().prepareCreate(indexName).addMapping(documentType, mapping).get();
        } catch (final IndexAlreadyExistsException ex){
            // We'll assume the mapping is correct rather than trying to overwrite it.
            LOGGER.info("The subjects index already exists.");
        }
    }

    private static void printUsageAndExit() {
        System.err.println(
            "Usage: YagoBulkIndexer <elasticsearch host> <elasticsearch port> <elasticsearch cluster name> <elasticserch index name> <elasticsearch doc type> <batch size> <forward/reverse> <@reverse>? <yago .tsv file>+");
        System.err.println("Examples:");
        System.err.println(
            "\t YagoBulkIndexer locahost 9300 elasticsearch subjects subject 1000 yagoTaxonomy.tsv yagoFacts.tsv yagoLiteralFacts.tsv yagoLabels.tsv yagoSources.tsv");
        System.err.println(
            "\t YagoBulkIndexer locahost 9300 elasticsearch subjects subject 1000 @reverse yagoTaxonomy.tsv yagoFacts.tsv yagoLiteralFacts.tsv yagoLabels.tsv yagoSources.tsv");
        System.exit(1);
    }

    private final int batchSize;
    private final String documentType;
    private final String indexName;
    private final Mode mode;
    private final JsonLdUpdater updater = new JsonLdUpdater();

    /**
     * Initialises a new indexer.
     *
     * @param mode The mode for the indexer. Reverse-mode will add reverse links to all of the objects' documents.
     * @param indexName The name of the elasticsearch index.
     * @param documentType The document type for the elasticsearch documents.
     * @param batchSize The maximum size of bulk requests to the elasticsearch server.
     */
    public YagoBulkIndexer(final Mode mode, final String indexName, final String documentType, final int batchSize) {
        this.mode = mode;
        this.indexName = indexName;
        this.documentType = documentType;
        this.batchSize = batchSize;
    }

    /**
     * Indexes the facts in the given files.
     * <p>In forward-mode, this creates a document for every subject. In reverse mode, this creates a document for every non-literal object.</p>
     * <p>In either mode, the indexer will take advantage of the sort-order of the files to batch adjacent facts with the same subject (or object).</p>
     *
     * @param client The elasticsearch client to use.
     * @param files The set of files to process.
     * @return A summary of the actions taken by the indexer.
     * @throws IOException If there is a problem reading the files, communicating with the server, of if the thread is interrupted.
     */
    public IndexingSummary index(final Client client, final IOStream<Path> files) throws IOException {

        final IndexingSummary summary = new IndexingSummary();

        createSubjectsIndex(client, indexName, documentType);

        final JsonLdUpdater jsonLdUpdater = new JsonLdUpdater();
        final NamedDocument reusableNamedDocument = new NamedDocument();

        files
            .observe(file -> {
                LOGGER.info("Reading {}.", file);
                summary.fileCount++;
            })
            // Read each line from each file
            .flatMap(file -> FileLineIOStream.fromFile(file, StandardCharsets.UTF_8))
            // Parse each line as a fact, skipping any invalid rows
            .map(YagoTsvParser.withDefaultNamespaces(), (row, ex) -> {
                LOGGER.error("Skipping row [{}]", row, ex);
                return FilterDecision.SKIP_AND_CONTINUE;
            })
            .observe(fact -> summary.factCount++)
            // If we're only processing reverse links, we can skip any facts with literal values
            .filter(fact -> {
                switch (mode) {
                    case FORWARD:
                        return FilterDecision.KEEP_AND_CONTINUE;
                    case REVERSE:
                        return fact.object != null ? FilterDecision.KEEP_AND_CONTINUE
                                                   : FilterDecision.SKIP_AND_CONTINUE;
                    default:
                        throw new IllegalArgumentException(mode.name());
                }
            })
            // Group adjacent facts with the same subject
            .group((a, b) -> {
                switch (mode) {
                    case FORWARD:
                        return Objects.equals(a.subjectNamespace, b.subjectNamespace) && Objects
                            .equals(a.subject, b.subject);
                    case REVERSE:
                        return Objects.equals(a.objectNamespace, b.objectNamespace) && Objects
                            .equals(a.object, b.object);
                    default:
                        throw new IllegalArgumentException(mode.name());
                }
            })
            // Convert groups of facts into named documents
            .map(facts -> fillNamedDocument(reusableNamedDocument, facts))
            // Batch documents together
            .group(batchSize)
            // Convert batches of documents into bulk requests
            .map(documents -> buildBulkRequest(client, documents))
            // Submit requests
            .observe(request -> {
                summary.actionCount += request.numberOfActions();
                summary.requestCount++;
                LOGGER.info("Submitting bulk request #{}... ({} facts read so far)", summary.requestCount,
                            summary.factCount);
            })
            .flatMap(request -> IOStreams.fromIterable(client.bulk(request).get()))
            // Monitor for failures - exit early if we're failing a lot.
            .filter(response -> {
                if (response.isFailed()) {
                    LOGGER.error("Failed to update {}: {}.", response.getId(), response.getFailureMessage(),
                                 response.getFailure().getCause());
                    if (++summary.refusalCount >= summary.actionCount * 0.1) {
                        LOGGER.error("At least 10% of all actions have failed. Aborting.");
                        return FilterDecision.SKIP_AND_TERMINATE;
                    }
                    return FilterDecision.SKIP_AND_CONTINUE;
                }
                return FilterDecision.KEEP_AND_CONTINUE;
            })
            .consume();

        return summary;
    }

    private BulkRequest buildBulkRequest(final Client client, final IOStream<NamedDocument> documents)
        throws IOStreamReadException, IOStreamCloseException {
        return documents.fold(client.prepareBulk(), (request, namedDocument) -> request.add(
            new UpdateRequest(indexName, documentType, namedDocument.id)
                .scriptedUpsert(true)
                .upsert(Collections.emptyMap())
                .script(
                    new Script(
                        ELASTIC_SEARCH_MERGE_SCRIPT,
                        ScriptService.ScriptType.INLINE,
                        "native",
                        namedDocument.document)
                )
        )).request();
    }

    private NamedDocument fillNamedDocument(final NamedDocument reusableNamedDocument,
                                            final IOStream<MutableYagoFact> facts)
        throws IOStreamCloseException, IOStreamReadException {
        try (final PeekableIOStream<MutableYagoFact> peekableFacts = facts.peekable()) {
            // Peek at the first fact so we can work out what the document id should be
            final MutableYagoFact first = peekableFacts.peek();
            final String id;
            switch (mode) {
                case FORWARD:
                    id = JsonLdBuilder.resolvedSubjectId(first);
                    break;
                case REVERSE:
                    id = JsonLdBuilder.resolvedObjectId(first);
                    break;
                default:
                    throw new IllegalStateException(mode.name());
            }
            reusableNamedDocument.id = id;
            // Convert all of the facts in the group to JSON-LD and use the updater to merge them.
            reusableNamedDocument.document = peekableFacts.fold(
                JsonLdUpdater.initial(),
                this::updateJsonLd
            );
            return reusableNamedDocument;
        }
    }

    private Map<String, Object> updateJsonLd(final Map<String, Object> existing, final MutableYagoFact fact) {
        switch (mode) {
            case FORWARD:
                updater.update(existing, JsonLdBuilder.buildSubjectDocument(fact));
                break;
            case REVERSE:
                updater.update(existing, JsonLdBuilder.buildObjectDocument(fact));
                break;
            default:
                throw new IllegalArgumentException(mode.name());
        }
        return existing;
    }
}
