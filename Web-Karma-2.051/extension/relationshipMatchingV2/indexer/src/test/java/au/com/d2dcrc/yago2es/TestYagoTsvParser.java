package au.com.d2dcrc.yago2es;

import au.id.ajlane.iostreams.FileLineIOStream;
import au.id.ajlane.iostreams.IOStreams;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestYagoTsvParser {


    public void testCanParseWithoutError() throws Exception {

        final YagoTsvParser parser = YagoTsvParser.withDefaultNamespaces();

        final List<MutableYagoFact> facts = new ArrayList<>();

        IOStreams.fromArray(YagoSample.PATHS)
            .map(file -> FileLineIOStream.fromClasspath(TestYagoTsvParser.class, file, StandardCharsets.UTF_8))
            .consume(lines -> lines.map(parser)
                .consume(fact -> facts.add(fact.clone())));

        Assert.assertEquals(8800, facts.size());
    }
}
