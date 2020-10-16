package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

import java.io.IOException;
import java.nio.file.Paths;

import static javax0.jamal.tools.Input.makeInput;

/**
 * Utility class containing static methods handling files.
 */
public class FileTools {

    private static final String RESOURCE_PREFIX = "res:";
    private static final int RESOURCE_PREFIX_LENGTH = RESOURCE_PREFIX.length();
    private static final String HTTPS_PREFIX = "https:";

    /**
     * Create a new input from a file.
     * <p>
     * Reads the file and returns an input object that has the content of the file.
     * <p>
     * The file can be
     * <ul>
     *     <li>a plain file,
     *     </li>
     *     <li>a Java resource (file name starts with {@code res:}, or
     *     </li>
     *     <li>a {@code https} downloadable content (file name starts with {@code https}.
     *     </li>
     * </ul>
     * <p>
     * If the content comes from a https URL then the local cache is checked before.
     * <p>
     * There is no cache eviction. All files donwloaded once are in the cache and the remote is never checked again.
     * If the URL contains the string literal {@code SNAPSHOT} (all capital letters) it is not cached.
     * <p>
     * There is no way to download a resource using the {@code http} protocol.
     *
     * @param fileName the name of the file. This is used to open and read the file as well as reference file name in
     *                 the input. When the file name starts with the characters {@code res:} then the rest of the string
     *                 is treated as the name of a Java resource. That way Jamal can load a Java resource from some JAR
     *                 that is on the classpath. If the file name starts with {@code https:} then the string is treated
     *                 as an URL. In that case the UTL is fetched and if there is a cache directory configured it will
     *                 be loaded from the cache.
     * @return the input containing the contend of the file.
     * @throws BadSyntaxAt if the file cannot be read.
     */
    public static Input getInput(String fileName) throws BadSyntax {
        try {
            final var pos = new Position(fileName);
            if (fileName.startsWith(RESOURCE_PREFIX)) {
                return makeInput(ResourceInput.getInput(fileName.substring(RESOURCE_PREFIX_LENGTH)), pos);
            }
            if (fileName.startsWith(HTTPS_PREFIX)) {
                return makeInput(CachedHttpInput.getInput(fileName), pos);
            } else {
                return makeInput(FileInput.getInput(fileName),pos);
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot get the content of the file '" + fileName + "'",e);
        }
    }

    /**
     * Convert the file name to an absolute file name if it is relative to the directory containing the reference file.
     * Note that {@code reference} is the name of a file and not a directory.
     * <p>
     * If the name of the file starts with one of the characters:
     * <ul>
     * <li>{@code /}</li>
     * <li>{@code \}</li>
     * <li>{@code ~}</li>
     * </ul>
     * <p>
     * or starts with an alpha character and a {@code :} (DOS drive letter, like {@code C:} then the file name is
     * absolute and it is returned as it is.
     * <p>
     * Otherwise the string in the parameter {@code reference} is used as it was a file name (the file does not need to
     * exist) and {@code file} is treated as a relative file name and the absolute path is calculated.
     *
     * @param reference the name of the reference file
     * @param fileName  the name of the file, absolute or relative
     * @return the absolute file name of the file
     */
    public static String absolute(final String reference, String fileName) {
        if (fileName.startsWith(RESOURCE_PREFIX) ||
            fileName.startsWith(HTTPS_PREFIX) ||
            fileName.startsWith("/") ||
            fileName.startsWith("\\") ||
            fileName.startsWith("~") ||
            (fileName.length() > 1 &&
                Character.isAlphabetic(fileName.charAt(0))
                && fileName.charAt(1) == ':')) {
            return fileName;
        }
        final var unixedReference = reference.replaceAll("\\\\", "/");
        final var referencePath = unixedReference.contains("/") ?
            unixedReference.substring(0, unixedReference.lastIndexOf("/") + 1)
            : "";
        return Paths.get(referencePath)
            .resolve(Paths.get(fileName))
            .normalize()
            .toString()
            .replaceAll("\\\\", "/");
    }
}
