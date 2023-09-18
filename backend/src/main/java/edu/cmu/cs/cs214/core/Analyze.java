package edu.cmu.cs.cs214.core;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class for analyzing sentiment of text
 * using Google Cloud Natural Language API
 */
public class Analyze {

    public static double[] analyzeSentimentText(String text, String jsonKey) {
        try (InputStream credentialsStream = new ByteArrayInputStream(jsonKey.getBytes())) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
                Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
                AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
                Sentiment sentiment = response.getDocumentSentiment();
                if (sentiment == null) {
                    System.out.println("No sentiment found");
                } else {
                    // System.out.printf("Sentiment magnitude: %.3f\n", sentiment.getMagnitude());
                    // System.out.printf("Sentiment score: %.3f\n", sentiment.getScore());
                    return new double[] {sentiment.getMagnitude(), sentiment.getScore()};
                }
            } catch (IOException e) {
                System.out.println("Error in analyzeSentimentText: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Error loading credentials: " + e.getMessage());
        }
        return new double[] {0, 0};
    }
}
