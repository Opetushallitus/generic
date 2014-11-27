package fi.vm.sade.generic.rest;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

public class HttpException extends IOException {

    private int statusCode;
    private String statusMsg;
    private String errorContent;

    public HttpException(HttpRequestBase req, HttpResponse response, String message) {
        super(message);
        this.statusCode = response.getStatusLine().getStatusCode();
        this.statusMsg = response.getStatusLine().getReasonPhrase();
        try {
            this.errorContent = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            CachingRestClient.logger.error("error reading errorContent: "+e, e);
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public String getErrorContent() {
        return errorContent;
    }
}
