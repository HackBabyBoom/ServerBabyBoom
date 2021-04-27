package com.example.demo.src.wooriOpenAPI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

@RestController
@RequestMapping("/main")
public class MainPageController {

    UserDao userDao = new UserDao();

    public static String toNumFormat(int num) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(num);
    }

    @ResponseBody
    @GetMapping("/getWooriBalance")
    public String getWooriBalance() { // 우리은행 계좌 잔액

        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/finance/getAccBasicInfo";

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("appKey", userDao.getAppKey());
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json charset=utf-8");
            con.setDoOutput(true);
            String parameters = "{\n" +
                    "  \"dataHeader\": {\n" +
                    "    \"UTZPE_CNCT_IPAD\": \"\",\n" +
                    "    \"UTZPE_CNCT_MCHR_UNQ_ID\": \"\",\n" +
                    "    \"UTZPE_CNCT_TEL_NO_TXT\": \"\",\n" +
                    "    \"UTZPE_CNCT_MCHR_IDF_SRNO\": \"\",\n" +
                    "    \"UTZ_MCHR_OS_DSCD\": \"\",\n" +
                    "    \"UTZ_MCHR_OS_VER_NM\": \"\",\n" +
                    "    \"UTZ_MCHR_MDL_NM\": \"\",\n" +
                    "    \"UTZ_MCHR_APP_VER_NM\": \"\"\n" +
                    "  },\n" +
                    "  \"dataBody\": {\n" +
                    "    \"INQ_ACNO\": \"1002123456789\",\n" +
                    "    \"INQ_BAS_DT\": \"20210220\",\n" +
                    "    \"ACCT_KND\": \"P\",\n" +
                    "    \"INQ_CUCD\": \"KRW\"\n" +
                    "  }\n" +
                    "}";

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());

            JSONParser jsonPar = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonPar.parse(String.valueOf(response));
            String balance = (String)((JSONObject)jsonObj.get("dataBody")).get("CT_BAL");
            System.out.println(balance);
            balance = balance.split("\\.")[0];
            balance = String.format("%,d", Integer.parseInt(balance));
            System.out.println("우리은행 계좌 잔액 : " + balance);
            return balance;

        } catch (Exception e) {
            System.out.println(e);
            return "error";
        }

    }

}
