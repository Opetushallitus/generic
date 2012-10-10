package fi.vm.sade.generic.esb.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.headers.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Camel processor implementation that adds WS-Security
 * headers from current CXF call to given proxy.
 * This processor can only be used when endpoint is in PAYLOAD mode.
 */
public class CxfProxyAuthenticationProcessor implements Processor, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CxfProxyAuthenticationProcessor.class);

    private Object client;

    @Override
    public void process(Exchange exchange) throws Exception {
        if(exchange.getIn().getBody() instanceof CxfPayload) {
            CxfPayload<SoapHeader> payload = exchange.getIn().getBody(CxfPayload.class);
            List<SoapHeader> invokeHeaders = new ArrayList<SoapHeader>();
            for (SoapHeader header : payload.getHeaders()) {
                invokeHeaders.add(new SoapHeader(header.getName(), header.getObject()));
            }

            Client proxy = ClientProxy.getClient(this.client);
            proxy.getRequestContext().put("thread.local.request.context", "true");
            proxy.getRequestContext().put(Header.HEADER_LIST, invokeHeaders);
        } else {
            logger.warn("Couldn't use the processor because body was not CxfPayload.");
        }
    }

    public Object getClient() {
        return client;
    }

    public void setClient(Object client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(client, "You must set CXF client proxy to the processor");
    }
}
