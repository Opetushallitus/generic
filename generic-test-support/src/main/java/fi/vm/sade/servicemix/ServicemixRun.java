package fi.vm.sade.servicemix;

/**
 * Main program to start servicemix and keep it running
 */
public final class ServicemixRun {

    private ServicemixRun() {
    }

    public static void main(String[] args) throws Exception {
        new ServicemixUtils(args).smxStop();
        new ServicemixUtils(args).smxStartDaemon("[KEEP_RUNNING]"); // keeps running until exitted/killed
    }

}
