package org.ev3nt.classes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLoader {
    static public InputStream getResourceAsStream(Path name) {
        return ResourceLoader.class.getResourceAsStream("/" + name.toString().replace("\\", "/"));
    }

//    static public String getResourceAsString(Path name) {
//        InputStream stream = ResourceLoader.getResourceAsStream(name);
//        StringBuilder builder = new StringBuilder();
//
//        if (stream != null) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//            String line;
//
//            try {
//                while ((line = reader.readLine()) != null) {
//                    builder.append(line);
//                    builder.append(System.lineSeparator());
//                }
//            } catch (IOException e) {
////            throw new RuntimeException(e);
//            }
//        }
//
//        return builder.toString();
//    }

    static public void extract(Path name, Path destination) {
        Path destinationPath = destination.resolve(name);

        try {
            Files.createDirectories(destinationPath.getParent());

            Files.copy(ResourceLoader.getResourceAsStream(name), destinationPath);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

    }

    static public void extract(Path name) {
        extract(name, Paths.get(""));
    }
}
