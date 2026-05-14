package com.envguard.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PatternLoaderTest {

    private final PatternLoader loader = new PatternLoader();

    @TempDir
    Path tempDir;

    @Test
    void loadFromPath_parsesValidPatterns() throws IOException {
        Path patternsFile = tempDir.resolve("patterns.txt");
        Files.writeString(patternsFile,
                "# comment line\n" +
                "\n" +
                "AWS_SECRET_ACCESS_KEY\\s*=\\s*[\\w/+]{40}\n" +
                "password\\s*=\\s*.+\n"
        );

        List<Pattern> patterns = loader.loadFromPath(patternsFile);

        assertEquals(2, patterns.size());
    }

    @Test
    void loadFromPath_skipsCommentsAndBlankLines() throws IOException {
        Path patternsFile = tempDir.resolve("patterns.txt");
        Files.writeString(patternsFile,
                "# this is a comment\n" +
                "   \n" +
                "# another comment\n" +
                "api_key\\s*=\\s*.+\n"
        );

        List<Pattern> patterns = loader.loadFromPath(patternsFile);

        assertEquals(1, patterns.size());
    }

    @Test
    void loadFromPath_skipsInvalidRegexWithoutThrowing() throws IOException {
        Path patternsFile = tempDir.resolve("patterns.txt");
        Files.writeString(patternsFile,
                "valid_pattern\\s*=.+\n" +
                "[invalid regex(\n" +
                "another_valid\\s*=.+\n"
        );

        List<Pattern> patterns = loader.loadFromPath(patternsFile);

        assertEquals(2, patterns.size());
    }

    @Test
    void loadFromPath_throwsWhenFileDoesNotExist() {
        Path missing = tempDir.resolve("nonexistent.txt");
        assertThrows(IOException.class, () -> loader.loadFromPath(missing));
    }

    @Test
    void loadFromPath_returnsEmptyListForEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        List<Pattern> patterns = loader.loadFromPath(emptyFile);

        assertTrue(patterns.isEmpty());
    }

    @Test
    void loadFromPath_returnsUnmodifiableList() throws IOException {
        Path patternsFile = tempDir.resolve("patterns.txt");
        Files.writeString(patternsFile, "secret\\s*=.+\n");

        List<Pattern> patterns = loader.loadFromPath(patternsFile);

        assertThrows(UnsupportedOperationException.class, () -> patterns.add(Pattern.compile("test")));
    }
}
