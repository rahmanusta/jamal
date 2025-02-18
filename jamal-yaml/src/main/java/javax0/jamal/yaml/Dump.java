package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;


public class Dump implements Macro, InnerScopeDependent {
    final Yaml yaml = YamlFactory.newYaml();

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        Params.using(processor).from(this).keys(clone, copy).between("()").parse(input);

        InputHandler.skipWhiteSpaces(input);
        final var id = InputHandler.fetchId(input);
        InputHandler.skipWhiteSpaces(input);
        final var from = InputHandler.fetchId(input);
        BadSyntax.when(!"to".equals(from), "Yaml:dump needs a 'to' after the identifier");
        InputHandler.skipWhiteSpaces(input);
        var reference = input.getReference();
        var fileName = FileTools.absolute(reference, input.toString().trim());

        final var yamlObject = Resolve.getYaml(processor, id);
        Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        try (final var writer = new FileWriter(fileName)) {
            yaml.dump(yamlObject.getObject(), writer);
        } catch (IOException ioe) {
            throw new BadSyntax("Not possible to dump yaml '" + id + "' into the file '" + fileName + "'", ioe);
        }
        return "";
    }

    @Override
    public String getId() {
        return "yaml:dump";
    }
}
