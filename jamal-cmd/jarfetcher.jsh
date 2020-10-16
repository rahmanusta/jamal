import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

    String JAMAL_CONNECT_TIMEOUT = "JAMAL_CONNECT_TIMEOUT";
    String JAMAL_READ_TIMEOUT = "JAMAL_READ_TIMEOUT";
    String JAMAL_HTTPS_CACHE = "JAMAL_HTTPS_CACHE";
    String DEFAULT_CACHE_ROOT = "~/.jamal/cache/";
    int CONNECT_TIMEOUT;
    int READ_TIMEOUT;
    File CACHE_ROOT_DIRECTORY;

    String envCacheRoot = System.getenv(JAMAL_HTTPS_CACHE);
    String userHome = System.getProperty("user.home");

    String cacheRoot;
    if (envCacheRoot != null) {
        cacheRoot = envCacheRoot;
    } else {
        cacheRoot = DEFAULT_CACHE_ROOT;
    }
    if (cacheRoot.charAt(0) == '~') {
        cacheRoot = userHome + cacheRoot.substring(1);
    }
    CACHE_ROOT_DIRECTORY = new File(new File(cacheRoot).getAbsoluteFile() + "/.jar/");
    if (CACHE_ROOT_DIRECTORY.exists() && !CACHE_ROOT_DIRECTORY.isDirectory()) {
        throw new RuntimeException(CACHE_ROOT_DIRECTORY.getAbsolutePath() + " exists but it is not a directory.");
    }

    CACHE_ROOT_DIRECTORY.mkdirs();
    if (!CACHE_ROOT_DIRECTORY.exists()) {
        throw new RuntimeException("I cannot create " + CACHE_ROOT_DIRECTORY.getAbsolutePath() + "for some reason");
    }

    String connTimeout = System.getenv(JAMAL_CONNECT_TIMEOUT);
    if (connTimeout != null) {
        CONNECT_TIMEOUT = Integer.parseInt(connTimeout);
    } else {
        CONNECT_TIMEOUT = 5000;
    }
    String readTimeout = System.getenv(JAMAL_READ_TIMEOUT);
    if (readTimeout != null) {
        READ_TIMEOUT = Integer.parseInt(readTimeout);
    } else {
        READ_TIMEOUT = 5000;
    }


    public void downloadFile(String urlString) throws IOException {
        final URL url = new URL(urlString);
        File jar = new File(CACHE_ROOT_DIRECTORY.getAbsolutePath() + url.getFile());
        if (jar.exists()) {
            return;
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);
        con.setInstanceFollowRedirects(true);
        final int status = con.getResponseCode();
        if (status != 200) {
            throw new IOException("GET url '" + url.toString() + "' returned " + status);
        }
        InputStream is = con.getInputStream();
        try (OutputStream outStream = new FileOutputStream(jar)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }