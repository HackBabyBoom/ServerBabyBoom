package com.example.demo.src.externalOpenAPI;

//import org.apache.tomcat.util.json.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@RestController
@RequestMapping("/open")
public class OpenBankingController { // 가계부 기능 - 모든은행의 계좌조회 관련 Class ( 금융결제원 Open API 사용 )

    private String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzcyMDgyIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MjcwNTUxODUsImp0aSI6IjkyZWU3NzZjLTIyMDUtNGIxMS1hNTJkLTJiY2Y3MmVjOTY3OSJ9.8vBKGflc8RtuqSAEIaP2DynZG4RabYYywGHVedGxfqg";
    private String header = "Bearer " + token;
    private String user_seq_no = "1100772082";

    private static final Logger logger = LogManager.getLogger(OpenBankingController.class.getName());

    public String goConnection(String apiURL){
        try {
            System.out.println(apiURL);
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", header);
            con.setRequestProperty("user_seq_no", user_seq_no);
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
    @GetMapping("/user")
    public void getUserInfo(){ // 사용자 정보

        String apiURL = "https://developers.kftc.or.kr/proxy/user/me";
        apiURL = apiURL + "?user_seq_no=" + user_seq_no;

        String result = goConnection(apiURL);
        logger.info(result);

    }

    @ResponseBody
    @GetMapping("/getAllAccountList") // 사용자의 전체 계좌 정보
    public JSONArray getAllAccountList() throws ParseException {

        String apiURL = "https://developers.kftc.or.kr/proxy/account/list";
        apiURL = apiURL + "?user_seq_no=" + user_seq_no +"&include_cancel_yn=N&sort_order=D";
        String result = goConnection(apiURL);
        logger.info(result);

        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(result);
        JSONArray jsonArray  = (JSONArray)jsonObj.get("res_list");
        JSONArray accountTransactionList = new JSONArray();

        for(Object ob : jsonArray){
            JSONObject tempObj = (JSONObject)ob;
            Map<String,String> map = new HashMap<>();
            String fintechUseNum = (String) tempObj.get("fintech_use_num");
            String bankCodeStd = (String) tempObj.get("bank_code_std");
            String bankName = (String) tempObj.get("bank_name");
            map.put("fintech_use_num",fintechUseNum);
            map.put("bank_code_std",bankCodeStd);
            map.put("bank_name",bankName);
            accountTransactionList.add(new JSONObject(map));
        }

        System.out.println(accountTransactionList);
        return accountTransactionList;

    }

    @ResponseBody
    @GetMapping("/getAllAccountTransactionList")
    public void getAllAccountTransactionList() throws ParseException {

        int Min = 111111111;
        int Max = 999999999;

        String from_date = "20210401"; // 사용자가 UI를 통해 입력한 변수를 넣을 예정
        String to_date = "20210426"; // 일단은 TEST를 위해 넣어둠

        String [] befor_inquiry_trace_info = {"333","222","123","111"};

        JSONArray allAccountList = getAllAccountList();
        String [] fintechNums = new String[allAccountList.size()];
        JSONArray [] allAccountTransactionLists  = new JSONArray[allAccountList.size()];

        for(int index = 0; index < allAccountList.size(); index++){
            JSONObject jsonObject = (JSONObject) allAccountList.get(index);
            String fintechUseNum = (String) jsonObject.get("fintech_use_num");
            fintechNums[index] = fintechUseNum;
        }

        for(int index = 0; index<fintechNums.length; index++){

            String random = Integer.toString(Min + (int)(Math.random() * ((Max - Min))));
            String bank_tran_id = "M202112088"+"U"+ random; // 이용기관코드 -> 뒤에 9자리 난수생성으로 수정

            String apiURL = "https://testapi.openbanking.or.kr/v2.0/account/transaction_list/fin_num";
            apiURL = apiURL + "?bank_tran_id=" + bank_tran_id +"&fintech_use_num="+fintechNums[index] +"&inquiry_type=A&inquiry_base=D&from_date="+from_date+"&to_date="+to_date+"&sort_order=D&tran_dtime=20201001150133&befor_inquiry_trace_info="+befor_inquiry_trace_info[index];
            String result = goConnection(apiURL);
            logger.info(result);

            JSONParser jsonPar = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonPar.parse(result);

            JSONArray resListArray = (JSONArray)jsonObj.get("res_list");
            JSONArray accountTransactionList = new JSONArray(); // 데이터 분석 시 파이썬으로 넘겨줄 JSONArray

            System.out.println(resListArray);

            for(Object ob : resListArray){ // 사실 전체 json 넘겨주고, 파이썬에서 column만 뽑으면 훨신 수월함 -> 코드의 효율성을 보고 제거 유무 결정
                JSONObject tempObj = (JSONObject)ob;
                Map <String,String> map = new HashMap<>();
                String date = (String) tempObj.get("tran_date");
                String store = (String) tempObj.get("print_content");
                String amount = (String) tempObj.get("tran_amt");
                map.put("tran_date",date);
                map.put("print_content",store);
                map.put("tran_amt",amount);
                accountTransactionList.add(new JSONObject(map));
            }

            allAccountTransactionLists[index] = accountTransactionList;

        }

        for(JSONArray jsonObject : allAccountTransactionLists){
            System.out.println(jsonObject);
        }

    }


}