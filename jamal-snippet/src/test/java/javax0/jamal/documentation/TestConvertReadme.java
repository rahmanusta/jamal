package javax0.jamal.documentation;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

public class TestConvertReadme {

    @Test
    void convertSnippetArticle() throws Exception {
        DocumentConverter.convert("./ARTICLE.wp.jam");
    }

    @Test
    void convertWritingBuiltIn() throws Exception {
        DocumentConverter.convert("../BUILTIN.adoc.jam");
    }

    @Test
    void convertParams() throws Exception {
        DocumentConverter.convert("../PARAMS.adoc.jam");
    }

    @Test
    void convertGlossary() throws Exception {
        DocumentConverter.convert("../GLOSSARY.adoc.jam");
    }

    @Test
    void convertReleases() throws Exception {
        DocumentConverter.convert("../RELEASES.adoc.jam");
    }

    @Test
    void convertTopReadme() throws Exception {
        DocumentConverter.convert("../README.adoc.jam");
    }

    @Test
    void convertTutorials() throws Exception {
        DocumentConverter.convert("../TUTORIALS.adoc.jam");
    }

    @Test
    void convertDebugReadme() throws Exception {
        DocumentConverter.convert("../jamal-debug/README.adoc.jam");
    }

    @Test
    void convertExtensionReadme() throws Exception {
        DocumentConverter.convert("../jamal-extensions/README.adoc.jam");
    }

    @Test
    void convertMavenExtensionReadme() throws Exception {
        DocumentConverter.convert("../jamal-maven-extension/README.adoc.jam");
    }

    @Test
    void convertMavenPluginReadme() throws Exception {
        DocumentConverter.convert("../jamal-maven-plugin/README.adoc.jam");
    }

    @Test
    void convertScriptBasicReadme() throws Exception {
        DocumentConverter.convert("../jamal-scriptbasic/README.adoc.jam");
    }

    @Test
    void convertTestReadme() throws Exception {
        DocumentConverter.convert("../jamal-test/README.adoc.jam");
    }

    @Test
    @DisplayName("Convert the Snippet module README.adoc.jam -> README.adoc")
    void convertSnippetReadme() throws Exception {
        DocumentConverter.convert("../jamal-snippet/README.adoc.jam");
    }

    @Test
    @DisplayName("Convert the Snippet module THINXML.adoc.jam -> THINXML.adoc")
    void convertThinXml() throws Exception {
        DocumentConverter.convert("../jamal-snippet/THINXML.adoc.jam");
    }
}
