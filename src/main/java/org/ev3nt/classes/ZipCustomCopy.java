package org.ev3nt.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCustomCopy implements AutoCloseable {
    public ZipCustomCopy(String destination, String source) throws IOException {
        contentMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        inputStream = new ZipInputStream(Files.newInputStream(Paths.get(source)));
        outputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(destination)));
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<String, byte[]> entry : contentMap.entrySet()) {
            ZipEntry zipEntry = new ZipEntry(entry.getKey());
            outputStream.putNextEntry(zipEntry);
            outputStream.write(entry.getValue());
        }

        ZipEntry zipEntry;
        while ((zipEntry = inputStream.getNextEntry()) != null) {
            if (contentMap.containsKey(zipEntry.getName())) {
                continue;
            }

            zipEntry = new ZipEntry(zipEntry.getName());
            outputStream.putNextEntry(zipEntry);

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }

            outputStream.closeEntry();
        }
        
        inputStream.close();
        outputStream.close();
    }

    public void add(String name, byte[] b) {
        Path path = Paths.get(name);
        contentMap.put(path.subpath(0, path.getNameCount()).toString().replace("\\", "/"), b);
    }

    public void add(String name, String data) {
        add(name, data.getBytes());
    }

    private final ZipInputStream inputStream;
    private final ZipOutputStream outputStream;

    private final Map<String, byte[]> contentMap;
}
