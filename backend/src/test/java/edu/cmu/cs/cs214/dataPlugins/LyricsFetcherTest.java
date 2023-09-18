package edu.cmu.cs.cs214.dataPlugins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for LyricsFetcher
 */
public class LyricsFetcherTest {
    private LyricsFetcher lyricsFetcher;

    @BeforeEach
    public void setUp() {
        lyricsFetcher = new LyricsFetcher();
    }

    @Test
    public void testFetchLyrics() {
        String lyrics = lyricsFetcher.fetchLyrics("Bohemian Rhapsody", "Queen");
        assertNotNull(lyrics);
        assertFalse(lyrics.isEmpty());
    }

    @Test
    public void testFetchLyricsWithInvalidInput() {
        String lyrics = lyricsFetcher.fetchLyrics("Nonexistent Song", "Nonexistent Artist");
        assertNotNull(lyrics);
        assertTrue(lyrics.isEmpty());
    }
}
