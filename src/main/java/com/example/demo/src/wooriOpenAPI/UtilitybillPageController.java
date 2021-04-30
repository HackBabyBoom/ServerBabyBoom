package com.example.demo.src.wooriOpenAPI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

@RestController
@RequestMapping("/Utilitybill")
public class UtilitybillPageController { // 앱의 공과금 화면에서 사용되는 Class

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
    @GetMapping("/getWooriUtilitybillList")
    public JSONArray getWooriUtilitybillList() throws ParseException { // 우리은행 공과금 리스트

        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/tax/getPaymentList";
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
                "    \"PYM_DTPE_FNM\": \"홍길동\",\n" +
                "    \"ENCY_RBNO\": \"111111111111111\"\n" +
                "  }\n" +
                "}";

        String response = goConnection(apiURL,parameters);
        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(String.valueOf(response));
        JSONArray jsonArray = (JSONArray)((JSONObject)jsonObj.get("dataBody")).get("REPT_FA");
        JSONArray utilitybillList = new JSONArray();

        for(Object ob : jsonArray) {
            JSONObject tempObj = (JSONObject) ob;
            JSONObject paymentObject = new JSONObject();
            paymentObject.put("kindOfUtilitybill",tempObj.get("ITTX_NM"));
            int payment = Integer.parseInt((String) tempObj.get("PYM_AM"));
            paymentObject.put("PmnAmt",String.format("%,d", payment));
            utilitybillList.add(paymentObject);
        }

        return utilitybillList;

    }

    // 전체 총 공과금 return


}
