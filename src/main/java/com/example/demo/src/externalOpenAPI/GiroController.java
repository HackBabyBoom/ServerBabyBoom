package com.example.demo.src.externalOpenAPI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.*;

@RestController
@RequestMapping("/giro")
public class GiroController { // 농협 API 이용

    private String token = "5ee315ef5aa3ed99a602af7c52eeaf27be8e45dcc77fce8e54365ec0e4fa5d2e";
    private String Iscd = "000921";

    private static final Logger logger = LogManager.getLogger(GiroController.class.getName());

    public String goConnection(String apiURL, String parameters){
        try {
            System.out.println(apiURL);
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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
    @GetMapping("/getSewageFarePayment")
    public JSONObject getSewageFarePayment() throws ParseException { // 상하수도 납부금액 조회

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String Tsymd = simpleDateFormat.format(new Date());
        int Min = 1111;
        int Max = 9999;
        String IsTuno = Integer.toString(Min + (int)(Math.random() * ((Max - Min))));

        String apiURL = "https://developers.nonghyup.com/InquireSewageFarePaymentHistory.nh";

        String parameters = "{\n" +
                "  \"Header\": {\n" +
                "    \"ApiNm\": \"InquireSewageFarePaymentHistory\",\n" +
                "    \"Tsymd\": \""+Tsymd+"\",\n" +
                "    \"Trtm\": \"112428\",\n" +
                "    \"Iscd\": \""+Iscd+"\",\n" +
                "    \"FintechApsno\": \"001\",\n" +
                "    \"ApiSvcCd\": \"13E_002_00\",\n" +
                "    \"IsTuno\": \""+IsTuno+"\",\n" +
                "    \"AccessToken\": \""+token+"\"\n" +
                "  },\n" +
                "  \"ElecPayNo\": \"2632001709000428753\",\n" +
                "  \"PageNo\": \"1\",\n" +
                "  \"Insymd\": \"20191108\",\n" +
                "  \"Ineymd\": \"20191108\"\n" +
                "}";

        String response = goConnection(apiURL,parameters);

        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(response);
        JSONArray jsonArray  = (JSONArray)jsonObj.get("Rec");
        System.out.println(jsonArray);
        int sumOfSewageFarePayment = 0;
        for(Object ob : jsonArray) {
            JSONObject tempObj = (JSONObject) ob;
            sumOfSewageFarePayment += Integer.parseInt((String) tempObj.get("PmntAmt"));
        }

        JSONObject SewageFarePayment = new JSONObject();
        SewageFarePayment.put("kindOfUtilitybill","상하수도요금");
        SewageFarePayment.put("PmnAmt",String.format("%,d", sumOfSewageFarePayment));

        return SewageFarePayment;
    }


    @ResponseBody
    @GetMapping("/getElectricityFarePayment")
    public JSONObject getElectricityFarePayment() throws ParseException { // 전기요금 납부금액 조회

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String Tsymd = simpleDateFormat.format(new Date());
        int Min = 1111;
        int Max = 9999;
        String IsTuno = Integer.toString(Min + (int)(Math.random() * ((Max - Min))));

        String apiURL = "https://developers.nonghyup.com/InquireElectricityFarePaymentHistory.nh";

        String parameters = "{\n" +
                "  \"Header\": {\n" +
                "    \"ApiNm\": \"InquireElectricityFarePaymentHistory\",\n" +
                "    \"Tsymd\": \""+Tsymd+"\",\n" +
                "    \"Trtm\": \"112428\",\n" +
                "    \"Iscd\": \""+Iscd+"\",\n" +
                "    \"FintechApsno\": \"001\",\n" +
                "    \"ApiSvcCd\": \"13E_001_00\",\n" +
                "    \"IsTuno\": \""+IsTuno+"\",\n" +
                "    \"AccessToken\": \""+token+"\"\n" +
                "  },\n" +
                "  \"ElecPayNo\": \"0606628088\",\n" +
                "  \"PageNo\": \"1\",\n" +
                "  \"Insymd\": \"20191010\",\n" +
                "  \"Ineymd\": \"20191010\"\n" +
                "}";

        String response = goConnection(apiURL,parameters);

        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(response);
        JSONArray jsonArray  = (JSONArray)jsonObj.get("Rec");
        System.out.println(jsonArray);

        int ElectricityFarePayment = Integer.parseInt((String) ((JSONObject)jsonArray.get(0)).get("PmntAmt "));

        JSONObject ElectricityFare = new JSONObject();
        ElectricityFare.put("kindOfUtilitybill","전기요금");
        ElectricityFare.put("PmnAmt",String.format("%,d", ElectricityFarePayment));

        return ElectricityFare;
    }


}
