package javax0.jamal.documentation;

import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertReadme {

    private static void generateAdoc(String directory) throws Exception {
        generateAdoc(directory, "README");
    }

    private static void generateAdoc(final String directory, final String fileName) throws Exception {
        generateDoc(directory, fileName, "adoc");
    }

    private static void generateDoc(final String directory, final String fileName, final String ext) throws Exception {
        final var in = FileTools.getInput(directory + "/" + fileName + "." + ext + ".jam");
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        FileTools.writeFileContent(directory + "/" + fileName + "." + ext, result);
    }

    @Test
    void convertSnippetArticle() throws Exception {
        generateDoc(".", "ARTICLE", "wp");
    }

    @Test
    void convertWritingBuiltIn() throws Exception {
        generateAdoc("..", "BUILTIN");
    }

    @Test
    void convertGlossary() throws Exception {
        generateAdoc("..", "GLOSSARY");
    }

    @Test
    void convertTopReadme() throws Exception {
        generateAdoc("..");
    }

    @Test
    void convertExtensionReadme() throws Exception {
        generateAdoc("../jamal-extensions");
    }

    @Test
    void convertScriptBasicReadme() throws Exception {
        generateAdoc("../jamal-scriptbasic");
    }

    @Test
    void convertTestReadme() throws Exception {
        generateAdoc("../jamal-test");
    }

    @Test
    void convertSnippetReadme() throws Exception {
        generateAdoc("../jamal-snippet");
    }
}
