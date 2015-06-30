package org.oscii.detokenize;

import com.google.common.collect.Iterators;
import edu.stanford.nlp.mt.process.Preprocessor;
import edu.stanford.nlp.mt.process.de.GermanPreprocessor;
import edu.stanford.nlp.mt.process.en.EnglishPreprocessor;
import edu.stanford.nlp.mt.process.es.SpanishPreprocessor;
import edu.stanford.nlp.mt.process.fr.FrenchPreprocessor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Command-line utility to train a detokenizer.
 */
public class TrainDetokenizer {
    private final static Logger log = LogManager.getLogger(TrainDetokenizer.class);

    public static void main(String[] args) throws IOException {
        OptionSet options = parse(args);

        Stream<String> rawCorpus = getLines(options, "raw");
        TokenizedCorpus corpus;
        if (options.has("tokenized")) {
            corpus = new TokenizedCorpus.ParallelCorpus(rawCorpus, getLines(options, "tokenized"));
        } else {
            Preprocessor preprocessor = getPreprocessor(options);
            corpus = new TokenizedCorpus.PreprocessorCorpus(preprocessor, rawCorpus);
        }

        // Split corpus into test and trainining
        Iterator<TokenizedCorpus.Entry> all = corpus.stream().iterator();
        int testSize = (Integer) options.valueOf("testsize");
        List<TokenizedCorpus.Entry> test = new ArrayList<>(testSize);
        for (int i = 0; i < testSize; i++) {
            test.add(all.next());
        }
        int trainSize = (Integer) options.valueOf("trainsize");
        Iterator<TokenizedCorpus.Entry> training = trainSize == 0 ? all : Iterators.limit(all, trainSize);

        double regularization = (double) options.valueOf("regularization");
        Detokenizer detokenizer = Detokenizer.train(regularization, training);
        logCounts("Trained", detokenizer);

        if (testSize > 0) {
            detokenizer.resetCounts();
            long start = System.currentTimeMillis();
            double accuracy = detokenizer.evaluate(test.iterator());
            double duration = .001 * (System.currentTimeMillis() - start);
            logCounts("Tested", detokenizer);
            log.info("Test accuracy: " + accuracy);
            log.info("Test segments/second: " + (test.size() / duration));

            if (options.has("errors")) {
                test.forEach(ex -> {
                    List<String> tokens = ex.getTokens();
                    List<TokenLabel> labels = detokenizer.predictLabels(tokens);
                    String roundTrip = TokenLabel.render(tokens, labels);
                    if (!ex.getRaw().equals(roundTrip)) {
                        log.info("Original:  " + ex);
                        log.info("Detoken'd: " + roundTrip);
                    }
                });
            }
        }

        if (options.has("out")) {
            File outFile = (File) options.valueOf("out");
            detokenizer.save(outFile);
        }
    }

    private static void logCounts(String set, Detokenizer detokenizer) {
        int a = detokenizer.getAccepted();
        int s = detokenizer.getSkipped();
        int t = a + s;
        double ap = 100.0 * a / t;
        log.info(String.format("%s on %d (%.2f%%) of %d examples", set, a, ap, t));
    }

    /*
     * Get lines of a file (maybe gzipped) from a command line option.
     */
    private static Stream<String> getLines(OptionSet options, String option) throws IOException {
        File trainFile = (File) options.valueOf(option);
        InputStream trainStream = new FileInputStream(trainFile);
        if (trainFile.getName().endsWith(".gz")) {
            trainStream = new GZIPInputStream(trainStream);
        }
        BufferedReader buffered = new BufferedReader(new InputStreamReader(trainStream, "utf-8"));
        return buffered.lines();
    }

    private static Preprocessor getPreprocessor(OptionSet options) {
        Preprocessor preprocessor = null;
        boolean cased = true;
        switch (((String) options.valueOf("language")).toLowerCase()) {
            case "de":
            case "german":
                preprocessor = new GermanPreprocessor(cased);
                break;
            case "en":
            case "english":
                preprocessor = new EnglishPreprocessor(cased);
                break;
            case "es":
            case "spanish":
                preprocessor = new SpanishPreprocessor(cased);
                break;
            case "fr":
            case "french":
                preprocessor = new FrenchPreprocessor(cased);
                break;
        }
        return preprocessor;
    }

    private static OptionSet parse(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("language", "Language").withRequiredArg().defaultsTo("de");
        parser.accepts("raw", "Path to raw segments file").withRequiredArg().ofType(File.class);
        parser.accepts("tokenized", "Path to tokenized segments file").withRequiredArg().ofType(File.class);
        parser.accepts("out", "Path to serialized detokenizer").withRequiredArg().ofType(File.class);
        parser.accepts("trainsize", "Max training size (0 for unlimited)").withRequiredArg().ofType(Integer.class).defaultsTo(0); // Unlimited
        parser.accepts("testsize", "Max test size (0 to skip testing)").withRequiredArg().ofType(Integer.class).defaultsTo(10000);
        parser.accepts("regularization").withRequiredArg().ofType(Double.class).defaultsTo(10.0);
        parser.accepts("errors");
        parser.acceptsAll(Arrays.asList("h", "help")).forHelp();

        OptionSet options = parser.parse(args);
        if (options.has("help") || options.has("h")) {
            parser.printHelpOn(System.out);
            System.exit(0);
        }
        return options;
    }
}

