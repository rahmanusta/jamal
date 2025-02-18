package javax0.jamal.jar.input;

import javax0.jamal.api.ResourceReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

/**
 * JarInput can read a file which is inside an archive file.
 * <p>
 * The format of the reference is
 *
 * <pre>
 *     jar:file://path_to_the_JAR_file!path_inside_the_jar_to_the_file
 * </pre>
 */
public class JarInput implements ResourceReader {

    public static final String PREFIX = "jar:file:";
    private static final int PREFIX_LENGTH = PREFIX.length();

    private static class ResourceCoordinates {
        final String jarFile;
        final String resourceName;

        final int jarFileStart;

        ResourceCoordinates(String url) {
            if (!url.startsWith(PREFIX)) {
                throw new IllegalArgumentException(String.format("The JAR resource should start with '%s'", PREFIX));
            }
            var jarFileStart = PREFIX_LENGTH;
            url = url.substring(jarFileStart);
            int excl = url.indexOf('!');
            if (excl < 0) {
                throw new IllegalArgumentException(String.format("The format of a JAR resource is '%spath_to_the_JAR_file!path_inside_the_jar_to_the_file'", PREFIX));
            }
            var jarPart = url.substring(0, excl);
            while (jarPart.startsWith("//")) {
                jarPart = jarPart.substring(1);
                jarFileStart++;
            }
            if (jarPart.length() >= 3 && jarPart.charAt(0) == '/' && Character.isAlphabetic(jarPart.charAt(1)) && jarPart.charAt(2) == ':') {
                jarFileStart++;
                jarPart = jarPart.substring(1);
            }
            jarFile = jarPart;
            resourceName = url.substring(excl + 1);
            this.jarFileStart = jarFileStart;
        }

    }

    @Override
    public boolean canRead(final String fileName) {
        return fileName.startsWith(PREFIX);
    }


    @Override
    public int fileStart(final String fileName) {
        return new ResourceCoordinates(fileName).jarFileStart;
    }

    @Override
    public String read(final String fileName) throws IOException {
        return read(fileName, false);
    }

    @Override
    public String read(final String fileName, final boolean noCache) throws IOException {
        try {
            final var coor = new ResourceCoordinates(fileName);

            try (final var jarFile = new JarFile(coor.jarFile)) {
                final var entry = jarFile.getJarEntry(coor.resourceName);
                if (entry != null) {
                    return new String(jarFile.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        throw new IOException("The file '" + fileName + "' was not found in the JAR archive");
    }
}
