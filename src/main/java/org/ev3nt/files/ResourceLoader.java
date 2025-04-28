package org.ev3nt.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceLoader {
    static public InputStream getResourceAsStream(String name) {
        return ResourceLoader.class.getResourceAsStream("/" + name.replace("\\", "/"));
    }

    static public URL getResource(String name) {
        return ResourceLoader.class.getResource("/" + name.replace("\\", "/"));
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

    static public void extract(String name, String destination) {
        Path destinationPath = Paths.get(destination).resolve(name);

        try {
            Files.createDirectories(destinationPath.getParent());

            Files.copy(ResourceLoader.getResourceAsStream(name), destinationPath);
        } catch (IOException ignored) {}
    }

    static public void extract(String name) {
        extract(name, "");
    }
}
