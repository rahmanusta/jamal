package javax0.jamal.jamal;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.Macro;
import org.junit.jupiter.api.Test;

public class TestConvertJamalJamalReadme {

    @Test
    void generateDoc() throws Exception {
        System.setProperty(Macro.JAMAL_CHECKSTATE_SYS,"false");
        DocumentConverter.convert("./README.adoc.jam");
    }

}
