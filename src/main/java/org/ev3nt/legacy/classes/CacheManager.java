package org.ev3nt.legacy.classes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CacheManager {
    public enum StreamType {
        INPUT,
        OUTPUT
    }

    static public Object getCachedDataAsStream(Path name, StreamType type) {
        Object stream = null;

        if (name != null) {
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException e) {
//           throw new RuntimeException(e);
            }

            try {
                Path fullName = cacheDir.resolve(name);
                if (type == StreamType.INPUT) {
                    if (fullName.toAbsolutePath().toFile().exists()) {
                        stream = Files.newInputStream(fullName.toFile().toPath());
                    }
                } else {
                    stream = Files.newOutputStream(fullName.toFile().toPath());
                }
            } catch (IOException e) {
//            throw new RuntimeException(e);
            }
        }

        return stream;
    }

    static public String getCachedDataAsString(Path name) {
        StringBuilder builder = new StringBuilder();

        InputStream stream = (InputStream)getCachedDataAsStream(name, StreamType.INPUT);
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.lineSeparator());
                }

                reader.close();
                stream.close();
            } catch (IOException e) {
//              throw new RuntimeException(e);
            }
        }

        return builder.toString();
    }

    static public void saveDataAsCache(Path name, String data) {
        OutputStream stream = (OutputStream)getCachedDataAsStream(name, StreamType.OUTPUT);
        if (stream != null && !data.isEmpty()) {
            try {
                stream.write(data.getBytes(StandardCharsets.UTF_8));

                stream.close();

                lastCachedFileName = name;
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    static public void deleteLastCachedFile() {
        try {
            if (lastCachedFileName != null) {
                Files.deleteIfExists(cacheDir.resolve(lastCachedFileName));
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    static Path cacheDir = Paths.get("cache");
    static Path lastCachedFileName = null;
}
