/*
 * @(#)ScreenRecorder.java 
 * 
 * Copyright (c) 2011-2012 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.screenrecorder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.BlockingQueue;

import org.monte.media.*;
import org.monte.media.converter.CodecChain;
import org.monte.media.converter.ScaleImageCodec;
import java.awt.GraphicsDevice;
import java.util.List;
import java.util.ArrayList;
import org.monte.media.beans.AbstractStateModel;
import javax.swing.SwingUtilities;
import javax.sound.sampled.AudioFormat;
import java.util.concurrent.ArrayBlockingQueue;
import org.monte.media.math.Rational;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine;

import org.monte.media.avi.AVIWriter;
import org.monte.media.color.Colors;
import org.monte.media.image.Images;
import org.monte.media.quicktime.QuickTimeWriter;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

/**
 * A screen recorder written in pure Java.
 * <p>
 * Captures the screen, the mouse cursor and audio.
 * <p>
 * This recorder uses four threads. Three capture threads for screen, mouse
 * cursor and audio, and one output thread for the movie writer.
 * <p>
 * FIXME - This class is a horrible mess.
 *
 * @author Werner Randelshofer
 * @version $Id: ScreenRecorder.java 210 2012-04-26 08:34:24Z werner $
 */
public class ScreenRecorder extends AbstractStateModel {

    public enum State {

        DONE, FAILED, RECORDING
    }
    private State state = State.DONE;
    /** 
     * "Encoding" for black mouse cursor. */
    public final static String ENCODING_BLACK_CURSOR = "black";
    /** 
     * "Encoding" for white mouse cursor. */
    public final static String ENCODING_WHITE_CURSOR = "white";
    /** 
     * The file format. "AVI" or "QuickTime" */
    private Format fileFormat;
    /** 
     * The input video format for cursor capture. "black" or "white". */
    private Format mouseFormat;
    /** 
     * The input video format for screen capture. */
    private Format screenFormat;
    /**
     * The input and output format for audio capture. */
    private Format audioFormat;
    /**
     * The bounds of the graphics device that we capture with AWT Robot. */
    private Rectangle captureArea;
    /**
     * The writer for the movie file. */
    private MovieWriter w;
    /**
     * The start time of the recording. */
    private long recordingStartTime;
    /**
     * The stop time of the recording. */
    private volatile long recordingStopTime;
    /**
     * The start time of the current movie file. */
    private long fileStartTime;
    /**
     * Holds the mouse captures made with {@code MouseInfo}. */
    private ArrayBlockingQueue<Buffer> mouseCaptures;
    /**
     * Timer for screen captures. */
    private ScheduledThreadPoolExecutor screenCaptureTimer;
    /**
     * Timer for mouse captures. */
    private ScheduledThreadPoolExecutor mouseCaptureTimer;
    /**
     * Thread for audio capture. */
    private ScheduledThreadPoolExecutor audioCaptureTimer;
    /** Thread for file writing. */
    private volatile Thread writerThread;
    /** Mouse cursor. */
    private BufferedImage cursorImg;
    /**
     * Hot spot of the mouse cursor in cursorImg. */
    private Point cursorOffset = new Point(-8, -5);
    /**
     * Object for thread synchronization. */
    private final Object sync = new Object();
    private ArrayBlockingQueue<Buffer> writerQueue;
    /** This codec encodes a video frame. */
    private Codec frameEncoder;
    /** outputTime and ffrDuration are needed for conversion of the
     * video stream from variable frame rate to fixed frame rate. 
     * FIXME - Do this with a CodecChain.
     */
    private Rational outputTime;
    private Rational ffrDuration;
    private ArrayList<File> recordedFiles;
    /** Id of the video track. */
    protected int videoTrack = 0;
    /** Id of the audio track. */
    protected int audioTrack = 1;
    /** The device from which screen captures are generated. */
    private GraphicsDevice captureDevice;
    private AudioGrabber audioGrabber;
    private ScreenGrabber screenGrabber;
    private MouseGrabber mouseGrabber;
    private ScheduledFuture audioFuture;
    private ScheduledFuture screenFuture;
    private ScheduledFuture mouseFuture;

    /** Creates a screen recorder.
     *
     * @param cfg Graphics configuration of the capture screen.
     */
    public ScreenRecorder(GraphicsConfiguration cfg) throws IOException, AWTException {
        this(cfg, null,
                // the file format
                new Format(FormatKeys.MediaTypeKey, MediaType.FILE,
                FormatKeys.MimeTypeKey, FormatKeys.MIME_QUICKTIME),
                //
                // the output format for screen capture
                new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO,
                FormatKeys.EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION,
                VideoFormatKeys.CompressorNameKey, VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION,
                VideoFormatKeys.DepthKey, 24, FormatKeys.FrameRateKey, new Rational(15, 1)),
                //
                // the output format for mouse capture 
                new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO,
                FormatKeys.EncodingKey, ENCODING_BLACK_CURSOR,
                FormatKeys.FrameRateKey, new Rational(30, 1)),
                //
                // the output format for audio capture 
                new Format(FormatKeys.MediaTypeKey, MediaType.AUDIO,
                FormatKeys.EncodingKey, AudioFormatKeys.ENCODING_QUICKTIME_TWOS_PCM,
                FormatKeys.FrameRateKey, new Rational(48000, 1),
                AudioFormatKeys.SampleSizeInBitsKey, 16,
                AudioFormatKeys.ChannelsKey, 2, AudioFormatKeys.SampleRateKey, new Rational(48000, 1),
                AudioFormatKeys.SignedKey, true, AudioFormatKeys.ByteOrderKey, ByteOrder.BIG_ENDIAN));
    }

    /** Creates a screen recorder.
     *
     * @param cfg Graphics configuration of the capture screen.
     * @param fileFormat The file format "AVI" or "QuickTime".
     * @param screenFormat The video format for screen capture.
     * @param mouseFormat The video format for mouse capture. The {@code EncodingKey}
     * must be ENCODING_BLACK_CURSOR or ENCODING_WHITE_CURSOR. The {@code SampleRateKey} can be
     * independent from the {@code screenFormat}. Specify null if you
     * don't want to capture the mouse cursor.
     * @param audioFormat  The audio format for audio capture. Specify null
     * if you don't want audio capture.
     */
    public ScreenRecorder(GraphicsConfiguration cfg,
            Format fileFormat,
            Format screenFormat,
            Format mouseFormat,
            Format audioFormat) throws IOException, AWTException {
        this(cfg, null, fileFormat, screenFormat, mouseFormat, audioFormat);
    }

    /** Creates a screen recorder.
     *
     * @param cfg Graphics configuration of the capture screen.
     * @param captureArea Defines the area of the screen that shall be captured.
     * @param fileFormat The file format "AVI" or "QuickTime".
     * @param screenFormat The video format for screen capture.
     * @param mouseFormat The video format for mouse capture. The {@code EncodingKey}
     * must be ENCODING_BLACK_CURSOR or ENCODING_WHITE_CURSOR. The {@code SampleRateKey} can be
     * independent from the {@code screenFormat}. Specify null if you
     * don't want to capture the mouse cursor.
     * @param audioFormat  The audio format for audio capture. Specify null
     * if you don't want audio capture.
     */
    public ScreenRecorder(GraphicsConfiguration cfg,
            Rectangle captureArea,
            Format fileFormat,
            Format screenFormat,
            Format mouseFormat,
            Format audioFormat) throws IOException, AWTException {

        this.fileFormat = fileFormat;
        this.screenFormat = screenFormat;
        this.mouseFormat = mouseFormat;
        if (this.mouseFormat == null) {
            this.mouseFormat = new Format(FormatKeys.FrameRateKey, new Rational(0, 0), FormatKeys.EncodingKey, ENCODING_BLACK_CURSOR);
        }
        this.audioFormat = audioFormat;
        this.recordedFiles = new ArrayList<File>();
        this.captureDevice = cfg.getDevice();
        this.captureArea = (captureArea == null) ? cfg.getBounds() : captureArea;
        if (mouseFormat != null && mouseFormat.get(FormatKeys.FrameRateKey).intValue() > 0) {
            mouseCaptures = new ArrayBlockingQueue<Buffer>(mouseFormat.get(FormatKeys.FrameRateKey).intValue()*2);
            if (this.mouseFormat.get(FormatKeys.EncodingKey).equals(ENCODING_BLACK_CURSOR)) {
                cursorImg = Images.toBufferedImage(Images.createImage(ScreenRecorder.class, "/org/monte/media/gui/images/Cursor.black.png"));
            } else {
                cursorImg = Images.toBufferedImage(Images.createImage(ScreenRecorder.class, "/org/monte/media/gui/images/Cursor.white.png"));
            }
        }

    }

    protected void createMovieWriter() throws IOException {
        File f = createMovieFile(fileFormat);
        recordedFiles.add(f);
        System.out.println("added: "+recordedFiles);

        w = Registry.getInstance().getWriter(fileFormat, f);

        // Create the video encoder
        Rational videoRate = Rational.max(screenFormat.get(FormatKeys.FrameRateKey), mouseFormat.get(FormatKeys.FrameRateKey));
        ffrDuration = videoRate.inverse();
        Format videoInputFormat = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO,
                FormatKeys.EncodingKey, VideoFormatKeys.ENCODING_BUFFERED_IMAGE,
                VideoFormatKeys.WidthKey, captureArea.width,
                VideoFormatKeys.HeightKey, captureArea.height,
                FormatKeys.FrameRateKey, videoRate).append(screenFormat);
        Format videoOutputFormat = new Format(
                FormatKeys.FrameRateKey, videoRate,
                FormatKeys.MimeTypeKey, fileFormat.get(FormatKeys.MimeTypeKey))//
                .append(screenFormat)//
                .append(new Format(//
                VideoFormatKeys.WidthKey, captureArea.width,
                VideoFormatKeys.HeightKey, captureArea.height));

        videoTrack = w.addTrack(videoOutputFormat);
        if (audioFormat != null) {
            audioTrack = w.addTrack(audioFormat);
        }

        Codec encoder = Registry.getInstance().getEncoder(w.getFormat(videoTrack));
        if (encoder == null) {
            throw new IOException("No encoder for format " + w.getFormat(videoTrack));
        }
        frameEncoder = encoder;
        frameEncoder.setInputFormat(videoInputFormat);
        frameEncoder.setOutputFormat(videoOutputFormat);
        if (frameEncoder.getOutputFormat() == null) {
            throw new IOException("Unable to encode video frames in this output format:" + videoOutputFormat);
        }

        // If the capture area does not have the same dimensions as the
        // video format, create a codec chain which scales the image before
        // performing the frame encoding.
        if (!videoInputFormat.intersectKeys(VideoFormatKeys.WidthKey, VideoFormatKeys.HeightKey).matches(
                videoOutputFormat.intersectKeys(VideoFormatKeys.WidthKey, VideoFormatKeys.HeightKey))) {
            ScaleImageCodec sic = new ScaleImageCodec();
            sic.setInputFormat(videoInputFormat);
            sic.setOutputFormat(videoOutputFormat.intersectKeys(VideoFormatKeys.WidthKey, VideoFormatKeys.HeightKey).append(videoInputFormat));
            frameEncoder = new CodecChain(sic, frameEncoder);
        }


        // FIXME - There should be no need for format-specific code.
        if (screenFormat.get(VideoFormatKeys.DepthKey) == 8) {
            if (w instanceof AVIWriter) {
                AVIWriter aviw = (AVIWriter) w;
                aviw.setPalette(videoTrack, Colors.createMacColors());
            } else if (w instanceof QuickTimeWriter) {
                QuickTimeWriter qtw = (QuickTimeWriter) w;
                qtw.setVideoColorTable(videoTrack, Colors.createMacColors());
            }
        }

        fileStartTime = System.currentTimeMillis();
    }

    /** Returns a list of all files that the screen recorder created. */
    public List<File> getCreatedMovieFiles() {
        return Collections.unmodifiableList(recordedFiles);
    }

    /** Creates a file for recording the movie. 
     * <p>
     * This implementation creates a file in the users "Video" folder on Windows,
     * or in the users "Movies" folders on Mac OS X.
     * <p>
     * You can override this method, if you would like to create a movie file
     * at a different location.
     * 
     * @param fileFormat
     * @return the file
     * @throws IOException 
     */
    protected File createMovieFile(Format fileFormat) throws IOException {
        File folder;
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            folder = new File(System.getProperty("user.home") + File.separator + "Videos");
        } else {
            folder = new File(System.getProperty("user.home") + File.separator + "Movies");
        }

        if (!folder.exists()) {
            folder.mkdirs();
        } else if (!folder.isDirectory()) {
            throw new IOException("\"" + folder + "\" is not a directory.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");

        File f = new File(folder,//
                "ScreenRecording " + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
        return f;
    }

    /** Returns the state of the recorder. */
    public State getState() {
        return state;
    }

    /** Setns the state of the recorder and fires a ChangeEvent. */
    private void setState(State newValue) {
        state = newValue;
        fireStateChanged();
    }

    /** Starts the screen recorder. */
    public void start() throws IOException {
        stop();
        recordedFiles.clear(); // TODO: anttis: clear oli väärässä paikassa, filet tyhjäytyi
        createMovieWriter();
        //recordedFiles.clear();
        recordingStartTime = System.currentTimeMillis();
        recordingStopTime = Long.MAX_VALUE;

        outputTime = new Rational(0, 0);
        startWriter();
        try {
            startScreenCapture();
        } catch (AWTException e) {
            IOException ioe = new IOException("Start screen capture failed");
            ioe.initCause(e);
            stop();
            throw ioe;
        } catch (IOException ioe) {
            stop();
            throw ioe;
        }
        if (mouseFormat != null && mouseFormat.get(FormatKeys.FrameRateKey).intValue() > 0) {
            startMouseCapture();
        }
        if (audioFormat != null) {
            try {
                startAudioCapture();
            } catch (LineUnavailableException e) {
                IOException ioe = new IOException("Start audio capture failed");
                ioe.initCause(e);
                stop();
                throw ioe;
            }
        }
        setState(State.RECORDING);
    }

    /** Starts screen capture. */
    private void startScreenCapture() throws AWTException, IOException {
        screenCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = max(1, (int) (1000 / screenFormat.get(FormatKeys.FrameRateKey).doubleValue()));
        screenGrabber = new ScreenGrabber(this, recordingStartTime);
        screenFuture=screenCaptureTimer.scheduleAtFixedRate(screenGrabber, delay, delay, TimeUnit.MILLISECONDS);
        screenGrabber.setFuture(screenFuture);
    }

    private static class ScreenGrabber implements Runnable {

        /** Previously draw mouse location. This is used to have the last mouse
         * location at hand, when a new screen capture has been created, but the
         * mouse has not been moved.
         */
        private Point prevDrawnMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        /**
         * Holds the screen capture made with AWT Robot. */
        private BufferedImage screenCapture;
        private ScreenRecorder recorder;
        private ScheduledThreadPoolExecutor screenTimer;
        /**
         * The AWT Robot which we use for capturing the screen. */
        private Robot robot;
        private Rectangle captureArea;
        /** Holds the composed image (screen capture and super-imposed mouse cursor).
         * This is the image that is written into the video track of the file.
         */
        private BufferedImage videoImg;
        /**
         * Graphics object for drawing into {@code videoImg}. */
        private Graphics2D videoGraphics;
        private final Format mouseFormat;
        /**
         * Holds the mouse captures made with {@code MouseInfo}. */
        private ArrayBlockingQueue<Buffer> mouseCaptures;
        /**
         * The time the previous screen frame was captured. */
        private Rational prevScreenCaptureTime;
        private final Object sync;
        private BufferedImage cursorImg;
        private Point cursorOffset;
        private int videoTrack;
        private long startTime;
        private volatile long stopTime = Long.MAX_VALUE;
        private ScheduledFuture future;
        private long sequenceNumber;

        public void setFuture(ScheduledFuture future) {this.future=future;}
        
        public synchronized void setStopTime(long newValue) {
            this.stopTime = newValue;
        }

        public synchronized long getStopTime() {
            return this.stopTime;
        }

        public ScreenGrabber(ScreenRecorder recorder, long startTime) throws AWTException, IOException {
            this.recorder = recorder;
            this.captureArea = recorder.captureArea;
            this.robot = new Robot(recorder.captureDevice);
            this.mouseFormat = recorder.mouseFormat;
            this.mouseCaptures = recorder.mouseCaptures;
            this.sync = recorder.sync;
            this.cursorImg = recorder.cursorImg;
            this.cursorOffset = recorder.cursorOffset;
            this.videoTrack = recorder.videoTrack;
            this.prevScreenCaptureTime = new Rational(startTime, 1000);
            this.startTime = startTime;

            Format screenFormat = recorder.screenFormat;
            if (screenFormat.get(VideoFormatKeys.DepthKey, 24) == 24) {
                videoImg = new BufferedImage(this.captureArea.width, this.captureArea.height, BufferedImage.TYPE_INT_RGB);
            } else if (screenFormat.get(VideoFormatKeys.DepthKey) == 16) {
                videoImg = new BufferedImage(this.captureArea.width, this.captureArea.height, BufferedImage.TYPE_USHORT_555_RGB);
            } else if (screenFormat.get(VideoFormatKeys.DepthKey) == 8) {
                videoImg = new BufferedImage(this.captureArea.width, this.captureArea.height, BufferedImage.TYPE_BYTE_INDEXED, Colors.createMacColors());
            } else {
                throw new IOException("Unsupported color depth " + screenFormat.get(VideoFormatKeys.DepthKey));
            }
            videoGraphics = videoImg.createGraphics();
            videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }

        @Override
        public void run() {
            try {
                grabScreen();
            } catch (Throwable ex) {
                ex.printStackTrace();
                screenTimer.shutdown();
                recorder.recordingFailed();
            }
        }

        /** Grabs a screen, generates video images with pending mouse captures
         * and writes them into the movie file.
         */
        private void grabScreen() throws IOException, InterruptedException {
            // Capture the screen
            BufferedImage previousScreenCapture = screenCapture;
            long timeBeforeCapture = System.currentTimeMillis();
            try {
                screenCapture = robot.createScreenCapture(captureArea);
            } catch (IllegalMonitorStateException e) {
                //IOException ioe= new IOException("Could not grab screen");
                //ioe.initCause(e);
                //throw ioe;
                // Screen capture failed due to a synchronization error
                return;
            }
            long timeAfterCapture = System.currentTimeMillis();
            if (previousScreenCapture == null) {
                previousScreenCapture = screenCapture;
            }
            videoGraphics.drawImage(previousScreenCapture, 0, 0, null);

            Buffer buf = new Buffer();
            buf.format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, FormatKeys.EncodingKey, VideoFormatKeys.ENCODING_BUFFERED_IMAGE);
            // Generate video frames with mouse cursor painted on them
            boolean hasMouseCapture = false;
            if (mouseFormat != null && mouseFormat.get(FormatKeys.FrameRateKey).intValue() > 0) {
                while (!mouseCaptures.isEmpty() && mouseCaptures.peek().timeStamp.compareTo(new Rational(timeAfterCapture, 1000)) < 0) {
                    Buffer mouseCapture = mouseCaptures.poll();
                    if (mouseCapture.timeStamp.compareTo(prevScreenCaptureTime) > 0) {
                        if (mouseCapture.timeStamp.compareTo(new Rational(timeBeforeCapture, 1000)) < 0) {
                            previousScreenCapture = screenCapture;
                            videoGraphics.drawImage(previousScreenCapture, 0, 0, null);
                        }

                        Point mcp = (Point) mouseCapture.data;
                        prevDrawnMouseLocation.setLocation(mcp.x - captureArea.x, mcp.y - captureArea.y);
                        Point p = prevDrawnMouseLocation;
                        long localStopTime = getStopTime();
                        if (mouseCapture.timeStamp.compareTo(new Rational(localStopTime, 1000)) > 0) {
                            break;
                        }
                        {
                            hasMouseCapture = true;

                            // draw cursor
                            videoGraphics.drawImage(cursorImg, p.x + cursorOffset.x, p.y + cursorOffset.y, null);
                            buf.clearFlags();
                            buf.data = videoImg;
                            buf.sampleDuration = mouseCapture.timeStamp.subtract(prevScreenCaptureTime);
                            buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
                            buf.track = videoTrack;
                            buf.sequenceNumber=sequenceNumber++;

                            // Fudge mouse position into the header
                            buf.header = p.x == Integer.MAX_VALUE ? null : p;
                            recorder.write(buf);
                            prevScreenCaptureTime = mouseCapture.timeStamp;

                            // erase cursor
                            videoGraphics.drawImage(previousScreenCapture, //
                                    p.x + cursorOffset.x, p.y + cursorOffset.y,//
                                    p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,//
                                    p.x + cursorOffset.x, p.y + cursorOffset.y,//
                                    p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,//
                                    null);
                        }

                    }
                }

                if (!hasMouseCapture && prevScreenCaptureTime.compareTo(new Rational(getStopTime(), 1000)) < 0) {
                    Point p = prevDrawnMouseLocation;
                    if (p != null) {
                        videoGraphics.drawImage(cursorImg, p.x + cursorOffset.x, p.y + cursorOffset.y, null);
                    }

                    buf.data = videoImg;
                    buf.sampleDuration = new Rational(timeAfterCapture, 1000).subtract(prevScreenCaptureTime);
                    buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
                    buf.track = videoTrack;
                    buf.sequenceNumber=sequenceNumber++;
                    buf.header = p.x == Integer.MAX_VALUE ? null : p;
                    recorder.write(buf);
                    prevScreenCaptureTime = new Rational(timeAfterCapture, 1000);
                    if (p != null) {//erase cursor
                        videoGraphics.drawImage(previousScreenCapture, //
                                p.x + cursorOffset.x, p.y + cursorOffset.y,//
                                p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,//
                                p.x + cursorOffset.x, p.y + cursorOffset.y,//
                                p.x + cursorOffset.x + cursorImg.getWidth() - 1, p.y + cursorOffset.y + cursorImg.getHeight() - 1,//
                                null);
                    }
                }
            } else if (prevScreenCaptureTime.compareTo(new Rational(getStopTime(), 1000)) < 0) {
                buf.data = videoImg;
                buf.sampleDuration = new Rational(timeAfterCapture, 1000).subtract(prevScreenCaptureTime);
                buf.timeStamp = prevScreenCaptureTime.subtract(new Rational(startTime, 1000));
                buf.track = videoTrack;
                buf.sequenceNumber=sequenceNumber++;
                buf.header = null; // no mouse position has been recorded for this frame
                recorder.write(buf);
                prevScreenCaptureTime = new Rational(timeAfterCapture, 1000);
            }
            
            if (timeBeforeCapture>getStopTime()) {
                future.cancel(false);
            }
        }

        public void close() {
            videoGraphics.dispose();
            videoImg.flush();
        }
    }

    /** Starts mouse capture. */
    private void startMouseCapture() {
        mouseCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = max(1, (int) (1000 / mouseFormat.get(FormatKeys.FrameRateKey).doubleValue()));
        mouseGrabber = new MouseGrabber(this, recordingStartTime, mouseCaptureTimer);
mouseFuture=        mouseCaptureTimer.scheduleAtFixedRate(mouseGrabber, delay, delay, TimeUnit.MILLISECONDS);
mouseGrabber.setFuture(mouseFuture);
    }

    private static class MouseGrabber implements Runnable {

        /** Previously captured mouse location. This is used to coalesce mouse
         * captures if the mouse has not been moved.
         */
        private Point prevCapturedMouseLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        private ScheduledThreadPoolExecutor timer;
        private ScreenRecorder recorder;
        private GraphicsDevice captureDevice;
        private Rectangle captureArea;
        private BlockingQueue<Buffer> mouseCaptures;
        private volatile long stopTime = Long.MAX_VALUE;
        private long startTime;
        private Format format;
        private ScheduledFuture future;

        public MouseGrabber(ScreenRecorder recorder, long startTime, ScheduledThreadPoolExecutor timer) {
            this.timer = timer;
            this.format = recorder.mouseFormat;
            this.captureDevice = recorder.captureDevice;
            this.captureArea = recorder.captureArea;
            this.mouseCaptures = recorder.mouseCaptures;
            this.startTime = startTime;
        }

        public void setFuture(ScheduledFuture future) {this.future=future;}
        
        @Override
        public void run() {
            try {
                grabMouse();
            } catch (Throwable ex) {
                ex.printStackTrace();
                timer.shutdown();
                recorder.recordingFailed();
            }
        }

        public synchronized void setStopTime(long newValue) {
            this.stopTime = newValue;
        }

        public synchronized long getStopTime() {
            return this.stopTime;
        }

        /** Captures the mouse cursor. */
        private void grabMouse() throws InterruptedException {
            long now = System.currentTimeMillis();
            if (now > getStopTime()) {
                future.cancel(false);
                return;
            }
            PointerInfo info = MouseInfo.getPointerInfo();
            Point p = info.getLocation();
            if (!info.getDevice().equals(captureDevice)
                    || !captureArea.contains(p)) {
                // If the cursor is outside the capture region, we
                // assign Integer.MAX_VALUE to its location.
                // This ensures that all mouse movements outside of the
                // capture region get coallesced. 
                p.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            // Only create a new capture event if the location has changed
            if (!p.equals(prevCapturedMouseLocation)) {
                Buffer buf = new Buffer();
                buf.format = format;
                buf.timeStamp = new Rational(now, 1000);
                buf.data = p;
                mouseCaptures.put(buf);
                prevCapturedMouseLocation.setLocation(p);
            }
        }

        public void close() {
        }
    }

    /** Starts audio capture. */
    private void startAudioCapture() throws LineUnavailableException {
        audioCaptureTimer = new ScheduledThreadPoolExecutor(1);
        int delay = 500;
        audioGrabber = new AudioGrabber(audioFormat, audioTrack, recordingStartTime, writerQueue);
        audioFuture=audioCaptureTimer.scheduleWithFixedDelay(audioGrabber, 0, 10, TimeUnit.MILLISECONDS);
        audioGrabber.setFuture(audioFuture);
    }

    /** This runnable grabs audio samples and enqueues them into the specified
     * BlockingQueue.
     * This runnable must be called twice a second.
     */
    private static class AudioGrabber implements Runnable {

        final private TargetDataLine line;
        final private BlockingQueue<Buffer> queue;
        final private Format audioFormat;
        final private int audioTrack;
        final private long startTime;
        private volatile long stopTime = Long.MAX_VALUE;
        private long totalSampleCount;
        private ScheduledFuture future;
        private long sequenceNumber;

        public AudioGrabber(Format audioFormat, int audioTrack, long startTime, BlockingQueue<Buffer> queue)
                throws LineUnavailableException {
            this.audioFormat = audioFormat;
            this.audioTrack = audioTrack;
            this.queue = queue;
            this.startTime = startTime;

            DataLine.Info info = new DataLine.Info(
                    TargetDataLine.class, AudioFormatKeys.toAudioFormat(audioFormat));
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();
        }

        public void setFuture(ScheduledFuture future) {
            this.future=future;
        }
        public void close() {
            line.close();
        }

        public synchronized void setStopTime(long newValue) {
            this.stopTime = newValue;
        }

        public synchronized long getStopTime() {
            return this.stopTime;
        }

        @Override
        public void run() {
            Buffer buf = new Buffer();
            AudioFormat lineFormat = line.getFormat();
            buf.format = AudioFormatKeys.fromAudioFormat(lineFormat);

            // For even sample rates, we select a buffer size that can 
            // hold half a second of audio. This allows audio/video interlave
            // twice a second, as recommended for AVI and QuickTime movies.
            // For odd sample rates, we have to select a buffer size that can hold
            // one second of audio. 
            int bufferSize = lineFormat.getFrameSize() * (int) lineFormat.getSampleRate();
            if (((int) lineFormat.getSampleRate() & 1) == 0) {
                bufferSize /= 2;
            }

            byte bdat[] = new byte[bufferSize];
            buf.data = bdat;
            Rational sampleRate = Rational.valueOf(lineFormat.getSampleRate());
            Rational frameRate = Rational.valueOf(lineFormat.getFrameRate());
            int count = line.read(bdat, 0, bdat.length);
            if (count > 0) {
                buf.sampleCount = count / (lineFormat.getSampleSizeInBits() / 8 * lineFormat.getChannels());
                buf.sampleDuration = sampleRate.inverse();
                buf.offset = 0;
                buf.sequenceNumber=sequenceNumber++;
                buf.length = count;
                buf.track = audioTrack;
                buf.timeStamp = new Rational(totalSampleCount, 1).divide(frameRate);

                // Check if recording should be stopped
                Rational stopTS = new Rational(getStopTime() - startTime, 1000);
                if (buf.timeStamp.add(buf.sampleDuration.multiply(buf.sampleCount)).compareTo(stopTS) > 0) {
                    // we recorderd too much => truncate the buffer 
                    buf.sampleCount = Math.max(0, (int) Math.ceil(stopTS.subtract(buf.timeStamp).divide(buf.sampleDuration).floatValue()));
                    buf.length = buf.sampleCount * (lineFormat.getSampleSizeInBits() / 8 * lineFormat.getChannels());
                    
                    future.cancel(false);
                }
                if (buf.sampleCount > 0) {
                    try {
                        queue.put(buf);
                    } catch (InterruptedException ex) {
                        // nothing to do
                    }
                }
                totalSampleCount += buf.sampleCount;
            }
        }
    }

    /** Starts file writing. */
    private void startWriter() {
        writerQueue = new ArrayBlockingQueue<Buffer>(
                Math.max(screenFormat.get(FormatKeys.FrameRateKey).intValue(), mouseFormat.get(FormatKeys.FrameRateKey).intValue()) + 1);
        writerThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (writerThread == this || !writerQueue.isEmpty()) {
                        try {
                            Buffer buf = writerQueue.take();
                            doWrite(buf);
                        } catch (InterruptedException ex) {
                            // We have been interrupted, terminate
                            break;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    recordingFailed();
                }
            }
        };
        writerThread.start();
    }

    private void recordingFailed() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    stop();
                    setState(State.FAILED);
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        });
    }

    /** Stops the screen recorder. 
     * <p>
     * Stopping the screen recorder may take several seconds, because audio
     * capture uses a large capture buffer. Also, the MovieWriter has to
     * finish up a movie file which may take some time depending on the amount
     * of meta-data that needs to be written.
     */
    public void stop() throws IOException {
        if (state == State.RECORDING) {
                recordingStopTime = System.currentTimeMillis();
            if (mouseCaptureTimer != null) {
                mouseGrabber.setStopTime(recordingStopTime);
            }
            if (screenCaptureTimer != null) {
                screenGrabber.setStopTime(recordingStopTime);
            }
            if (audioCaptureTimer != null) {
                audioGrabber.setStopTime(recordingStopTime);
            }


            try {
                if (mouseCaptureTimer != null) {
                    try {
                        mouseFuture.get();
                    } catch (InterruptedException ex) {
                    } catch (CancellationException ex) {
                    } catch (ExecutionException ex) {
                    }
                    mouseCaptureTimer.shutdown();
                    mouseCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
                    mouseCaptureTimer = null;
                    mouseGrabber.close();
                    mouseGrabber = null;
                }
                if (screenCaptureTimer != null) {
                    try {
                        screenFuture.get();
                    } catch (InterruptedException ex) {
                    } catch (CancellationException ex) {
                    } catch (ExecutionException ex) {
                    }
                    screenCaptureTimer.shutdown();
                    screenCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
                    screenCaptureTimer = null;
                    screenGrabber.close();
                    screenGrabber = null;
                }
                if (audioCaptureTimer != null) {
                    try {
                        audioFuture.get();
                    } catch (InterruptedException ex) {
                    } catch (CancellationException ex) {
                    } catch (ExecutionException ex) {
                    }
                    audioCaptureTimer.shutdown();
                    audioCaptureTimer.awaitTermination(5000, TimeUnit.MILLISECONDS);
                    audioCaptureTimer = null;
                    audioGrabber.close();
                    audioGrabber = null;
                }
            } catch (InterruptedException ex) {
                // nothing to do
            }
            
            Thread pendingWriterThread = writerThread;
            writerThread = null;

            
            try {
                if (pendingWriterThread != null) {
                    pendingWriterThread.interrupt();
                    pendingWriterThread.join();
                }
            } catch (InterruptedException ex) {
                // nothing to do
                ex.printStackTrace();
            }
            w.close();
            w = null;
            setState(State.DONE);
        }
    }
    long counter = 0;

    /** Writes a buffer into the movie. Since the file system may not be 
     * immediately available at all times, we do this asynchronously. 
     * <p>
     * The buffer is copied and passed to the writer queue, which is consumed
     * by the writer thread. See method startWriter().
     * <p>
     * AVI does not support a variable frame rate for the video track. Since
     * we can not capture frames at a fixed frame rate we have to resend the
     * same captured screen multiple times to the writer.
     * <p>
     * This method is called asynchronously from different threads. 
     * <p>
     * You can override this method if you wish to process the media data.
     * 
     * 
     * @param buf A buffer with un-encoded media data. If {@code buf.track==videoTrack},
     * then the buffer contains a {@code BufferedImage} in {@code buffer.data} 
     * and a {@code Point} in {@code buffer.header} with the recorded mouse
     * location. The header is null if the mouse is outside the capture area, or
     * mouse recording has not been enabled.
     * 
     * @throws IOException 
     */
    protected void write(Buffer buf) throws IOException, InterruptedException {
        MovieWriter writer = this.w;
        if (writer == null) {
            return;
        }
        if (buf.track == videoTrack) {
            if (writer.getFormat(videoTrack).get(VideoFormatKeys.FixedFrameRateKey, false) == false) {
                // variable frame rate is supported => easy
                Buffer wbuf = new Buffer();
                frameEncoder.process(buf, wbuf);
                writerQueue.put(wbuf);
            } else {// variable frame rate not supported => convert to fixed frame rate
                
                // FIXME - Use CodecChain for this
                
                Rational inputTime = buf.timeStamp.add(buf.sampleDuration);
                boolean isFirst = true;
                while (outputTime.compareTo(inputTime) < 0) {
                    buf.timeStamp = outputTime;
                    buf.sampleDuration = ffrDuration;
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        buf.setFlag(BufferFlag.SAME_DATA);
                    }
                    Buffer wbuf = new Buffer();
                    if (frameEncoder.process(buf, wbuf) != Codec.CODEC_OK) {
                        throw new IOException("Codec failed or could not process frame in a single step.");
                    }
                    writerQueue.put(wbuf);
                    outputTime = outputTime.add(ffrDuration);
                }
            }
        } else {
            Buffer wbuf = new Buffer();
            wbuf.setMetaTo(buf);
            wbuf.data = ((byte[]) buf.data).clone();
            wbuf.length = buf.length;
            wbuf.offset = buf.offset;
            writerQueue.put(wbuf);
        }
    }

    /**
     * The actual writing of the buffer happens here.
     * <p>
     * This method is called exclusively from the writer thread in startWriter(). 
     * 
     * @param buf
     * @throws IOException 
     */
    private void doWrite(Buffer buf) throws IOException {
        MovieWriter mw = w;
        mw.write(buf.track, buf);
        // Close file on a separate thread if file is full or an hour
        // has passed.
        long now = System.currentTimeMillis();
        //synchronized (sync) {
        if (mw.isDataLimitReached() || now - fileStartTime > 60 * 60 * 1000) {
            final MovieWriter closingWriter = mw;
            new Thread() {

                @Override
                public void run() {
                    try {
                        closingWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }.start();
            createMovieWriter();
        }
        //}
    }
}
