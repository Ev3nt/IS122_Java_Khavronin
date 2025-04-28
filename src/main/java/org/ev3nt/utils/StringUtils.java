package org.ev3nt.utils;

import java.nio.file.Paths;
import java.util.List;

public class StringUtils {
    public static String getFileNameByList(String path, List<String> fileNames, String fileExtension) {
        String baseName = Paths.get(path).resolve(String.join(", ", fileNames)).toString();

        if (baseName.length() > MAX_FILENAME_LENGTH) {
            baseName = baseName.substring(0, MAX_FILENAME_LENGTH - fileExtension.length());

            int lastSeparator = Math.max(
                    baseName.lastIndexOf(','),
                    baseName.lastIndexOf(' ')
            );

            if (lastSeparator > 0) {
                baseName = baseName.substring(0, lastSeparator);
            }

            baseName = baseName.trim() + " и т.д.";
        }

        return baseName + fileExtension;
    }

    public static void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null && !value.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(value.trim());
        }
    }

    static final int MAX_FILENAME_LENGTH = 200;
}
