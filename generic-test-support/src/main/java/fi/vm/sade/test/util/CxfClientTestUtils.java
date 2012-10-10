package fi.vm.sade.test.util;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.junit.Assert;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test utils for CXF objects.
 * User: Tommi Hännikkälä
 * Date: 10/10/12
 * Time: 9:53 AM
 */
public class CxfClientTestUtils {

    private static final Map<Client, CounterInterceptor> counterInterceptors = new HashMap<Client, CounterInterceptor>();

    public static void expectClientHeaderExists(QName header, Object client) {
        Client cxfClient = ClientProxy.getClient(client);
        cxfClient.getOutInterceptors().add(new HeaderAssertInterceptor(Phase.POST_PROTOCOL, header));
    }

    /**
     * Adds interceptor to listen, how many times client has been called.
     * @param client
     */
    public static void expectCall(Object client) {
        Client cxfClient = ClientProxy.getClient(client);
        CounterInterceptor counterInterceptor = new CounterInterceptor(Phase.POST_PROTOCOL);
        cxfClient.getOutInterceptors().add(counterInterceptor);
        counterInterceptors.put(cxfClient, counterInterceptor);
    }

    /**
     * Verifies that client has been called given times. Add listener by calling
     * {@link fi.vm.sade.test.util.CxfClientTestUtils.expectCall} first
     * @param client
     * @param times
     */
    public static void verifyCalled(Object client, int times) {
        Client cxfClient = ClientProxy.getClient(client);
        CounterInterceptor interceptor = counterInterceptors.get(cxfClient);
        Assert.assertEquals("Incorrect times of call.", times, interceptor.count);
    }

    private static class HeaderAssertInterceptor extends AbstractPhaseInterceptor<Message> {

        private QName headerName;

        private HeaderAssertInterceptor(String phase, QName headerName) {
            super(phase);
            this.headerName = headerName;
        }

        @Override
        public void handleMessage(Message message) throws Fault {
            List<SoapHeader> headers = CastUtils.cast((List) message.get(Header.HEADER_LIST));
            SoapHeader headerToFind = null;
            for(SoapHeader header : headers) {
                if(header.getName().equals(this.headerName)) {
                    headerToFind = header;
                    break;
                }
            }
            Assert.assertNotNull("Header " + headerName + " must be set.", headerToFind);
        }
    }

    private static class CounterInterceptor extends AbstractPhaseInterceptor<Message> {

        private int count = 0;

        private CounterInterceptor(String phase) {
            super(phase);
        }

        @Override
        public void handleMessage(Message message) throws Fault {
            count += 1;
        }
    }
}
