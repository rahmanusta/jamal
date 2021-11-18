package javax0.jamal.jamal;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.EnvironmentVariables;
import org.junit.jupiter.api.Test;

public class TestConvertJamalJamalReadme {

    @Test
    void generateDoc() throws Exception {
        EnvironmentVariables.setenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV,"false");
        DocumentConverter.convert("./README.adoc.jam");
        EnvironmentVariables.resetenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV);
    }

}
