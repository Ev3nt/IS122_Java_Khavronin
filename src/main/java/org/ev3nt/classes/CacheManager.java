package org.ev3nt.classes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheManager {
    public enum StreamType {
        INPUT,
        OUTPUT
    }

    static public Object getCachedDataAsStream(Path name, StreamType type) {
        Object stream = null;

        if (name != null && name.toFile().exists()) {
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException e) {
//           throw new RuntimeException(e);
            }

            try {
                Path fullName = cacheDir.resolve(name);
                if (type == StreamType.INPUT) {
                    stream = new FileInputStream(fullName.toFile());
                } else {
                    stream = new FileOutputStream(fullName.toFile());
                }
            } catch (IOException e) {
//            throw new RuntimeException(e);
            }
        }

        return stream;
    }

    static public String getCachedDataAsString(Path name) {
        StringBuilder builder = new StringBuilder();

        FileInputStream stream = (FileInputStream)getCachedDataAsStream(name, StreamType.INPUT);
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;

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
        FileOutputStream stream = (FileOutputStream)getCachedDataAsStream(name, StreamType.OUTPUT);
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
            if (lastCachedFileName != null && lastCachedFileName.toFile().exists()) {
                Files.deleteIfExists(cacheDir.resolve(lastCachedFileName));
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    static Path cacheDir = Path.of("cache");
    static Path lastCachedFileName = null;
}
