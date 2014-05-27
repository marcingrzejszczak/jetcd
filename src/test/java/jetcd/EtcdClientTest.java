/*
 * Copyright 2013 Diwaker Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetcd;

import static jetcd.stub.StubbedHttpResponseBuilder.*;
import static jetcd.stub.WireMockHttpRequestMapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import jetcd.stub.EtcdStubTest;
import retrofit.RetrofitError;

/**
 * Tests for EtcdClient.
 */
public final class EtcdClientTest extends EtcdStubTest {

	private final EtcdClient client = EtcdClientFactory.newInstance("http://localhost:8001");	

	@Test
	public void should_set_value_in_etcd() throws EtcdException {
		stubInteraction(wireMockPut("/v2/keys/hello"), okJsonResponse(valueSet("hello", "world")));
		try {
			client.set("hello", "world");
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void should_return_existing_value() throws EtcdException {
		stubInteraction(wireMockPut("/v2/keys/hello"), okJsonResponse(valueSet("hello", "world")));		

		String value = client.get("hello");
		
		assertThat(value).isEqualTo("world");
	}
	
	@Test
	public void should_fail_on_retrieving_non_existing_key() throws EtcdException {
		stubInteraction(wireMockGet("/v2/keys/non-existent"), notFoundJsonResponse(keyNotFound("non-existent")));

		try {
			client.get("non-existent");
			fail();
		} catch (EtcdException e) {
			assertThat(e.getErrorCode()).isEqualTo(100);
		}
	}

	@Test
	public void should_fail_when_etcd_server_is_down() throws EtcdException {
		EtcdClient client = EtcdClientFactory.newInstance("http://127.0.0.1:9999");
		try {
			client.get("hello");
			fail();
		} catch (RetrofitError e) {
			assertThat(e.getResponse()).isNull();
			assertThat(e.isNetworkError()).isTrue();
		}
	}

	@Test
	public void should_return_overridden_value() throws EtcdException {
		stubInteraction(wireMockGet("/v2/keys/hello")
				.inScenario("Value overridden")
				.willReturn(okJsonResponse(valueSet("hello", "value")))
				.whenScenarioStateIs("Initial value set"));
		stubInteraction(wireMockPut("/v2/keys/hello")
				.inScenario("Value overridden")
				.whenScenarioStateIs("Initial value set")
				.willReturn(okJsonResponse(valueOverriden("hello", "newValue", "value")))
				.willSetStateTo("Value overridden"));		
		stubInteraction(wireMockGet("/v2/keys/hello")
				.inScenario("Value overridden")
				.whenScenarioStateIs("Value overridden")
				.willReturn(okJsonResponse(valueSet("hello", "newValue"))));		
		String initialValue = client.get("hello");

		client.set("hello", "newValue");

		assertThat(initialValue).isEqualTo("value");
		String overriddenValue = client.get("hello");
		assertThat(overriddenValue).isEqualTo("newValue");
	}

	@Test
	public void testSetWithTtl() throws Exception {
		client.set("newKey", "newValue", 1);
		assertThat(client.get("newKey")).isEqualTo("newValue");

		// Wait for key to expire
		Thread.sleep(1500);
		try {
			client.get("newKey");
			fail();
		} catch (EtcdException e) {
			assertThat(e.getErrorCode()).isEqualTo(100);
		}
	}

	@Test
	public void testDelete() throws EtcdException {
		client.delete("hello");
		try {
			client.get("hello");
			fail();
		} catch (EtcdException e) {
			assertThat(e.getErrorCode()).isEqualTo(100);
		}

		try {
			client.delete("non-existent");
			fail();
		} catch (EtcdException e) {
			assertThat(e.getErrorCode()).isEqualTo(100);
		}
	}

	@Test
	public void testList() throws EtcdException {
		client.set("b/bar", "baz");
		client.set("b/foo", "baz");
		assertThat(client.list("b")).hasSize(2)
				.containsEntry("/b/bar", "baz").containsEntry("/b/foo", "baz");
	}

	@Test
	public void testCompareAndSwap() throws EtcdException {
		client.compareAndSwap("hello", "world", "new value");
		assertThat(client.get("hello")).isEqualTo("new value");

		try {
			client.compareAndSwap("hello", "bad old", "world");
			fail();
		} catch (EtcdException e) {
			assertThat(e.getErrorCode()).isEqualTo(101);
		}
	}

	private String valueSet(String key, String value) {
		return "{\"action\":\"set\"," +
				"\"node\":{\"key\":\"/" + key + "\",\"value\":\"" + value + "\",\"modifiedIndex\":1,\"createdIndex\":1}}";
	}

	private String keyNotFound(String key) {
		return "{\"errorCode\":100,\"message\":\"Key not found\",\"cause\":\"/" + key + "\",\"index\":1}";
	}
	
	private String valueOverriden(String key, String currentValue, String prevValue) {
		return "{\"action\":\"set\",\"node\":{\"key\":\"/" + key + "\",\"value\":\"" + currentValue + "\",\"modifiedIndex\":22,\"createdIndex\":22}," +
				"\"prevNode\":{\"key\":\"/" + key + "\",\"value\":\"" + prevValue + "\",\"modifiedIndex\":21,\"createdIndex\":21}}";
	}
}
