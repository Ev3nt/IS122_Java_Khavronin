package org.ev3nt.classes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCustomCopy implements AutoCloseable {
    public ZipCustomCopy(String destination, String source) throws FileNotFoundException {
        contentMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        inputStream = new ZipInputStream(new FileInputStream(source));
        outputStream = new ZipOutputStream(new FileOutputStream(destination));
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

            outputStream.putNextEntry(zipEntry);

            inputStream.transferTo(outputStream);
        }
        
        inputStream.close();
        outputStream.close();
    }

    public void add(String name, byte[] b) {
        contentMap.put(name.replace("\\", "/"), b);
    }

    public void add(String name, String data) {
        add(name, data.getBytes());
    }

    private final ZipInputStream inputStream;
    private final ZipOutputStream outputStream;

    private final Map<String, byte[]> contentMap;
}
