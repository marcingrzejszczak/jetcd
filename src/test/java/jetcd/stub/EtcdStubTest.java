package jetcd.stub;

import org.junit.After;
import org.junit.Before;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

public abstract class EtcdStubTest {
	protected WireMock wireMock;
	protected HttpMockServer httpMockServer;
	
	@Before
	public void setEtcdStub() {
		httpMockServer = new HttpMockServer();
		httpMockServer.start();
		wireMock = new WireMock("localhost", httpMockServer.port());
		wireMock.resetMappings();	
	}
	
	@After
	public void stopEtcdStub() {
		httpMockServer.shutdownServer();
	}

	protected void stubInteraction(MappingBuilder mapping, ResponseDefinitionBuilder response) {
		wireMock.register(mapping.willReturn(response));
	}
	
	protected void stubInteraction(MappingBuilder mapping) {
		wireMock.register(mapping);
	}
	
}
