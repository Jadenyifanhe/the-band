package edu.cmu.cs.cs214.dataPlugins;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The track class is the objects to be processed in the framework.
 * It store the content of the data and the sentiment analysis result.
 * A playlist is a list of track objects.
 */
public class Track {
    /**
     * The title of the track.
     */
    private String title;

    /**
     * The artist of the track.
     */
    private String artist;

    /**
     * The genre of the track.
     */
    private List<String> genre;

    /**
     * The timestamp when the track was played/Added.
     */
    private String timeStamp;

    /**
     * The Sentiment analysis result for the text content.
     */
    private double[] sentimentScore;

    /**
     * The constructor for Track object.
     *
     * @param content The text content.
     */
    public Track(String title, String artist, List<String> genre, String timePlayed, double[] sentimentScore) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.timeStamp = timePlayed;
        this.sentimentScore = sentimentScore;
    }

    /**
     * Get the title of the Track.
     *
     * @return The title of Track object.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Get the artist of the Track.
     *
     * @return The artist of Track object.
     */
    public String getArtist() {
        return this.artist;
    }

    /**
     * Get the genre of the Track.
     *
     * @return The genre of Track object.
     */
    public List<String> getGenre() {
        return this.genre;
    }

    /**
     * Get the time played of the Track.
     *
     * @return The time played of Track object.
     */
    public String getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Get the sentiment analysis score of the Track.
     *
     * @return The sentiment analysis score.
     */
    public double[] getSentimentScore() {
        return this.sentimentScore;
    }

    @Override
    public String toString() {
        String genreString = genre.stream()
                .map(g -> "\"" + g + "\"")
                .collect(Collectors.joining(", "));

        return ("{\"title\": \"" + this.title.replace("\"", "") + "\"," +
                " \"genre\": [" + genreString + "]," +
                " \"artist\": \"" + this.artist + "\"," +
                " \"timestamp\": \"" + this.timeStamp + "\"," +
                " \"score\": " + Arrays.toString(this.sentimentScore) + "}").replace("null", "");

    }

}
