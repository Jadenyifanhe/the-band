package edu.cmu.cs.cs214.core;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test class for Analyze Sentiment
 * Test might fail if the credentials path is not set correctly
 */
public class AnalyzeTest {
    private static final String JSON_KEY;

    static {
        String tempKey;
        try {
            tempKey = new String(Files.readAllBytes(Paths.get("src/main/java/edu/cmu/cs/cs214/sentimentConfig.json")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            tempKey = "";
            System.err.println("Error reading JSON key file: " + e.getMessage());
        }
        JSON_KEY = tempKey;
    }

@Test
public void testAnalyzeSentimentText() {
    String positiveText = "I love this product!";
    String negativeText = "I hate this product!";

    double[] positiveSentiment = Analyze.analyzeSentimentText(positiveText, JSON_KEY);
    double[] negativeSentiment = Analyze.analyzeSentimentText(negativeText, JSON_KEY);

    assertTrue("Positive sentiment score should be greater than 0", positiveSentiment[1] > 0);
    assertTrue("Negative sentiment score should be less than 0", negativeSentiment[1] < 0);
    }
}
