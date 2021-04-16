package javax0.jamal.snake;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Test;

public class TestConvertYamlReadme {

    @Test
    void generateDoc() throws Exception {
        final var in = FileTools.getInput("./README.adoc.jam");
        final var processor = new Processor("{%", "%}");
        final var result = processor.process(in);
        FileTools.writeFileContent("./README.adoc", result);
    }

}
