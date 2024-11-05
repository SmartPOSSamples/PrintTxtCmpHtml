package com.cloudpos.demo.print.util;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HtmlFileUtils {
    public static String readHtmlFile(AssetManager assetManager, String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = assetManager.open(filePath);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return stringBuilder.toString();
    }

    public static InputStream readPDFFile(AssetManager assetManager, String filePath) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(filePath);
            return inputStream;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
