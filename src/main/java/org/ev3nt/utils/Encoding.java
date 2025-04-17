package org.ev3nt.utils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

public class Encoding {
    public static void setCharset(String encoding) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("file.encoding", encoding);

        Field charsetField = Charset.class.getDeclaredField("defaultCharset");
        charsetField.setAccessible(true);
        charsetField.set(null, null);
    }
}
