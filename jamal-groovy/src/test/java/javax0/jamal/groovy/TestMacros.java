package javax0.jamal.groovy;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestMacros {

    @Test
    @DisplayName("Test a simple groovy eval")
    void testSimpleEval() throws Exception {
        TestThat.theInput("{@groovy:eval 6+3}").results("9");
    }

    @Test
    @DisplayName("Test the multiple evals do keep variables")
    void testSimpleMultipleEval() throws Exception {
        TestThat.theInput(
            "{@groovy:eval myVar = 3\n" +
                "2}" +
                "{@groovy:eval myVar}"
        ).results("23");
    }

    @Test
    @DisplayName("Test groovy can be started via eval as JSR")
    void testEvalJSRIntegration() throws Exception {
        TestThat.theInput(
            "{@eval/groovy def z = \"\"; (0..9).each{z+=it};z}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Test simple Groovy shell example")
    void testSimpleShell() throws Exception {
        TestThat.theInput(
            "{#sep {@escape `|`{% %}`|`}}\\\n" +
                "{%@import res:groovy.jim%}\\\n" +
                "{%@groovy:shell {%shell=engine%}\n" +
                "def z = \"\"; (0..9).each{z+=it};\n" +
                "z\n" +
                "%}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Test complex Groovy shell example")
    void testMultipleShell() throws Exception {
        TestThat.theInput(
            "{#sep {@escape `|`{% %}`|`}}{%@groovy:shell script\n" +
                "z = \"\";%}" +
                "{%@groovy:shell script\n" +
                "(0..9).each{z+=it};\n" +
                "z\n" +
                "%}"
        ).results("0123456789");
    }

    @Test
    @DisplayName("Test complex Groovy shell example with setting and getting a property")
    void testMultipleShellSetGetProperty() throws Exception {
        TestThat.theInput(
            "{#sep {@escape `|`{% %}`|`}}" +
                "{%@groovy:property z=%}" +
                "{%@groovy:shell engine script\n" +
                "(0..9).each{z+=it};\n" +
                "z\n" +
                "%}" +
                "{%@groovy:property z%}"
        ).results("01234567890123456789");
    }

    @Test
    @DisplayName("Test complex Groovy shell example with setting and getting casted property")
    void testMultipleShellSetGetCastedProperty() throws Exception {
        TestThat.theInput(
            "{#sep {@escape `|`{% %}`|`}}" +
                "{%@groovy:property z=(int)55%}" +
                "{%@groovy:shell script\n" +
                "z *= 2\n" +
                "(0..9).each{z+=it}\n" +
                "z\n" +
                "%}"
        ).results("155");
    }

    @Test
    @DisplayName("Test complex Groovy shell example with methods defined in the shell")
    void testMultipleShellMethods() throws Exception {
        TestThat.theInput(
            "{#sep {@escape `|`{% %}`|`}}" +
                "{%@groovy:import\n" +
                "class Main {\n" +
                "  static int increment(int x){\n" +
                "     x + 1\n" +
                "     }\n" +
                "  }\n" +
                "%}" +
                "{%@groovy:shell\n" +
                "Main.increment(13)" +
                "%}"
        ).results("14");
    }

    @Test
    @DisplayName("Test complex Groovy shell example with methods defined in the shell")
    void testMultipleShellMethodsImportResource() throws Exception {
        TestThat.theInput(
                "{%@groovy:import         res:increment.groovy%}\\\n" +
                "{%@groovy:shell\n" +
                "Main.increment(13)" +
                "%}"
        ).usingTheSeparators("{%", "%}").results("14");
    }

    @Test
    @DisplayName("Test complex Groovy shell example with methods defined in the shell")
    void testCatchGroovyErrorWithTryMacro() throws Exception {
        TestThat.theInput(
            "{%@try! {%@groovy:eval z%}%}"
        ).usingTheSeparators("{%", "%}").results("Error evaluating groovy script using eval");
    }
}
