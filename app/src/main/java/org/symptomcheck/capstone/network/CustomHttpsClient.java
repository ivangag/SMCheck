package org.symptomcheck.capstone.network;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * This is an example of an HTTP client that does not properly
 * validate SSL certificates that are used for HTTPS. You should
 * NEVER use a client like this in a production application. Self-signed
 * certificates are usually only OK for testing purposes, such as
 * this use case. 
 * 
 * @author jules
 *
 */
public class CustomHttpsClient {

    //TODO#BPR_5 Http Client used to interacts over the network via HTTP(S)
	public static HttpClient createUnsafeClient() {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build());

            return HttpClients.custom()
                    .setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
