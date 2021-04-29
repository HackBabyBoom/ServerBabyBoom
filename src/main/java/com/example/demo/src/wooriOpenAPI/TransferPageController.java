package com.example.demo.src.wooriOpenAPI;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/transfer")
public class TransferPageController {

    UserDao userDao = new UserDao();

    public String goConnection(String apiURL, String parameters){
        try {
            System.out.println(apiURL);
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("appKey", userDao.getAppKey());
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json charset=utf-8");
            con.setDoOutput(true);

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
            return response.toString();
        } catch (Exception e) {
            System.out.println(e);
            return String.valueOf(e);
        }
    }

    @ResponseBody
    @GetMapping("/doAccountTransfer")
    public String doAccountTransfer() throws ParseException { // 당행 계좌이체

        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/trans/executeWooriAcctToWooriAcct";
        String parameters = "{\n" +
                "  \"dataHeader\": {\n" +
                "    \"UTZPE_CNCT_IPAD\": \"\",\n" +
                "    \"UTZPE_CNCT_MCHR_UNQ_ID\": \"\",\n" +
                "    \"UTZPE_CNCT_TEL_NO_TXT\": \"\",\n" +
                "    \"UTZPE_CNCT_MCHR_IDF_SRNO\": \"\",\n" +
                "    \"UTZ_MCHR_OS_DSCD\": \"\",\n" +
                "    \"UTZ_MCHR_OS_VER_NM\": \"\",\n" +
                "    \"UTZ_MCHR_MDL_NM\": \"\",\n" +
                "    \"UTZ_MCHR_APP_VER_NM\": \"\",\n" +
//                "    \"PARTNER_CODE\": \"8MOPN001\"\n" +
                "  },\n" +
                "  \"dataBody\": {\n" +
                "    \"WDR_ACNO\": \"1002123456789\",\n" +
                "    \"TRN_AM\": \"500000\",\n" +
                "    \"RCV_BKCD\": \"020\",\n" +
                "    \"RCV_ACNO\": \"1002987654321\",\n" +
                "    \"PTN_PBOK_PRNG_TXT\": \"보너스\"\n" +
                "  }\n" +
                "}";

        String response = goConnection(apiURL,parameters);
//        JSONParser jsonPar = new JSONParser();
//        JSONObject jsonObj = (JSONObject) jsonPar.parse(String.valueOf(response));
//        String balance = (String)((JSONObject)jsonObj.get("dataBody")).get("CT_BAL");
//        System.out.println(balance);
//        balance = balance.split("\\.")[0];
//        balance = String.format("%,d", Integer.parseInt(balance));
//        System.out.println("우리은행 계좌 잔액 : " + balance);
//        return balance;
        return response;

    }

    @ResponseBody
    @GetMapping ("/doAccountTransferTest")
    public String doAccountTransferTest() throws ParseException { // 당행 계좌이체

        try {

            URL url = new URL("https://openapi.wooribank.com:444/oai/wb/v1/trans/executeWooriAcctToWooriAcct");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            System.out.println(userDao.getAppKey());
            conn.setRequestProperty("appKey", userDao.getAppKey());
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json charset=utf-8");
            conn.setDoOutput(true);

//            conn.setRequestProperty("token", "53Sol4YO1x6SBvDjdSueKEqqjUzBMo4E");

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
                    "    \"WDR_ACNO\": \"1002123456789\",\n" +
                    "    \"TRN_AM\": \"500000\",\n" +
                    "    \"RCV_BKCD\": \"020\",\n" +
                    "    \"RCV_ACNO\": \"1002987654321\",\n" +
                    "    \"PTN_PBOK_PRNG_TXT\": \"보너스\"\n" +
                    "  }\n" +
                    "}";


            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            System.out.println(responseCode + " 응답코드 ");
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            return response.toString();

        } catch (Exception e) {
            System.out.println(e);
            return String.valueOf(e);
        }

    }



}
