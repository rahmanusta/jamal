package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Debuggable;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.InputHandler;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.getParts;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Macros defined in static inner classes that some way help handling regular expressions.
 */
public class Regex {

    /**
     * This macro splits the input into three parts using {@link InputHandler#getParts(Input)} and then returns {@code
     * }part[0].replaceAll(part[2], part[3])}
     */
    public static class ReplaceAll implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var part = getParts(in);
            BadSyntax.when(part.length != 3,  "replaceAll needs exactly 3 parts separated but the input has %d", part.length);
            return part[0].replaceAll(part[1], part[2]);
        }

        @Override
        public String getId() {
            return "replaceAll";
        }
    }

    private static class GroupUserDefinedMacro implements UserDefinedMacro {

        final String name;
        final java.util.regex.Matcher matcher;

        @Override
        public Optional<Debuggable.UserDefinedMacro> debuggable() {
            return Optional.empty();
        }

        GroupUserDefinedMacro(String name, java.util.regex.Matcher matcher) {
            this.name = name;
            this.matcher = matcher;
        }

        @Override
        public String getId() {
            return name;
        }

        @Override
        public String evaluate(String... parameters) throws BadSyntax {
            BadSyntax.when(parameters.length == 0,  "The generated macro %s needs at least one argument", name);
            var command = parameters[0].trim();
            final String arg;
            if (parameters.length > 1) {
                arg = parameters[1];
            } else {
                arg = null;
            }
            try {
                switch (command) {
                    case "nr":
                        return "" + matcher.groupCount();
                    case "groupIndices":
                        return IntStream.range(1, matcher.groupCount() + 1)
                            .mapToObj(i -> "" + i).collect(Collectors.joining(","));
                    case "start":
                        return arg == null ? "" + matcher.start() : "" + matcher.start(Integer.parseInt(arg));
                    case "end":
                        return arg == null ? "" + matcher.end() : "" + matcher.end(Integer.parseInt(arg));
                    case "group":
                        if (arg == null) {
                            return matcher.group();
                        }
                        try {
                            var index = Integer.parseInt(arg);
                            try {
                                return matcher.group(index);
                            } catch (IllegalStateException ise) {
                                throw new BadSyntax("The generated macro '" + name + "' for 'group' caused exception with the argument '" + index + "'");
                            }
                        } catch (NumberFormatException ignored) {
                            try {
                                return matcher.group(arg);
                            } catch (IllegalStateException ise) {
                                throw new BadSyntax("The generated macro '" + name + "' for 'group' caused exception with the argument '" + arg + "'");
                            }
                        }
                    case "matches":
                        return "" + matcher.matches();
                    case "find":
                        if (arg == null) {
                            return "" + matcher.find();
                        } else {
                            var start = Integer.parseInt(arg);
                            return "" + matcher.find(start);
                        }
                    default:
                        throw new BadSyntax("The generated macro '" + name + "' argument '" + command + "' cannot be interpreted.");
                }
            } catch (NumberFormatException nfe) {
                throw new BadSyntax("The command '" + command + "' in the generated macro '" + name + "' needs a numeric argument");
            }
        }

        @Override
        public int expectedNumberOfArguments() {
            return -1;
        }
    }

    /**
     * Match a string against a regular expression.
     * <p>
     * The sintax of the macro is
     * <pre>{@code
     *    {@matcher name regex string}
     * }</pre>
     * <p>
     * where the {@code name} will be the name of the matcher created. This name can later be used as a user defined
     * macro to get the different parts of the result of the matching.
     * <p>
     * The {@code regex} is the patters used to patch.
     * <p>
     * The {@code }
     * <p>
     * This macro splits the input into three parts using {@link InputHandler#getParts(Input)}. The first will be the
     * name of the
     */
    public static class Matcher implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            skipWhiteSpaces(in);
            final var id = fetchId(in);
            skipWhiteSpaces(in);
            final var part = getParts(in, 2);
            BadSyntax.when(part.length != 2,  "matcher needs exactly 3 parts separated but the input has %d%d", part.length, 1);
            var pattern = Pattern.compile(part[0]);
            var matcher = pattern.matcher(part[1]);
            var udm = new GroupUserDefinedMacro(id, matcher);
            processor.define(udm);
            return "";
        }
    }

}
