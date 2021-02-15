package javax0.jamal.engine.macro;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Delimiters;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Stackable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MacroRegister implements javax0.jamal.api.MacroRegister {

    private static final int TOP_LEVEL = 0;

    /**
     * Stores the data that describes the scopes that are stacked.
     */
    private static class Scope {
        /**
         * Stores the user defined macros that were defined on the level.
         */
        final Map<String, Identified> udMacros = new HashMap<>();

        Map<String, Identified> getUdMacros() {
            return udMacros;
        }

        /**
         * Stores the built-in macros that were defined on the level. Note that built-in macros usually are loaded on
         * the global level, especially when the service loader loads the macro instances. The built-in macro {@code
         * use} however defined the built-in macro scoped level unless the {@code global} keyword is used in it.
         */
        final Map<String, Macro> macros = new HashMap<>();

        Map<String, Macro> getMacros() {
            return macros;
        }

        /**
         * The delimiters that were saved on this level.
         */
        final List<Delimiters> savedDelimiterPairs = new ArrayList<>();
        /**
         * The last delimiter pair that was defined on this scope level. Null if there was none defined in this scope.
         */
        Delimiters delimiterPair = null;

        Delimiters getDelimiterPair() {
            return delimiterPair;
        }

        /**
         * The marker object that was used to open the scope. When the scope gets locked or closed (pop) then a marker
         * object that equals this has to be provided. This is to ensure that the last scope is closed.
         */
        final Marker checkObject;
        /**
         * When a scope is locked then the definitions will go up higher to the next scope. Even if that scope is
         * locked.
         */
        boolean locked = false;

        private Scope(Marker checkObject) {
            this.checkObject = checkObject;
        }
    }

    private final List<Scope> scopeStack = new ArrayList<>();
    private final List<Marker> poppedMarkers = new ArrayList<>();

    public MacroRegister() {
        try {
            push(null);
        } catch (BadSyntax badSyntax) {
            throw new RuntimeException("SNAFU: should not happen");
        }
    }

    private Scope currentScope() {
        return scopeStack.get(scopeStack.size() - 1);
    }

    /**
     * When defining a new macro then this offset is used from the end. It is 1 or 2 because we subtract this value from
     * the length of the stack, and the indexing starts with zero, therefore the last element is length-1, but when that
     * locked then the last writable element is length-2
     *
     * @return 1 or 2
     */
    private int writableOffset() {
        return currentScope().locked ? 2 : 1;
    }

    /**
     * Return the last scope or the scope above that in case the last scope is locked.
     * <p>
     * When th active scope is locked then the methods, like define, export and so on work with the scope that is above
     * the last one. This is the case even if that scope is also locked. It may happen that some macro evaluation opens
     * a new scope even after the current scope was locked.
     * <p>
     * If that happens the writing (user defined macros or something else) in the already locked one level higher scope
     * just works until the scope gets closed. Locking a scope essentially means that we want to write new macros into
     * one level higher and not into this scope while the locked scope is the actual scope.
     * <p>
     * This is used when user defined macros are evaluated. The content of the macro is evaluated in a new scope. It
     * means that any macro defined inside the USE of a user defined macro is local in the use of the user defined
     * macro.
     * <p>
     * These macros are available when the macro itself is evaluated, but if a macro is defined inside the user defined
     * macro (not in the use) then this macro should already be defined one level higher. That way the lowers scope is
     * locked. The macros are still there and usable, but any new macro is already defined after that. Finally when the
     * result of the user defined macro is evaluated this is already fully in the outer scope, macros defined in the USE
     * of the user defined macro are not reachable any more. A good example from the test file {@code
     * TestUDMacroEvaluationOrder}:
     *
     * <pre>{@code
     * {@define firstName=Julia}
     * {@define k(h)=h, {firstName} h{@define son=Junior Bond}}
     * {k /Bond{@define firstName=James}}
     * {k /Bond}
     * {son}
     * }</pre>
     * <p>
     * will result
     *
     * <pre>{@code
     * Bond, James Bond
     * Bond, Julia Bond
     * Junior Bond
     * }</pre>
     * <p>
     * Here {@code {@define firstName=James}} is evaluated in the scope, which was opened when the evaluation of the
     * content of the first use of macro {@code k} started. The value of {@code firstName} is local inside the macro USE
     * evaluation, but this is not demonstrated in this case as it is kind of trivial.
     * <p>
     * When the macro evaluated and the content {@code h, {firstName} h{@define son=Junior Bond}} then the in-macro-use
     * redefinition if {@code firstName} is in effect. The scope where the {@code firstName} is taken from is locked,
     * but this means it still overrides the definition of the same macro in the scope above. The evaluation of {@code
     * {@define son=Junior Bond}} defines the macro {@code son} in the higher layer and not the locked one.
     * <p>
     * The second use of the macro {@code h} does not contain any redefinition of the macro {@code firstName}. In this
     * case the value defined at the start of the example is used. When the macro is evaluated the evaluation of {@code
     * {@define son=Junior Bond}} overwrites the same macro with the same value.
     * <p>
     * Finally the macro {@code son} is used when the top level is the current one, and it is defined (actually twice),
     * but that is not a problem, you can redefine any macro any times.
     *
     * @return the writable scope
     */
    private Scope writableScope() {
        return scopeStack.get(scopeStack.size() - writableOffset());
    }

    /**
     * Get the macro or user defined macro from the stack.
     *
     * @param field is either 'macros' or 'udMacros'
     * @param id
     * @param <T>
     * @return
     */
    private <T> Optional<T> stackGet(Function<Scope, Map<String, T>> field, String id) {
        final int end = scopeStack.size() - 1;
        return IntStream.range(TOP_LEVEL, scopeStack.size())
            .sequential()
            .mapToObj(i -> scopeStack.get(end - i))
            .map(field)
            .filter(map -> map.containsKey(id))
            .map(map -> map.get(id))
            .findFirst();
    }

    @Override
    public <T extends Identified> Optional<T> getUserDefined(String id) {
        return (Optional<T>) stackGet(Scope::getUdMacros, id);
    }

    @Override
    public Optional<Macro> getMacro(String id) {
        return stackGet(Scope::getMacros, id);
    }

    @Override
    public void global(Identified macro) {
        scopeStack.get(TOP_LEVEL).udMacros.put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro) {
        scopeStack.get(TOP_LEVEL).macros.put(macro.getId(), macro);
    }

    @Override
    public void global(Macro macro, String alias) {
        scopeStack.get(TOP_LEVEL).macros.put(alias, macro);
    }

    @Override
    public void define(Identified macro) {
        writableScope().udMacros.put(macro.getId(), macro);
    }

    @Override
    public void define(Macro macro) {
        define(macro, macro.getId());
    }

    @Override
    public void define(Macro macro, String alias) {
        writableScope().macros.put(alias, macro);
    }

    @Override
    public void export(String id) throws BadSyntax {
        if (scopeStack.size() > writableOffset()) {
            var macro = writableScope().udMacros.get(id);
            if (macro == null) {
                throw new BadSyntax("Macro '" + id + "' cannot be exported");
            }
            scopeStack.get(scopeStack.size() - writableOffset() - 1).udMacros.put(id, macro);
            writableScope().udMacros.remove(id);
        } else {
            throw new BadSyntax("Macro '" + id + "' cannot be exported from the top level");
        }
    }

    private void stack(Macro macro, Consumer<Stackable> c) {
        if (macro instanceof Stackable) {
            c.accept((Stackable) macro);
        }
    }

    @Override
    public void push(Marker check) throws BadSyntax {
        if (markerIsInTheStack(check)) {
            throw new BadSyntax("Push was performed using the marker " + check + " which happens to be already in the stack.");
        }
        final var scope = new Scope(check);
        scopeStack.add(scope);
        scopeStack.forEach(scp -> scp.macros.values().forEach(macro -> stack(macro, Stackable::push)));
        scope.delimiterPair = new javax0.jamal.engine.Delimiters();
        poppedMarkers.clear();
    }

    @Override
    public Marker test() throws BadSyntax {
        return currentScope().checkObject;
    }

    @Override
    public void test(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            final var current = currentScope();
            if (!Objects.equals(check, current.checkObject)) {
                if (markerIsInTheStack(check)) {
                    tryCleanUpStack(check);
                }
                throw new BadSyntaxAt("Scope was changed from " +
                    check + " to " + current.checkObject + " and it was not closed before the end.",
                    current.checkObject.getPosition());
            }
        } else {
            if (check != null) {
                throw new BadSyntax("Scope opened with " + check + " was closed immature.");
            }
        }
    }

    @Override
    public void pop(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            final var current = currentScope();
            if (!Objects.equals(check, current.checkObject)) {
                if (markerIsInTheStack(check)) {
                    tryCleanUpStack(check);
                    popStackOneLevel();
                }
                throw new BadSyntax("Pop was performed by " +
                    check + " for a level pushed by " + current.checkObject);
            }
            popStackOneLevel();
        } else {
            throw new BadSyntax("Cannot close the top level scope.");
        }
    }

    private boolean markerIsInTheStack(Marker check) throws BadSyntax {
        for (final var scopeWalker : scopeStack) {
            if (Objects.equals(check, scopeWalker.checkObject)) {
                return true;
            }
        }
        return false;
    }

    private void tryCleanUpStack(Marker check) throws BadSyntax {
        while (scopeStack.size() > 0 && !Objects.equals(check, currentScope().checkObject)) {
            popStackOneLevel();
        }
    }

    private void popStackOneLevel() throws BadSyntax {
        if (scopeStack.size() > 0) {
            final var removedScope = scopeStack.remove(scopeStack.size() - 1);
            for (final var macro : removedScope.getMacros().values()) {
                if (macro instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) macro).close();
                    } catch (Exception e) {
                        throw new BadSyntax("Closing AutoCloseable macro '" + macro.getId() + "' caused exception.", e);
                    }
                }
            }
            for (final var macro : removedScope.getUdMacros().values()) {
                if (macro instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) macro).close();
                    } catch (Exception e) {
                        throw new BadSyntax("Closing AutoCloseable macro '" + macro.getId() + "' caused exception.", e);
                    }
                }
            }
            poppedMarkers.add(removedScope.checkObject);
            scopeStack.forEach(scope -> scope.macros.values().forEach(macro -> stack(macro, Stackable::pop)));
        }
    }

    @Override
    public void lock(Marker check) throws BadSyntax {
        if (scopeStack.size() > 1) {
            if (!Objects.equals(check, currentScope().checkObject)) {
                throw new BadSyntax("Lock was performed by " +
                    check +
                    " for a level pushed by " +
                    currentScope().checkObject);
            }
            currentScope().locked = true;
        } else {
            throw new BadSyntax("Cannot lock the top level scope.");
        }
    }

    /**
     * Walk up the stack and return the first open or close string.
     *
     * @param openOrClose either Delimiters::open or Delimiters::close
     * @return the defined string from the stack or null
     */
    private String stackGet(Function<Delimiters, String> openOrClose) {
        final int end = scopeStack.size() - 1;
        return IntStream.range(TOP_LEVEL, scopeStack.size())
            .sequential()
            .mapToObj(i -> scopeStack.get(end - i))
            .map(scope -> scope.delimiterPair)
            .map(openOrClose)
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @Override
    public String open() {
        return stackGet(Delimiters::open);
    }

    @Override
    public String close() {
        return stackGet(Delimiters::close);
    }

    /**
     * Sets the opening and closing delimiter strings. If {@code openDelimiter} or {@code closeDelimiter} is {@code
     * null} then it resets the delimiter to the last value that was saved in the stack. If {@code openDelimiter} is
     * {@code null} then {@code closeDelimiter} is ignored, and also the other way around, but it is good practice to
     * pass {@code null} in both arguments when resetting to the last saved delimiter pair.
     *
     * @param openDelimiter  the macro opening string to be set. If this parameter is {@code null} then the method
     *                       treats this information as a restore process. This class saves the old values of the
     *                       separators in a stack and when {@code openDelimiter} is {@code null} it restores the
     *                       delimiters from the top of the stack.
     * @param closeDelimiter the macro closing string to be set. Ignored when {@code openDelimiter} is {@code null}. If
     *                       this parameter is {@code null} the functionality will be the same as in case {@code
     *                       openDelimiter} is {@code null}.
     * @throws BadSyntaxAt when the call tries to restore an older version but there is no saved older version.
     */
    @Override
    public void separators(String openDelimiter, String closeDelimiter) throws BadSyntax {
        var delimiterPair = currentScope().delimiterPair;
        var savedList = currentScope().savedDelimiterPairs;
        if (openDelimiter == null || closeDelimiter == null) {
            if (savedList.size() == TOP_LEVEL) {
                throw new BadSyntax("There was no saved macro start and end string to restore.");
            }
            var savedDelim = savedList.remove(savedList.size() - 1);
            delimiterPair.separators(savedDelim.open(), savedDelim.close());
        } else {
            var savedDelimiterPair = new javax0.jamal.engine.Delimiters();
            savedDelimiterPair.separators(delimiterPair.open(), delimiterPair.close());
            savedList.add(savedDelimiterPair);
            delimiterPair.separators(openDelimiter, closeDelimiter);
        }
    }
}
