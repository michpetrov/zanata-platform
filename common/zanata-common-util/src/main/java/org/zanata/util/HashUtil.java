package org.zanata.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.base.Joiner;

public class HashUtil {

    private static final Joiner pipeJoiner = Joiner.on("|").useForNull("");

    // This class is used in import.sql to provide the MD5 function in H2 tests
    public static String generateHash(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    public static String sourceHash(@Nonnull List<String> sourceContents) {
        if (sourceContents.isEmpty()) {
            return null;
        } else {
            String sourceString = pipeJoiner.join(sourceContents);
            return generateHash(sourceString);
        }
    }

    public static String sourceHash(@Nonnull String sourceContent) {
        if (sourceContent.isEmpty()) {
            return null;
        } else {
            return generateHash(sourceContent);
        }
    }

    public static String md5Hex(String message) {
        return DigestUtils.md5Hex(message);
    }

    /**
     * Generates the MD5 checksum from the contents of a file.
     *
     * @param f
     *            The file to calculate the checksum for.
     * @return The MD5 checksum for f.
     * @throws java.io.FileNotFoundException
     *             If the given file does not exist.
     * @throws java.io.IOException
     *             On error reading from the file.
     */
    public static String getMD5Checksum(File f)
            throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(f);
        try {
            return DigestUtils.md5Hex(fis);
        } finally {
            fis.close();
        }
    }
}
