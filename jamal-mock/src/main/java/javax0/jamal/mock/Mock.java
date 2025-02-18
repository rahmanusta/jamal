package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Optional;
import java.util.regex.Pattern;

public class Mock implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        // snippet mock_options
        Params.Param<String> id = Params.<String>holder(null, "macro", "id").asString(); //| the identifier of the macro.
        //|This option is mandatory and has to define the identifier of the macro to be mocked.
        Params.Param<String> when = Params.<String>holder(null, "when").orElseNull(); //| regular expression when to apply the mock.
        //|This option is not mandatory.
        //|In case it is specified, the mock response will only be used when the input of the macro matches the regular expression specified.
        //|If the option is missing the mock response will always be matched and used when it gets activated regardless of the input of the macro.
        Params.Param<Integer> repeat = Params.<Integer>holder(null, "repeat", "times").orElseInt(1); //| how many times the mock can be used.
        //|Can specify how many times the mock can be used.
        //|It is an error to use a negative number.
        //|You can use zero to switch off the mock response in your text temporarily without deleting it.
        Params.Param<Boolean> infinite = Params.<Boolean>holder(null, "inf", "infinite", "forever").asBoolean(); //| if the mock be used infinite number of times.
        //|Can be used to specify that the mock response can be used unlimited number of times.
        // end snippet
        Params.using(processor).from(this).between("()").keys(id, when, repeat, infinite).parse(in);

        BadSyntax.when(repeat.isPresent() && infinite.isPresent(), "You cannot use options 'repeat' and 'infinite' at the same time.");
        BadSyntax.when(repeat.isPresent() && repeat.get() < 0, "The option 'repeat' should be non-negative.");
        if (repeat.isPresent() && repeat.get() == 0) {
            processor.logger().log(System.Logger.Level.WARNING, in.getPosition(), "Repeat is zero.");
        }
        final Pattern inputCheck = when.isPresent() ? Pattern.compile(when.get()) : null;

        final MockImplementation mock = getMockImplementation(id.get(), processor.getRegister());
        mock.response(in.toString(), when.isPresent(), inputCheck, infinite.is(), repeat.get());
        return "";
    }

    /**
     * Get an already existing mock implementation to add the new response to, or create a new one if no prior exists.
     * If the mock implementation is newly created it will also be registered in the macro register, so that Jamal
     * will find it by the id when it is used. If the macro shadowed existed and is on the same scope as the mock, then
     * it will be overwritten by the mock. When the mock exhaust Jamal still invokes the mock implementation and that
     * code calls the original macro, which is not in the register anymore.
     *
     * @param id       the identifier of the macro to be mocked
     * @param register the macro register
     * @return the already existing mock or a newly created one
     * @throws BadSyntax if the existing mock cannot be get due to some error
     */
    private MockImplementation getMockImplementation(final String id, final MacroRegister register) throws BadSyntax {
        final var existingMock = getMockIfExists(id, register);
        final MockImplementation mock;
        if (existingMock.isPresent()) {
            mock = existingMock.get();
        } else {
            mock = new MockImplementation(id, register.getMacro(id).orElse(null));
            register.define(mock);
        }
        return mock;
    }

    /**
     * Get an existing mock implementation or return empty if one with the given id does not exist.
     *
     * @param id       the identifier of the macro to be mocked and thus the id of the mock.
     * @param register the macro register where we look for the already existing mock.
     * @return the mock implementation macro or empty
     * @throws BadSyntax if the existing mock cannot be get due to some error
     */
    private Optional<MockImplementation> getMockIfExists(final String id, final MacroRegister register) throws BadSyntax {
        return register.getMacroLocal(id)
                .filter(m -> m instanceof MockImplementation).map(m -> (MockImplementation) m);
    }
}
