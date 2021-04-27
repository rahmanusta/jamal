package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSnippets {

    @Test
    @DisplayName("Finds all files and collects all snippets and inserts them into the output")
    void testSimpleCase() throws Exception {
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/test1.jam}")
            .results("\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "2. snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "Next file\n" +
                "\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "Second snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "and this is the end");
    }

    @Test
    @DisplayName("List all the snippets, all or only some of them")
    void testListSnippets() throws Exception {
        /* This test fails if you accidentally run Jamal for all *.jam files on the source tree and it generates
         *
         * /jamal-snippet/src/test/resources/javax0/jamal/snippet/test{X}
         *
         * files for X=1, 2, 3, 4.
         * DELETE THEM.
         * */
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/test4.jam}")
            .results(
                "first_snippet,second_snippet,second_file_first$snippet,seconda_snippet_uniconde\n" +
                    "first_snippet,second_file_first$snippet\n" +
                    "first_snippet,second_snippet\n" +
                    "second_snippet,seconda_snippet_uniconde"
            );
    }

    @Test
    @DisplayName("Finds files that match the 'include' and collects all snippets and inserts them into the output")
    void testSimpleCaseWithInclude() throws Exception {
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/test2.jam}")
            .results("\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "2. snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "Next file\n" +
                "Snippet 'second_file_first$snippet' is not defined\n" +
                "and this is the end");
    }

    @Test
    @DisplayName("Finds files that does not match the 'exclude' and collects all snippets and inserts them into the output")
    void testSimpleCaseWithExclude() throws Exception {
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/test3.jam}")
            .results("\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "2. snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "Next file\n" +
                "Snippet 'second_file_first$snippet' is not defined\n" +
                "and this is the end");
    }

    @Test
    @DisplayName("Finds files that does not match the 'exclude' and collects all snippets and inserts them into the output")
    void testPropertySnippets() throws Exception {
        TestThat.theInput("{@snip:properties src/test/resources/javax0/jamal/snippet/testproperties.properties}\n" +
            "{@snip a}\n" +
            "{@snip b}\n" +
            "{@snip c}"
        )
            .results("\n" +
                "letter a\n" +
                "letter b\n" +
                "letter c");
    }

    @Test
    @DisplayName("Finds files that does not match the 'exclude' and collects all snippets and inserts them into the output")
    void testPropertySnippetsFromXml() throws Exception {
        TestThat.theInput("{@snip:properties src/test/resources/javax0/jamal/snippet/testproperties.xml}\n" +
            "{@snip a}\n" +
            "{@snip b}\n" +
            "{@snip c}"
        )
            .results("\n" +
                "letter a\n" +
                "letter b\n" +
                "letter c");
    }
}
