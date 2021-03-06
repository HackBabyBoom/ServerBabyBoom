package com.example.demo.src.wooriOpenAPI;

import com.example.demo.src.externalOpenAPI.GiroController;
import com.example.demo.src.externalOpenAPI.OpenBankingController;
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
import java.text.DecimalFormat;

@RestController
@RequestMapping("/main")
public class MainPageController { // 앱의 메인화면에서 사용되는 Controller

    UserDao userDao = new UserDao();
    OpenBankingController openBankingController = new OpenBankingController();
    UtilitybillPageController utilitybillPageController = new UtilitybillPageController();
    GiroController giroController = new GiroController();

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
    @GetMapping("")
    public JSONObject main() throws ParseException {

        JSONArray allWooriAccountInfo = getAllWooriAccountInfo();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accounts",allWooriAccountInfo);
        String sumOfAllAccountWithdrawal = openBankingController.getSumOfAllAccountWithdrawal();
        jsonObject.put("month_total_transaction",sumOfAllAccountWithdrawal);

        String sumOfUtilitybill = utilitybillPageController.getSumOfAllUtilitybill();
        jsonObject.put("sumOfUtilitybill",sumOfUtilitybill);

        return jsonObject;
    }


    @ResponseBody
    @GetMapping("/getAllWooriAccountInfo")
    public JSONArray getAllWooriAccountInfo() throws ParseException { // 우리은행 전체 계좌 정보 조회

        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/finance/getIndivAllAccInfo";
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
                "  \"dataBody\": {}\n" +
                "}";

        String response = goConnection(apiURL,parameters);
        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(String.valueOf(response));
        JSONArray accountArray = (JSONArray)((JSONObject)jsonObj.get("dataBody")).get("GRID");

        for(int index=0; index<accountArray.size(); index++){
            ((JSONObject)accountArray.get(index)).remove("NEW_DT");
            ((JSONObject)accountArray.get(index)).remove("ADNT_RGS_YN");
            ((JSONObject)accountArray.get(index)).remove("ACT_STCD");
            ((JSONObject)accountArray.get(index)).remove("FAXC_BAL");
            ((JSONObject)accountArray.get(index)).remove("ACCT_KND");
            ((JSONObject)accountArray.get(index)).remove("CUCD");
            ((JSONObject)accountArray.get(index)).remove("KRW");
            ((JSONObject)accountArray.get(index)).remove("XPR_DT");
            ((JSONObject)accountArray.get(index)).remove("PSKL_ACT_YN");
            String balance = (String) ((JSONObject)accountArray.get(index)).get("PBOK_BAL");
            balance = balance.split("\\.")[0];
            balance = String.format("%,d", Integer.parseInt(balance));
            ((JSONObject)accountArray.get(index)).replace("PBOK_BAL",balance);
        }
        return accountArray;
    }


    @ResponseBody
    @GetMapping("/getWooriBalance")
    public String getWooriBalance() throws ParseException { // 우리은행 계좌 잔액

        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/finance/getAccBasicInfo";
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

        String response = goConnection(apiURL,parameters);
        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(String.valueOf(response));
        String balance = (String)((JSONObject)jsonObj.get("dataBody")).get("CT_BAL");
        System.out.println(balance);
        balance = balance.split("\\.")[0];
        balance = String.format("%,d", Integer.parseInt(balance));
        System.out.println("우리은행 계좌 잔액 : " + balance);
        return balance;

    }


    @ResponseBody
    @GetMapping("/getWooriAccountTransaction")
    public JSONArray getWooriAccountTransaction() throws ParseException { // 우리은행 계좌 거래내역
        String apiURL = "https://openapi.wooribank.com:444/oai/wb/v1/finance/getAccTransList";
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
                "    \"INQ_STA_DT\": \"20210101\",\n" +
                "    \"INQ_END_DT\": \"20210310\",\n" +
                "    \"NEW_DT\": \"20140522\",\n" +
                "    \"ACCT_KND\": \"P\",\n" +
                "    \"CUCD\": \"KRW\"\n" +
                "  }\n" +
                "}";

        String response = goConnection(apiURL,parameters);
        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(response);
        JSONArray transactionListArray = (JSONArray)((JSONObject)jsonObj.get("dataBody")).get("GRID");
        System.out.println("거래내역 : " + transactionListArray);
        return transactionListArray;
    }

}
