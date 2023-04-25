package com.caspar.eservicemall.thirdparty;

import com.alibaba.fastjson.JSON;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.core.http.HttpClient;
import com.aliyun.core.http.HttpMethod;
import com.aliyun.core.http.ProxyOptions;
import com.aliyun.httpcomponent.httpclient.ApacheAsyncHttpClientBuilder;
import com.aliyun.sdk.service.dysmsapi20170525.models.*;
import com.aliyun.sdk.service.dysmsapi20170525.*;
import com.caspar.eservicemall.thirdparty.service.impl.SmsServiceImpl;
import com.google.gson.Gson;
import darabonba.core.RequestConfiguration;
import darabonba.core.TeaResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import darabonba.core.interceptor.InterceptorContext;
import darabonba.core.utils.CommonUtil;
import darabonba.core.TeaPair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//import javax.net.ssl.KeyManager;
//import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class EserviceThirdPartyApplicationTests {

	@Test
	void contextLoads() {

	}

    @Autowired
	SmsServiceImpl smsService;
	@Test
	void testSms() throws ExecutionException, InterruptedException {
		// HttpClient Configuration
        /*HttpClient httpClient = new ApacheAsyncHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(10)) // Set the connection timeout time, the default is 10 seconds
                .responseTimeout(Duration.ofSeconds(10)) // Set the response timeout time, the default is 20 seconds
                .maxConnections(128) // Set the connection pool size
                .maxIdleTimeOut(Duration.ofSeconds(50)) // Set the connection pool timeout, the default is 30 seconds
                // Configure the proxy
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<your-proxy-hostname>", 9001))
                        .setCredentials("<your-proxy-username>", "<your-proxy-password>"))
                // If it is an https connection, you need to configure the certificate, or ignore the certificate(.ignoreSSL(true))
                .x509TrustManagers(new X509TrustManager[]{})
                .keyManagers(new KeyManager[]{})
                .ignoreSSL(false)
                .build();*/

		// Configure Credentials authentication information, including ak, secret, token
		StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
				.accessKeyId("ak")
				.accessKeySecret("secret")
				//.securityToken("<your-token>") // use STS token
				.build());

		// Configure the Client
		AsyncClient client = AsyncClient.builder()
				.region("cn-hangzhou") // Region ID
				//.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
				.credentialsProvider(provider)
				//.serviceConfiguration(Configuration.create()) // Service-level configuration
				// Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
				.overrideConfiguration(
						ClientOverrideConfiguration.create()
								.setEndpointOverride("dysmsapi.aliyuncs.com")
						//.setConnectTimeout(Duration.ofSeconds(30))
				)
				.build();
		String code="8906";
		Map<String,String> codeMap=new HashMap<String,String>();
		codeMap.put("code",code);
		// Parameter settings for API request
		SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
				.signName("浴龙商城")
				.templateCode("SMS_276516612")
			//	.templateCode("SMS_276516812")
		//		.templateParam("{\"code\":\"6678\"}")
				.templateParam(JSON.toJSONString(codeMap))
				.phoneNumbers("15915931924")
				// Request-level configuration rewrite, can set Http request parameters, etc.
				// .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
				.build();

		// Asynchronously get the return value of the API request
		CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
		// Synchronously get the return value of the API request
		SendSmsResponse resp = response.get();
		System.out.println(new Gson().toJson(resp));

		//处理返回数据

		String resCode=resp.getBody().getCode();
		String message=resp.getBody().getMessage();
		if("OK".equalsIgnoreCase(resCode)){
              //成功
			System.out.println("成功");
		}else{
			//失败
			System.out.println(message);
		}


//		{
//			"headers": {
//			"Access-Control-Allow-Origin": "*",
//					"x-acs-request-id": "B4C15116-96B7-5C37-952A-531B88600263",
//					"Connection": "keep-alive",
//					"Content-Length": "110",
//					"Date": "Sat, 22 Apr 2023 07:31:28 GMT",
//					"Content-Type": "application/json;charset\u003dutf-8",
//					"x-acs-trace-id": "0bc9dc998dde33ccdea1942a0a42e821"
//		},
//			"body": {
//			"bizId": "749208282148688422^0",
//					"code": "OK",
//					"message": "OK",
//					"requestId": "B4C15116-96B7-5C37-952A-531B88600263"
//		}
//		}


//		CompletableFuture<Integer> resFuture = response.thenAcceptAsync(resp -> {
//			System.out.println(new Gson().toJson(resp));
//		}).handleAsync((success, throwable) -> {
//			//BiFunction<? super T, Throwable, ? extends U> fn
//            if(success!=null){
//				System.out.println(success.toString());
//			}
//			if(throwable!=null){
//				System.out.println(throwable.getMessage());
//			}
//			return 1;
//		});
		// Synchronously get the return value of the API request
		// Asynchronous processing of return values
//        response.thenAccept(resp -> {
//            System.out.println(new Gson().toJson(resp));
//        }).exceptionally(throwable -> { // Handling exceptions
//            System.out.println(throwable.getMessage());
//            return null;
//        });

		// Finally, close the client
		client.close();
	}
}
