package com.example.batch.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {

	private final Environment env;
	
	@Autowired
    public PhoneService(Environment env) {
        this.env = env;
    }
	
	public String sendMessage(String error) {
		String hostNameUrl ="https://sens.apigw.ntruss.com";
        String requestUrl = "/sms/v2/services/";
        String requestUrlType = "/messages";
        String accessKey = env.getProperty("phone.accessKey");
        String serviceId = env.getProperty("phone.serviceId");
        String method = "POST";
        String timestamp = Long.toString(System.currentTimeMillis());
        requestUrl += serviceId + requestUrlType;
        String apiUrl = hostNameUrl + requestUrl;

        // JSON을 활용한 body data 생성
        JSONObject bodyJson = new JSONObject();
        JSONObject toJson = new JSONObject();
        JSONArray toArr = new JSONArray();

        toJson.put("subject", "");
        toJson.put("content", "[ BATCH ERROR OCCURED ]\n" + error +"]");
        toJson.put("to", env.getProperty("phone.from").replaceAll("-", ""));
        toArr.put(toJson);

        bodyJson.put("type", "sms");
        bodyJson.put("contentType", "COMM");
        bodyJson.put("countryCode", "82");
        bodyJson.put("from", env.getProperty("phone.from").replaceAll("-", ""));
        bodyJson.put("subject", "");
        bodyJson.put("content", "BATCH ERROR");
        bodyJson.put("messages", toArr);

        String body = bodyJson.toString();
        System.out.println(body);

        try {
            URL url = new URL(apiUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("content-type", "application/json");
            con.setRequestProperty("x-ncp-apigw-timestamp", timestamp);
            con.setRequestProperty("x-ncp-iam-access-key", accessKey);
            con.setRequestProperty("x-ncp-apigw-signature-v2", makeSignature(timestamp));
            con.setRequestMethod(method);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

            wr.write(body.getBytes());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            System.out.println("responseCode" + " " + responseCode);
            if(responseCode == 202) {
                //정상호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            else {
                //에러발생
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            return error;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public String makeSignature(String timestamp) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        String space = " "; // one space
        String newLine = "\n"; // new line
        String method = "POST"; // method String
        String url = "/sms/v2/services/" + env.getProperty("phone.serviceId") + "/messages"; // url (include query string)
        String accessKey = env.getProperty("phone.accessKey"); // access key id (from portal or Sub Account)
        String secretKey = env.getProperty("phone.secretKey");
        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();
        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), env.getProperty("phone.encodingMethod"));
        Mac mac = Mac.getInstance(env.getProperty("phone.encodingMethod"));
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);
        return encodeBase64String;
    }
}
