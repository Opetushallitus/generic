package fi.vm.sade.generic.esb.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.MessageContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Camel processor implementation that adds WS-Security
 * headers from current CXF call to given proxy.
 * This processor can only be used when endpoint is in PAYLOAD mode.
 */
public class SoapHeaderDelegatingProcessor implements Processor, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SoapHeaderDelegatingProcessor.class);

    private final List<Object> clients = new ArrayList<Object>();
    private List<QName> qNames;

    @Override
    public void process(Exchange exchange) throws Exception {
        // Payload mode
        if(exchange.getIn().getBody() instanceof CxfPayload) {
            CxfPayload<SoapHeader> payload = exchange.getIn().getBody(CxfPayload.class);
            List<SoapHeader> invokeHeaders = new ArrayList<SoapHeader>();
            for (SoapHeader header : payload.getHeaders()) {
                if(qNames.contains(header.getName())) {
                    invokeHeaders.add(new SoapHeader(header.getName(), header.getObject()));
                }
            }

            addHeadersToAllClients(invokeHeaders);
        // POJO mode
        } else if(exchange.getIn().getBody() instanceof MessageContentsList) {
            MessageContentsList list = exchange.getIn().getBody(MessageContentsList.class);
            List<SoapHeader> soapHeaders = exchange.getIn().getHeader(Header.HEADER_LIST, List.class);
            List<SoapHeader> invokeHeaders = new ArrayList<SoapHeader>();
            for(SoapHeader header : soapHeaders) {
                if(qNames.contains(header.getName())) {
                    invokeHeaders.add(new SoapHeader(header.getName(), header.getObject()));
                }
            }

            addHeadersToAllClients(invokeHeaders);
        } else {
            logger.warn("Couldn't use the processor because body was not CxfPayload or MessageContentsList.");
        }
    }

    private void addHeadersToAllClients(List<SoapHeader> soapHeaders) {
        for(Object client : this.clients) {
            try {
                Client proxy = ClientProxy.getClient(client);
                proxy.getRequestContext().put("thread.local.request.context", "true");
                proxy.getRequestContext().put(Header.HEADER_LIST, soapHeaders);
            } catch (Exception e) {
                // Most likely not client proxy.
                logger.warn("Header delegating for client " + client + " of type " + (client != null ? client.getClass().getName() : null) + " failed", e);
            }
        }
    }

    public void setClient(Object client) {
        this.clients.add(client);
    }

    public void setClients(List<Object> clients) {
        this.clients.addAll(clients);
    }

    public List<QName> getqNames() {
        return qNames;
    }

    public void setqNames(List<QName> qNames) {
        this.qNames = qNames;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(clients.isEmpty(), "You must set CXF client proxy to the processor");
        Assert.notNull(qNames, "List of delegated qnames is null.");
    }
}
