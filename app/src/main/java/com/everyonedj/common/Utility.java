package com.everyonedj.common;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Siddharth on 7/16/2015.
 */
public class Utility {

    public static byte[] toByteArray(File file) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024 * 4];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(b)) > 0) {
                bos.write(b, 0, bytesRead);
            }

            byteArray = bos.toByteArray();
            bos.reset();
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    public static byte[] bitmapTooByte(Bitmap bitmap) {
        byte[] byteArray = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();
        return byteArray;
    }
}
