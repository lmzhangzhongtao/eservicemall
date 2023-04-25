package com.caspar.eservicemall.thirdparty.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.caspar.eservicemall.thirdparty.service.SmsService;
import com.fasterxml.jackson.annotation.JsonInclude;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Data
@Slf4j
public class SmsServiceImpl implements SmsService {

    private String ak;

    private String secret;
//
//    private String token;
    private String endpoint;


    private String signName;
    @Override
    public Boolean sendCode(String phone, String code,String templateCode) throws ExecutionException, InterruptedException {
        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(ak)
                .accessKeySecret(secret)
                //.securityToken("<your-token>") // use STS token
                .build());

        // Configure the Client
        AsyncClient client = AsyncClient.builder()
                .region("cn-shenzhen") // Region ID
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(endpoint)
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();
        Map<String,String> codeMap=new HashMap<String,String>();
        codeMap.put("code",code);
        // Parameter settings for API request
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .signName(signName)
                .templateCode(templateCode)
                .templateParam(JSON.toJSONString(codeMap))
                .phoneNumbers(phone)
                // Request-level configuration rewrite, can set Http request parameters, etc.
                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                .build();
        // Asynchronously get the return value of the API request
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        // Synchronously get the return value of the API request
        SendSmsResponse resp = response.get();
        String resCode=resp.getBody().getCode();
        String message=resp.getBody().getMessage();
        if("OK".equalsIgnoreCase(resCode)){
            //成功
            log.info("发送短信成功");
            log.info("成功返回报文:"+new Gson().toJson(resp));
        }else{
            //失败
            log.error("发送短信失败，失败原因为:"+message);
            return false;
        }
        // Finally, close the client
        client.close();
        return true;
    }
}
