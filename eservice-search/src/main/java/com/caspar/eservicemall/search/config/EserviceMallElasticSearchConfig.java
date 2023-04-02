package com.caspar.eservicemall.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.nacos.shaded.com.google.common.base.Supplier;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class EserviceMallElasticSearchConfig {
    @Value("${elasticsearch.username}")
    private String elasticUserName;
    @Value("${elasticsearch.passwd}")
    private String elasticPasswd;
    @Value("${elasticsearch.ip}")
    private String ip;
    @Value("${elasticsearch.port}")
    private String port;
    @Value("${elasticsearch.protocol}")
    private String protocol;
    @Bean
    public ElasticsearchClient elasticsearchClient() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        //设置账号密码
        credentialsProvider.setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(elasticUserName, elasticPasswd));

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
        final MyHostnameVerifier hostnameVerifier = new MyHostnameVerifier();
        RestClient restClient = RestClient.builder(new HttpHost(ip, Integer.parseInt(port), "https"))
                .setHttpClientConfigCallback(httpClientBuilder->httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(hostnameVerifier)
                ).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return client;
    }

    /**
     * 重写HostnameVerifier接口
     */
    private static class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public  static void main(String[] args) {
        Supplier<Integer> integerSupplier = () -> 5;
        System.out.println(integerSupplier);
    }
}
