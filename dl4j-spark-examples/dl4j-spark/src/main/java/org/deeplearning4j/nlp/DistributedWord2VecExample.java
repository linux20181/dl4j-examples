package org.deeplearning4j.nlp;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.spark.models.word2vec.SparkWord2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

/**
 * This example shows how to build Word2Vec model with distributed p2p ParameterServer.
 *
 * PLEASE NOTE: This example is NOT meant to be run on localhost, consider spark-submit ONLY
 *
 * @author raver119@gmail.com
 */
public class DistributedWord2VecExample {

    @Parameter(names = {"-l","--layer"}, description = "Word2Vec layer size")
    protected int layerSize = 100;

    @Parameter(names = {"-s", "--shards"}, description = "Number of ParameterServer Shards")
    protected int numShards = 2;

    @Parameter(names = {"-t","--text"}, description = "HDFS path to training corpus")
    protected String corpusTextFile;

    @Parameter(names = {"-x"}, description = "Launch locally (NOT RECOMMENDED!)", arity = 1)
    protected boolean useSparkLocal = true;


    public void entryPoint(String[] args) {
        JCommander jcmdr = new JCommander(this);
        try {
            jcmdr.parse(args);
        } catch (ParameterException e) {
            //User provides invalid input -> print the usage info
            jcmdr.usage();
            try { Thread.sleep(500); } catch (Exception e2) { }
            throw e;
        }



        SparkConf sparkConf = new SparkConf();
        if (useSparkLocal) {
            sparkConf.setMaster("local[*]");
        }
        sparkConf.setAppName("DL4j Spark Word2Vec + ParameterServer example");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> corpus = sc.textFile(corpusTextFile);

        SparkWord2Vec word2Vec = new SparkWord2Vec.Builder()
            .setTokenizerFactory(new DefaultTokenizerFactory())
            .build();

        word2Vec.fitSentences(corpus);
    }

    public static void main(String[] args) throws Exception {
        new DistributedWord2VecExample().entryPoint(args);
    }
}
