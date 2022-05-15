package com.spring.resource.server;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SpringResourceServerLiveTests {
	private final String redirectUrl = "http://localhost:8080/";
	private final String authorizeUrlPattern = "http://localhost:8083/auth/realms/baeldung/protocol/openid-connect/auth?response_type=code&client_id=fooClient&scope=%s&redirect_uri=" + redirectUrl;
	private final String tokenUrl = "http://localhost:9000/oauth2/token";
	private final String resourceUrl = "http://localhost:8081/resource-server-jwt/foos";

	@Test
	void contextLoads() {
	}
	@Test
	public void givenUserWithReadScope_whenGetFooResource_thenSuccess() {
		String accessToken = obtainClientAccessToken("read");

		Response response = RestAssured.given()
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.get("http://localhost:8081/resource-server-jwt/foos");
		assertThat(response.as(List.class)).hasSizeGreaterThan(0);
	}
	private String obtainAccessToken(String scopes) {
		// obtain authentication url with custom codes
		Response response = RestAssured.given()
				.redirects()
				.follow(false)
				.get(String.format(authorizeUrlPattern, scopes));
		String authSessionId = response.getCookie("AUTH_SESSION_ID");
		String kcPostAuthenticationUrl = response.asString()
				.split("action=\"")[1].split("\"")[0].replace("&amp;", "&");

		// obtain authentication code and state
		response = RestAssured.given()
				.redirects()
				.follow(false)
				.cookie("AUTH_SESSION_ID", authSessionId)
				.formParams("username", "john@test.com", "password", "123", "credentialId", "")
				.post(kcPostAuthenticationUrl);
		assertThat(HttpStatus.FOUND.value()).isEqualTo(response.getStatusCode());

		// extract authorization code
		String location = response.getHeader(HttpHeaders.LOCATION);
		String code = location.split("code=")[1].split("&")[0];

		// get access token
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		params.put("client_id", "fooClient");
		params.put("redirect_uri", redirectUrl);
		params.put("client_secret", "fooClientSecret");
		response = RestAssured.given()
				.formParams(params)
				.post(tokenUrl);
		return response.jsonPath()
				.getString("access_token");
	}
	private String obtainClientAccessToken(String scopes) {

		// get access token
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "client_credentials");
		params.put("client_id", "client_id1");
		params.put("client_secret", "client_secret1");
		params.put("scope", "read");
		Response  response = RestAssured.given()
				.formParams(params).contentType("application/x-www-form-urlencoded")
				.post(tokenUrl);
		return response.jsonPath()
				.getString("access_token");
	}

}
