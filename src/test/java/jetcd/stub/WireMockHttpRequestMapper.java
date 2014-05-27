package jetcd.stub;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

public class WireMockHttpRequestMapper {

    public static MappingBuilder wireMockGet(String path) {
        return WireMock.get(WireMock.urlEqualTo(path));
    }
    
    public static MappingBuilder wireMockPut(String path) {
        return WireMock.put(WireMock.urlEqualTo(path));
    }
    
}