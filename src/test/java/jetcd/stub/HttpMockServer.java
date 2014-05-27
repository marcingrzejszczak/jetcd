package jetcd.stub;

import com.github.tomakehurst.wiremock.WireMockServer;

public class HttpMockServer extends WireMockServer {

	static final int DEFAULT_PORT = 8001;

	public HttpMockServer(int port) {
		super(port);
	}

	public HttpMockServer() {
		super(DEFAULT_PORT);
	}

	public void shutdownServer() {
		if (isRunning()) {
			stop();
		}
		shutdown();
	}
}