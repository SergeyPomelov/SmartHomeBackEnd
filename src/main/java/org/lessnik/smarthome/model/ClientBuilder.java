package org.lessnik.smarthome.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Properties;


@Slf4j
@RequiredArgsConstructor
public final class ClientBuilder {

    public static HttpClient getClient() {
        try {
            final Properties props = System.getProperties();
            props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            }};

            SSLParameters parameters = new SSLParameters();
            parameters.setEndpointIdentificationAlgorithm("");
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .sslContext(sc)
                    .sslParameters(parameters)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("HttpClient initialization failed");
    }
}
