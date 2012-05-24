package fi.vm.sade.koodisto.selenium;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaType;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.*;

/**
 * TestWather that will record test video if 'videoMode' systemproperty or 'VIDEO_MODE' envvar is !false.
 *
* @author Antti
*/
public class TestCaseVideoRecorder extends TestWatcher {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ScreenRecorder screenRecorder;
    /**
     * Flag that enables or disables tests video recording.
     */
    protected boolean takeVideo = false;
    private String testName;

    public TestCaseVideoRecorder() {
        takeVideo = TestUtils.getEnvOrSystemPropertyAsBoolean(takeVideo, "VIDEO_MODE", "videoMode");
    }

    @Override
    protected void starting(Description description) {
        testName = TestUtils.getTestName(description);
        startVideo();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        stopVideo();
    }

    @Override
    protected void succeeded(Description description) {
        stopVideo();
    }

    public void startVideo() {

        //maybeFixMacFocus(); already done in SeleniumTestCaseSupport.setUp

        if (takeVideo) {

            try {

                GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                log.info("GraphicsDevices: " + devices.length);
                for (GraphicsDevice device : devices) {
                    log.info("    GraphicsDevice - id: " + device.getIDstring() + ", type: " + device.getType() + ", device: " + device);
                }

                GraphicsConfiguration gc = GraphicsEnvironment//
                        .getLocalGraphicsEnvironment()//
                        .getDefaultScreenDevice()//
                        .getDefaultConfiguration();

                // Create a instance of ScreenRecorder with the required configurations
                screenRecorder = new ScreenRecorder(gc,
                        new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DepthKey, (int) 24, FrameRateKey, Rational.valueOf(15),
                                QualityKey, 1.0f,
                                KeyFrameIntervalKey, (int) (15 * 60)),
                        new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                                FrameRateKey, Rational.valueOf(30)),
                        null);

                // Call the start method of ScreenRecorder to begin recording
                screenRecorder.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopVideo() {

        if (takeVideo) {

            try {

                screenRecorder.stop();

                File video = screenRecorder.getCreatedMovieFiles().get(0);
                //String relativePath = "videos/" + video.getName();
                String relativePath = "videos/" + testName + ".avi";
                File destFile = new File(TestUtils.getReportDir(), relativePath);
                FileUtils.deleteQuietly(destFile);
                FileUtils.moveFile(video, destFile);
                log.info("VIDEO: " + destFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public boolean isTakeVideo() {
        return takeVideo;
    }
}
