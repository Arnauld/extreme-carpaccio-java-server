package fr.arolla.util;

import org.assertj.core.util.Files;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class WorkDirectory extends ExternalResource {

    private static final AtomicInteger idGen = new AtomicInteger();
    private static final Logger LOG = LoggerFactory.getLogger(WorkDirectory.class);

    private File dir;
    private boolean deleteAfterwards = true;

    public WorkDirectory deleteAfterwards(boolean deleteAfterwards) {
        this.deleteAfterwards = deleteAfterwards;
        return this;
    }

    @Override
    protected void after() {
        if (deleteAfterwards && dir != null)
            Files.delete(dir);
    }

    public File dir() {
        if (dir == null) {
            File baseDir = TestProperties.get().getWorkingDirectory();
            dir = new File(baseDir, "tmp/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-" + idGen.incrementAndGet());
            if (!dir.mkdirs())
                throw new RuntimeException("Fail to create temporary directory at '" + dir.getAbsolutePath() + "'");

            LOG.info("Temporary directory created at '{}'", dir.getAbsolutePath());
        }
        return dir;
    }

}
