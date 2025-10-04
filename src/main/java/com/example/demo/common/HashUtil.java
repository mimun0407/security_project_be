package com.example.demo.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

public class HashUtil {
    public static String sha256Hex(MultipartFile file) throws Exception {
        return DigestUtils.sha256Hex(file.getInputStream());
    }

    public static String sha256Hex(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(fis);
        }
    }
}