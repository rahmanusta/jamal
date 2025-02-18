package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.regex.Pattern;

public class KillLines implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pattern = Params.holder("kill", "pattern").orElse("^\\s*$").asPattern();
        final var keep = Params.<Boolean>holder("keep").asBoolean();
        Scan.using(processor).from(this).firstLine().keys(pattern, keep).parse(in);

        convertTextBlock(in.getSB(), in.getPosition(), pattern, keep);
        return in.toString();
    }

    @Override
    public String getId() {
        return "killLines";
    }

    @Override
    public void convertTextBlock(final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(2, params);
        final var pattern = params[0].asType(Pattern.class);
        final var keep = params[1].asBoolean();
        final var lines = sb.toString().split("\n", -1);
        int from = 0, to = 0;
        boolean lastLineCopied = false;
        while (from < lines.length) {
            if (keep.is() == pattern.get().matcher(lines[from]).find()) {
                lines[to] = lines[from];
                lastLineCopied = from == lines.length - 1;
                to++;
            }
            from++;
        }
        SkipLines.joinLines(sb, lines, to, lastLineCopied);
    }

}
