package com.swift.dvengers.sandbox.mgwclient;

import javax.net.ssl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swift.dvengers.sandbox.util.Util;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClientApp {

	private OkHttpClient client = null;
	private String busAppName = "BO2";
	private String profileId = "trackerProfileChannel";
	private String sharedKey = "Abcd1234Abcd1234Abcd1234Abcd1234";
	private final static Logger LOG = LoggerFactory.getLogger(ClientApp.class);

	public ClientApp() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		
		/* Setting up custom host name verifier to ignore the host name in the certificate provided by MGW.
		 * This is only applicable for testing in sand box environment and should not be used in Live.
		 */
		builder.hostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		/* Setting up the trust store to connect to microgateway. */
		System.setProperty("javax.net.ssl.trustStore", "config\\demo.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "Abcd1234");

		client = builder.build();
	}
	
	public static void main(String[] args) {
		ClientApp app = new ClientApp();

		LOG.info("Starting the Client Application..\n");
		/* Run swift-apitracker/v4/payments/changed/transactions. */
		app.runPaymentsChangedTransactions();
	}

	public OkHttpClient getClient() {
		return client;
	}

	public String getBusAppName() {
		return busAppName;
	}

	public String getProfileId() {
		return profileId;
	}

	public String getSharedKey() {
		return sharedKey;
	}

	public void runPaymentsChangedTransactions() {
		try {
			/* Implementation for V4 changed transactions. */
			String url = "https://localhost:9003/swift/mgw/swift-apitracker/v4/payments/changed/transactions";
			String jwsSign = null;

			String payload = Util.buildPayload(getBusAppName(), getProfileId(), url, null);			
			/* Sign the request. */
			jwsSign = Util.sign(payload, getSharedKey());
			LOG.info("Request JWS Signature : " + jwsSign + "\n");
			
			/* Add authorization header to the request. */
			Request request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + jwsSign)
					.addHeader("X-SWIFT-Signature", "false").build();

			Call call = client.newCall(request);
			Response response = call.execute();
			
			String jwsToken = response.header("Authorization");
			String apiresp = response.body().string();
			
			if (Util.verifyResponse(apiresp, jwsToken, getSharedKey())) {
				LOG.info("Response JWS signature is successfully verified.\n");
			} else {
				LOG.info("Failed to verify response JWS signature.\n");
			}
			/* API response. */
			LOG.info("\nResponse : " + apiresp);
		} catch (Exception ex) {
			LOG.error("Error in running changed transactions");
			ex.printStackTrace();
		}
	}	
}
