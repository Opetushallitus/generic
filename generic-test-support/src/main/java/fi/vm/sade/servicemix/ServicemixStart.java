package fi.vm.sade.servicemix;

public final class ServicemixStart {

    private ServicemixStart() {
    }

    public static void main(String[] args) throws Exception {
        new ServicemixUtils(args).smxStop();
//        new ServicemixUtils().smxStartDaemon("Persistence bundle started.");
//        new ServicemixUtils().smxStartDaemon("Apache ServiceMix");
        // TODO: hack
        String waitforBundle = "koodisto-webservice";
        if (args != null && args.length >= 2) {
            waitforBundle = args[1];
        }
        new ServicemixUtils(args).smxStartDaemon(waitforBundle);
        System.out.println(" ***** SMX FIRST START OK, RESTARTING... *****");
        new ServicemixUtils(args).smxStop();
        new ServicemixUtils(args).smxStartDaemon(waitforBundle);
    }

}
