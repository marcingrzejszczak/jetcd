package jetcd.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class StubbedHttpResponseBuilder {
   
    public static ResponseDefinitionBuilder okJsonResponse(String responseBody) {
        return createResponse(200, responseBody, "application/json");
    }
	
    public static ResponseDefinitionBuilder notFoundJsonResponse(String responseBody) {
        return createResponse(404, responseBody, "application/json");
    }

    private static ResponseDefinitionBuilder createResponse(int status, String responseBody, String contentType) {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", contentType)
                .withBody(responseBody);
    }   
        
}