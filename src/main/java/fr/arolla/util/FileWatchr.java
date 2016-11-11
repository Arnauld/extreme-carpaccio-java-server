package fr.arolla.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class FileWatchr {

    private static final Logger log = LoggerFactory.getLogger(FileWatchr.class);
    private final File file;
    //
    private String md5;

    public FileWatchr(File file) {
        this.file = file;
        this.md5 = md5(file);
    }

    private static String md5(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Fail to calculate md5 of '{}'", file.getAbsolutePath(), e);
            return UUID.randomUUID().toString();
        }
    }

    public boolean hasChanged() {
        String currentMd5 = md5(file);
        String previousMd5 = this.md5;
        this.md5 = currentMd5;

        return previousMd5 == null || !currentMd5.equals(previousMd5);
    }
}
