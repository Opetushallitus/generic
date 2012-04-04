package fi.vm.sade.servicemix;

public final class ServicemixStart {

    private ServicemixStart() {
    }

    public static void main(String[] args) throws Exception {
        new ServicemixUtils(args).smxStop();
//        new ServicemixUtils().smxStartDaemon("Persistence bundle started.");
//        new ServicemixUtils().smxStartDaemon("Apache ServiceMix");
        // TODO: hack
        String waitforBundle = "generic-service";
        if (args != null && args.length >= 2) {
            waitforBundle = args[1];
        }
        ServicemixUtils smx1 = new ServicemixUtils(args);
        smx1.smxStartDaemon(waitforBundle);
        if (smx1.hasErrors) {
            // should be stopped already
            System.out.println(" ***** SMX ERROR !!! *****");
            System.out.println("servicemix error line: "+smx1.errorLine);
            throw new RuntimeException("FAILED to start servicemix, errorline: "+smx1.errorLine);
        } else {
            System.out.println(" ***** SMX FIRST START OK, RESTARTING... *****");
            new ServicemixUtils(args).smxStop();
            new ServicemixUtils(args).smxStartDaemon(waitforBundle);
        }
    }

}
