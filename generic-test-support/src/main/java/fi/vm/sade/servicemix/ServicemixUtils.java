package fi.vm.sade.servicemix;

import java.io.*;
import java.util.Date;

/**
 * Runs Servicemix server, useful for example in tests
 * 
 * @author Antti Salonen
 */
public class ServicemixUtils {

    private static File smxHomeDir = new File("target/servers/apache-servicemix-4.4.1");
    public static final int WAIT_MS = 1000;
    public static final int WAIT_MILLIS_BEFORE_TOUCH_DEPLOY = 10000;
    public static final int LOG_TAIL_SLEEP_MILLIS = 100;
    public static final int SMX_WAIT_LOG_INTERVAL = 10;

    private boolean ready = false;
    private boolean installed = false;
    private boolean stopped = false;
    public boolean hasErrors = false;
    @SuppressWarnings("unused")
    private BufferedWriter out;
    private String startedString = "[startedStringNotNeededByDefault]";
    public boolean shutDown = false;
    public String errorLine;

    @Deprecated
    public static void main(String[] args) throws Exception {
        // new ServicemixUtils().smxStop();
        // new ServicemixUtils().smxStart();
        // new ServicemixUtils().smxInstallDaemon();

        // smxStartAndInstallDaemon();
    }

    public ServicemixUtils(String[] args) {
        if (args != null) {
            if (args.length > 0) {
                setSmxHomeDir(new File(args[0]));
            }
        }
        System.out.println("[[ServicemixUtils, smxHomeDir: " + smxHomeDir.getAbsolutePath() + "]]");
    }

    private static void setSmxHomeDir(File smxHomeDir) {
        ServicemixUtils.smxHomeDir = smxHomeDir;
    }

    /*
     * public ServicemixUtils smxInstall() throws Exception {
     * System.out.println("installing bundles to servicemix...");
     * ServicemixCommander.installKoodisto();
     * System.out.println("installing bundles to servicemix done."); installed =
     * true; return this; }
     * 
     * public void smxInstallDaemon() { Thread thread = new Thread() {
     * 
     * @Override public void run() { try { smxInstall(); } catch (Exception e) {
     * throw new RuntimeException(e); }
     * System.out.println("smxInstallDaemon thread done.."); } };
     * thread.setDaemon(true); thread.start();
     * waitUntilReadyOrStoppedOrInstalled("SMXINSTALLD");
     * System.out.println("smxInstallDaemon done."); }
     */

    // public static void smxStartAndInstall() throws Exception {
    // new ServicemixUtils().smxStop();
    // new ServicemixUtils().smxStart("Persistence bundle started.");
    // new
    // ServicemixUtils().smxInstall().waitUntilReadyOrStoppedOrInstalled("SMXSTARTINSTALL");
    // }

    // public static void smxStartAndInstallDaemon() {
    // final ServicemixUtils smxInstUtils = new ServicemixUtils();
    // Thread thread = new Thread() {
    // @Override
    // public void run() {
    // try {
    // new ServicemixUtils().smxStop();
    // new ServicemixUtils().smxStart("Persistence bundle started.");
    // smxInstUtils.smxInstall();
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // };
    // thread.setDaemon(true);
    // thread.start();
    // smxInstUtils.waitUntilReadyOrStoppedOrInstalled("SMXSTARTINSTALLD");
    // }

    public void smxStartDaemon(final String waitforBundle) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    smxStart(waitforBundle);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        waitUntilReadyOrStoppedOrInstalled("SMXSTARTD");
    }

    public void smxStart(String waitforBundle) throws IOException {
        this.startedString = "Application context successfully refreshed (OsgiBundleXmlApplicationContext(bundle=" + waitforBundle;
        tailLogDaemon();
        touchDeployFiles();
        exec(smxHomeDir, smxcommand("servicemix"), false, "SMX");
    }

    public void smxStop() throws IOException {
        exec(smxHomeDir, smxcommand("stop"), true, "SMXSTOP");
    }

    private void exec(File dir, String command, boolean markStoppedAfter, final String prefix) throws IOException {
        System.out.println("executing: " + command + " (dir: " + dir.getAbsolutePath() + ")");
        // System.setIn(new ByteArrayInputStream(new byte[0]));
        Process p = Runtime.getRuntime().exec(command.split(" "), null, dir);
        // out = new BufferedWriter(new
        // OutputStreamWriter(p.getOutputStream()));
        followstream(p.getInputStream(), markStoppedAfter, "[" + prefix + "-OUT] ");
        followstream(p.getErrorStream(), markStoppedAfter, "[" + prefix + "-ERR] ");
        // sendKeysToProcess(1000, "XXX\n");
        // while (wait && !stopped[0]) {
        waitUntilReadyOrStoppedOrInstalled(prefix);
        System.out.println("executed: " + command);
    }

    /*
     * private void sendKeysToProcess(final int millis, final String str) {
     * Thread thread = new Thread() {
     * 
     * @Override public void run() { while (true) { try { Thread.sleep(millis);
     * //System.err.println("sendKeysToProcess: "+str); out.write(str);
     * out.flush(); } catch (Exception e) {
     * System.err.println("ERROR in sendKeysToProcess: " + e); //throw new
     * Error(e); } } } }; thread.setDaemon(true); thread.start(); }
     */

    private void waitUntilReadyOrStoppedOrInstalled(String info) {
        int i = 0;
        while (!shutDown && !ready && !stopped && !installed) {
            try {
                if (!"true".equals(System.getenv("SMX_WAIT_QUIET"))) {
                    if (i % SMX_WAIT_LOG_INTERVAL == 0) { // logita vain joka
                                                          // kymmenes kierros
                        System.out.println("[[wait for: " + info + "...]]");
                    }
                }
                Thread.sleep(WAIT_MS);
            } catch (Exception e) {
                throw new Error(e);
            }
            i++;
        }
        System.out.println("[[wait done for: " + info + " (ready: " + ready + ", stopped: " + stopped + ", installed: " + installed + ")]]");
    }

    private void followstream(InputStream inputStream, final boolean markStoppedAfter, final String prefix) {
        final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(prefix + line);
                        checkError(line);
                        if (line.contains(startedString)) {
                            ready = true;
                        }
                    }
                } catch (Exception e) {
                    throw new Error(e);
                } finally {
                    if (markStoppedAfter) {
                        stopped = true;
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private void checkError(String line) throws IOException {
        if (line.contains("Unresolved constraint in bundle") || line.contains("Unsatisfied requirements") || line.contains("OutOfMemoryError")
                || line.contains("Address already in use") || line.contains("java.lang.Exception: No valid revisions in bundle archive directory:") // occurs
                                                                                                                                                    // in
                                                                                                                                                    // bamboo
                                                                                                                                                    // sometimes
        ) {
            System.out.println("ERROR encountered in srevicemix, stopping... - line was: " + line);
            hasErrors = true;
            errorLine = line;
            smxStop();
            throw new RuntimeException("ERROR IN SERVICEMIX: " + line);
        }
    }

    private String smxcommand(final String cmd) {
        if (isWindows()) {
            return "cmd /c bin\\" + cmd + ".bat";
        } else {
            return "bin/" + cmd; // TODO: sh/bash?
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    private void tailLogDaemon() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    boolean running = true;
                    long updateInterval = LOG_TAIL_SLEEP_MILLIS;
                    File file = new File(smxHomeDir, "data/log/servicemix.log");
                    long filePointer = file.length(); // start tailing at the
                                                      // end
                    while (!shutDown && running) {
                        Thread.sleep(updateInterval);
                        long len = file.length();
                        if (len < filePointer) {
                            // Log must have been jibbled or deleted.
                            System.out.println("[SMX-LOG] " + "Log file was reset. Restarting logging from start of file.");
                            filePointer = len;
                        } else if (len > filePointer) {
                            // File must have had something added to it!
                            RandomAccessFile raf = new RandomAccessFile(file, "r");
                            raf.seek(filePointer);
                            String line = null;
                            while ((line = raf.readLine()) != null) {
                                System.out.println("[SMX-LOG] " + line);
                                checkError(line);
                                if (line.contains(startedString)) {
                                    ready = true;
                                }
                            }
                            filePointer = raf.getFilePointer();
                            raf.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[SMX-LOG] " + "Fatal error reading log file, log tailing has stopped.");
                }
                // dispose();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private void touchDeployFiles() throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(WAIT_MILLIS_BEFORE_TOUCH_DEPLOY);
                    File smxDeployDir = new File(smxHomeDir, "deploy");
                    File[] files = smxDeployDir.listFiles();
                    for (File file : files) {
                        if (file.isFile()) {
                            if (isWindows()) {
                                boolean b = file.setLastModified(new Date().getTime());
                                System.out.println("[[touch deploy file: " + file.getAbsolutePath() + " = " + b + "]]");
                            } else {
                                System.out.println("[[touch deploy file: " + file.getAbsolutePath() + "]]");
                                String command = "touch " + file.getName();
                                Process p = Runtime.getRuntime().exec(command, null, smxDeployDir);
                                followstream(p.getInputStream(), false, "[" + command + "-OUT] ");
                                followstream(p.getErrorStream(), false, "[" + command + "-ERR] ");
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

}
