package javax0.jamal.documentation;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
// snippet sample
public class Sample implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) {
        return in.toString()
            .replaceAll("^\\n+","")
            .replaceAll("\\n+$","");
    }
}
// end snippet