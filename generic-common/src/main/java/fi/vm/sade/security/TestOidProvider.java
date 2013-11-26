package fi.vm.sade.security;

import java.util.*;

/**
 * Testeissä oikeuksien tarkastukseen käytetty organisaatiorakenne:
 *
 *  org1
 *      org1.1
 *          org1.1.1
 *          org1.1.2
 *  org2
 *      org2.1
 *
 * Tämä luokka siis pätkii oidit pisteiden kohdalta,
 * ja osaa palauttaa esim "xxx1.1.1" ===> "[xxx1.1.1, xxx1.1, xxx1]"
 *
 * (etc)
 *
 * @author Antti Salonen
 */
public class TestOidProvider extends OidProvider {

    @Override
    public List<String> getSelfAndParentOids(String oid) {
        List<String> oidpath = new ArrayList<String>();
        if (oid.contains(".")) { // parents
            oidpath.addAll(getSelfAndParentOids(oid.substring(0, oid.lastIndexOf("."))));
        }
        oidpath.add(oid); // self
        return oidpath;
    }

    public static void main(String[] args) { // simple test
        System.out.println(new TestOidProvider().getSelfAndParentOids("xxx1.2.3"));
    }
}
