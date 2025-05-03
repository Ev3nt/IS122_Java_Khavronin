package org.ev3nt.files;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class CacheManager {
    public enum StreamType {
        INPUT,
        OUTPUT
    }

    static public Object getCachedDataAsStream(String name, StreamType type) {
        Object stream = null;

        if (name != null) {
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException ignored) {}

            try {
                Path fullName = cacheDir.resolve(name);
                if (type == StreamType.INPUT) {
                    if (fullName.toAbsolutePath().toFile().exists()) {
                        stream = Files.newInputStream(fullName.toFile().toPath());
                    }
                } else {
                    stream = Files.newOutputStream(fullName.toFile().toPath());
                }
            } catch (IOException ignored) {}
        }

        return stream;
    }

    static public String getCachedDataAsString(String name) {
        StringBuilder builder = new StringBuilder();

        InputStream stream = (InputStream)getCachedDataAsStream(name, StreamType.INPUT);
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.lineSeparator());
                }

                reader.close();
                stream.close();
            } catch (IOException ignored) {}
        }

        return builder.toString();
    }

    static public void saveDataAsCache(String name, String data) {
        OutputStream stream = (OutputStream)getCachedDataAsStream(name, StreamType.OUTPUT);
        if (stream != null && !data.isEmpty()) {
            try {
                stream.write(data.getBytes(StandardCharsets.UTF_8));

                stream.close();
            } catch (IOException ignored) {}
        }
    }

    static Path cacheDir = Paths.get("cache");
}
