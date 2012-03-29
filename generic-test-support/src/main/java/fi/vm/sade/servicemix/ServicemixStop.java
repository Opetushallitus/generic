package fi.vm.sade.servicemix;

public final class ServicemixStop {

    private ServicemixStop() {
    }

    public static void main(String[] args) throws Exception {
        new ServicemixUtils(args).smxStop();
    }

}
