package javax0.jamal.api;

import java.io.IOException;
import java.util.Properties;

/**
 * The processor object that can be used to process an input to generate the Jamal output.
 */
public interface Processor extends AutoCloseable {
    /**
     * Process the input and result the string after processing all built-in and user defined macros.
     *
     * @param in the input the processor has to work on.
     * @return the string after the processing
     * @throws BadSyntax when the input contains something that cannot be processed.
     */
    String process(final Input in) throws BadSyntax;

    /**
     * Get the macro register of this processor. See {@link MacroRegister}
     *
     * @return the register
     */
    MacroRegister getRegister();

    /**
     * Get the JShell engine that the processor has.
     * <p>
     * Note that the JShell engine may not be initialized. It initializes automatically the first time when the engine's
     * {@link JShellEngine#evaluate(String)} is invoked.
     *
     * @return the JShell engine
     */
    JShellEngine getJShellEngine();

    /**
     * Create a new user defined macro. The implementation of this method calls the constructor of the matching
     * implementation of the user defined macro. The existence of this method loosens the coupling of the user of the
     * API and the actual implementation. The code using the implementation may need only to initiate the engine that
     * implements this interface, but does not need to access directly the UserDefinedMacro or other interface
     * implementations.
     * <p>
     * NOTE: The invocation of this method creates a new object but it DOES NOT register the created user defined macro
     * in the macro registry. The sole purpose of this method is to decouple the API usage and the implementation.
     *
     * @param id     the identifier (name) of the macro
     * @param input  the content of the macro
     * @param params the parameter names of the macro
     * @return the new user defined macro
     * @throws BadSyntax in case the parameter names contain each other
     */
    UserDefinedMacro newUserDefinedMacro(String id, String input, String[] params) throws BadSyntax;

    /**
     * Create a new user defined script. Read the important comments for {@link #newUserDefinedMacro(String, String,
     * String[])}
     *
     * @param id         see {@link #newUserDefinedMacro(String, String, String[])}
     * @param scriptType see {@link #newUserDefinedMacro(String, String, String[])}
     * @param input      see {@link #newUserDefinedMacro(String, String, String[])}
     * @param params     see {@link #newUserDefinedMacro(String, String, String[])}
     * @return see {@link #newUserDefinedMacro(String, String, String[])}
     * @throws BadSyntax see {@link #newUserDefinedMacro(String, String, String[])}
     */
    ScriptMacro newScriptMacro(String id, String scriptType, String input, String[] params) throws BadSyntax;

    /**
     * Register an AutoClosable resource that has to be closed when the execution is finished.
     * <p>
     * Some user defined (Java implemented) or built-in macro may create resources that perform some actions
     * asynchronous. The typical example is when a macro that creates some external resource starts a separate thread to
     * execute the task. This task has to be joined at the end of the processing. The general model is that there is a
     * resource that has to be closed. If it is a thread, then an object has to be created that joins the thread upon
     * closing. If it is a task in a thread pool then it has to wait for the task to be finished. It is up to the
     * implementation of the macro.
     * <p>
     * Calling this method the macro may register the object as something autocloseable. The processor itself is also
     * autocloseable, so when the processor is closed then the resources are closed one by one.
     * <p>
     * Note that this method, or any other method of the processor MUST NOT be invoked from other than the main thread
     * of the Jamal processing. Even if a macro spawns a new thread the new thread must not do anything with the
     * processor.
     *
     * @param resource the autocloseable object to be closed at the end of the processing.
     */
    void deferredClose(AutoCloseable resource);

    /**
     * @param id the identifier of the user defined macro
     * @return {@code true} if the user defined macro is defined at the current contex and {@code false} otherwise.
     */
    default boolean isDefined(String id) {
        return getRegister().getUserDefined(id).isPresent();
    }

    /**
     * Define a new user defined macro on the global level. Technically anything can be defined that implements the
     * {@link Identified} interface. Usually {@link UserDefinedMacro} is registered using this method.
     *
     * @param macro the macro to be registered
     */
    default void defineGlobal(Identified macro) {
        getRegister().global(macro);
    }

    /**
     * Define a new user defined macro on the current scope. Technically anything can be defined that implements the
     * {@link Identified} interface. Usually {@link UserDefinedMacro} is registered using this method.
     *
     * @param macro the macro to be registered
     */
    default void define(Identified macro) {
        getRegister().define(macro);
    }

    /**
     * This is a convenience method with the default implementation calling to the {@link
     * MacroRegister#separators(String, String)} method.
     *
     * @param openMacro  see {@link MacroRegister#separators(String, String)}
     * @param closeMacro see {@link MacroRegister#separators(String, String)}
     * @throws BadSyntax see {@link MacroRegister#separators(String, String)}
     */
    default void separators(String openMacro, String closeMacro) throws BadSyntax {
        getRegister().separators(openMacro, closeMacro);
    }

    /**
     * Convert a Jamal version string to a {@link Runtime.Version}.
     * <p>
     * The method removes the trailing zero versions, because those are not allowed by the parsing of {@link
     * Runtime.Version}.
     *
     * @param version the version string, probably from the macro argument {@code require}
     * @return the parsed version, which can be compared to the current version
     */
    static Runtime.Version jamalVersion(final String version) {
        return Runtime.Version.parse(version.replaceAll("(?:\\.0+){0,2}\\.0+(-|$)", "$1"));
    }

    /**
     * @return the current Jamal version in the form of a {@link Runtime.Version}
     */
    static Runtime.Version jamalVersion() {
        final var version = new Properties();
        try {
            version.load(Processor.class.getClassLoader().getResourceAsStream("version.properties"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Version information of Jamal cannot be identified.");
        }
        return jamalVersion(version.getProperty("version"));
    }
}
