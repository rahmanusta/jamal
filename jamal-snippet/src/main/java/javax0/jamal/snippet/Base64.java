package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

public class Base64 {

    public static class Encode implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var quote = Params.holder("quote").asBoolean();
            final var compress = Params.holder("compress").asBoolean();
            final var url = Params.holder("url").asBoolean();
            Scan.using(processor).from(this).between("()").keys(quote, compress, url).parse(in);

            final var plainText = getBytes(in, quote.is(), compress.is());
            final var encoder = url.is() ? java.util.Base64.getUrlEncoder() : java.util.Base64.getEncoder();
            return encoder.encodeToString(plainText);
        }

        private static final String[] names = {"base64", "base64:encode"};

        @Override
        public String[] getIds() {
            return names;
        }

    }

    public static class Decode implements Macro {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var quote = Params.holder("quote").asBoolean();
            final var url = Params.holder("url").asBoolean();
            Scan.using(processor).from(this).between("()").keys(quote,url).parse(in);
            try {
                final var plainText = getBytes(in, quote.is(), false);
                final var decoder = url.is() ? java.util.Base64.getUrlDecoder() : java.util.Base64.getDecoder();
                return new String(decoder.decode(plainText), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new BadSyntax("Illegal base64 string", e);
            }
        }

        private static final String[] names = {"base64:decode"};

        @Override
        public String[] getIds() {
            return names;
        }
    }

    private static byte[] getBytes(final Input in, final boolean quote, final boolean compress) throws BadSyntax {
        final String plainText;
        final var trimText = in.toString().trim();
        if (quote && trimText.length() > 2) {
            final var start = trimText.charAt(0);
            final var end = trimText.charAt(trimText.length() - 1);
            BadSyntax.when(start != end, "The text to be encoded must be quoted with the same character.");
            plainText = trimText.substring(1, trimText.length() - 1);
        } else {
            plainText = trimText;
        }
        final byte[] byteArray;
        if (compress) {
            byteArray = compress(plainText.getBytes(StandardCharsets.UTF_8));
        } else {
            byteArray = plainText.getBytes(StandardCharsets.UTF_8);
        }
        return byteArray;
    }

    private static byte[] compress(byte[] source) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(source);
        deflater.finish();

        byte[] buffer = new byte[2048];
        int compressedLength = deflater.deflate(buffer);
        byte[] result = new byte[compressedLength];
        System.arraycopy(buffer, 0, result, 0, compressedLength);
        return result;
    }
}

