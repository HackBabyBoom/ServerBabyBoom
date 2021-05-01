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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@RestController
@RequestMapping("/open")
public class OpenBankingController { // 금융결제원 Open API 이용하는 모든은행의 계좌조회 및 오픈뱅킹 관련 Class

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
    @GetMapping("/getAllAccountList") // 모든 오픈뱅킹 계좌 정보
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
    @GetMapping("/getAllAccountWithdrawal")  // 각 오픈뱅킹 계좌 별 총 출금액 & 퍼센트
    public int [] getAllAccountWithdrawal() throws ParseException {
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        int [] allAcountWithdrawlList = new int [allAccountTransactionLists.length];

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("inout_type").equals("출금")){
                    allAcountWithdrawlList[accountIndex] += Integer.parseInt(String.valueOf(jsonOb.get("tran_amt")));
                }
            }
        }

        // 하나, 신한, 기업, 우리
        System.out.println(allAcountWithdrawlList[0] + " " + allAcountWithdrawlList[1] + " " + allAcountWithdrawlList[2] + " " + allAcountWithdrawlList[3]);
        return allAcountWithdrawlList;
    }


    @ResponseBody
    @GetMapping("/getAllAccountWithdrawalAndPercent")  // 각 오픈뱅킹 계좌 별 총 출금액 & 퍼센트
    public JSONArray getAllAccountWithdrawalAndPercent() throws ParseException {
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        String [] cardName = {"하나체크카드", "신한체크카드", "IBK기업체크카드","우리체크카드"};
        JSONArray allAccountWithdrawal = new JSONArray();
        int [] allAcountWithdrawallList = getAllAccountWithdrawal();
        String sumOfAllAccountWithdrawal= getSumOfAllAccountWithdrawal().replace(",","");

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            JSONObject jsonObject = new JSONObject();
            JSONObject cardObject = new JSONObject();
            cardObject.put("card_name",cardName[accountIndex]);
            cardObject.put("card_cunsumption",String.format("%,d", allAcountWithdrawallList[accountIndex] ));
            int percent = (int)( (double) allAcountWithdrawallList[accountIndex] / ((double)Integer.parseInt(sumOfAllAccountWithdrawal)) * 100.0);

            jsonObject.put("percent",percent);
            jsonObject.put("card",cardObject);
            allAccountWithdrawal.add(jsonObject);
        }

        return allAccountWithdrawal;
    }



    @ResponseBody
    @GetMapping("/getWooriWithdrawal")
    public String getWooriWithdrawal() throws ParseException {
        int [] allAcountWithdrawlList = getAllAccountWithdrawal();
        String WooriWithdrawal = String.format("%,d", allAcountWithdrawlList[3]);
        return WooriWithdrawal;
    }

    @ResponseBody
    @GetMapping("/getSumOfAllAccountWithdrawal") // 전체 오픈뱅킹 출금액
    public String getSumOfAllAccountWithdrawal() throws ParseException {
        int [] allAcountWithdrawallList = getAllAccountWithdrawal();
        int sum = 0;
        for(int withDrawl : allAcountWithdrawallList){
            sum += withDrawl;
        }
        String sumOfAllAccountWithdrawal = String.format("%,d", sum);
        return sumOfAllAccountWithdrawal;
    }

    @ResponseBody
    @GetMapping("/getSumOfAllAccountDeposit")  // 전체 오픈뱅킹 입금액
    public String getSumOfAllAccountDeposit() throws ParseException {
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        int sum = 0;

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("inout_type").equals("입금")){
                    sum += Integer.parseInt(String.valueOf(jsonOb.get("tran_amt")));
                }
            }
        }

        String sumOfAllAccountDeposit = String.format("%,d", sum);
        return sumOfAllAccountDeposit;
    }

    @ResponseBody
    @GetMapping("/getAllAccountTransactionList")
    public JSONArray[] getAllAccountTransactionList() throws ParseException { // 모든 오픈뱅킹 계좌의 거래내역

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
                String inout_type = (String) tempObj.get("inout_type");
                String store = (String) tempObj.get("print_content");
                String amount = (String) tempObj.get("tran_amt");
                map.put("tran_date",date);
                map.put("inout_type",inout_type);
                map.put("print_content",store);
                map.put("tran_amt",amount);
                accountTransactionList.add(new JSONObject(map));
            }
            allAccountTransactionLists[index] = accountTransactionList;
        }

        for(JSONArray jsonObject : allAccountTransactionLists){
            System.out.println(jsonObject);
        }
        return allAccountTransactionLists;

    }


    @ResponseBody
    @GetMapping("/getDepositList")  // 날짜 별 총 입금액 배열
    public JSONArray getDepositList() throws ParseException {
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        JSONArray depositList = new JSONArray();
        Map <String, Integer> map = new HashMap<>();

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;

                if(jsonOb.get("inout_type").equals("입금")){
                    if(map.containsKey(jsonOb.get("tran_date"))){
                        Integer deposit = map.get(jsonOb.get("tran_date")) + Integer.parseInt(String.valueOf(jsonOb.get("tran_amt")));
                        map.replace((String) jsonOb.get("tran_date"),deposit);
                    }else{
                        map.put((String) jsonOb.get("tran_date"),Integer.parseInt(String.valueOf(jsonOb.get("tran_amt"))));
                    }
                }
            }
        }

        String [] mapToStringArr = map.toString().replace("{","").replace("}","").split(",");
        Arrays.sort(mapToStringArr);
        for(String str : mapToStringArr){
            String [] strArr = str.split("=");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tran_date",strArr[0].replace(" ",""));
            jsonObject.put("tran_amt",Integer.parseInt(strArr[1]));
            depositList.add(jsonObject);
        }
        return depositList;
    }

//    연경이가짠거

    @ResponseBody
    @GetMapping("/getWithdrawalList")  // 날짜 별 총 출금액 배열
    public JSONArray getWithdrawalList() throws ParseException {
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        JSONArray withdrawalList = new JSONArray();
        Map <String, Integer> map = new HashMap<>();

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("inout_type").equals("출금")){
                    if( map.containsKey(jsonOb.get("tran_date"))){
                        Integer withdrawal = map.get(jsonOb.get("tran_date")) + Integer.parseInt(String.valueOf(jsonOb.get("tran_amt")));
                        map.replace((String) jsonOb.get("tran_date"),withdrawal);
                    }else{
                        map.put((String) jsonOb.get("tran_date"),Integer.parseInt(String.valueOf(jsonOb.get("tran_amt"))));
                    }
                }
            }
        }

        String [] mapToStringArr = map.toString().replace("{","").replace("}","").replace(" ","").split(",");
        Arrays.sort(mapToStringArr);
        for(String str : mapToStringArr){
            String [] strArr = str.split("=");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tran_date",strArr[0]);
            jsonObject.put("tran_amt",Integer.parseInt(strArr[1])*(-1));
            withdrawalList.add(jsonObject);
        }
        return withdrawalList;
    }


    @ResponseBody
    @GetMapping("/getList")  // 날짜 별 (입금-출금) 리스트
    public JSONArray getList() throws ParseException { // 적절한 함수명으로 추후 수정 예정

        JSONArray depositList = getDepositList();
        JSONArray withdrawalList = getWithdrawalList();
        JSONArray finalList = new JSONArray();

        for (Object withdrawal : withdrawalList) {
            finalList.add(withdrawal);
        }

        for (Object deposit : depositList) {
            JSONObject depositObject = (JSONObject) deposit;
            for (int index = 0; index < finalList.size(); index++) {
                JSONObject withdrawalObject = (JSONObject) finalList.get(index);
                String trandateOfWithdrawal = String.valueOf(withdrawalObject.get("tran_date"));
                String trandateOfDeposit = String.valueOf(depositObject.get("tran_date"));
                if (trandateOfWithdrawal.equals(trandateOfDeposit)) {
                    Integer money = (Integer) withdrawalObject.get("tran_amt") + (Integer) depositObject.get("tran_amt");
                    ((JSONObject) finalList.get(index)).replace("tran_amt",money);
                    break;
                }else if(trandateOfDeposit.compareTo(trandateOfWithdrawal) < 0 ){
                    finalList.add(index,depositObject);
                    break;
                }
            }
        }
        return finalList;
    }


    @ResponseBody
    @GetMapping("/getDepositListByDay")  // 날짜 별 입금 내역
    public JSONArray getDepositListByDay(String date) throws ParseException {
        date = "20210430"; // 테스트용
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        JSONArray depositListByDay = new JSONArray();

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("inout_type").equals("입금") && jsonOb.get("tran_date").equals(date)){
                    jsonOb.remove("inout_type");
                    jsonOb.remove("tran_date");
                    depositListByDay.add(jsonOb);
                }
            }
        }
        return depositListByDay;
    }

    @ResponseBody
    @GetMapping("/getWithdrawalListByDay")  // 날짜 별 출금 내역
    public JSONArray getWithdrawalListByDay(String date) throws ParseException {
        date = "20210430"; // 테스트용
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        JSONArray withdrawalListByDay = new JSONArray();

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("inout_type").equals("출금") && jsonOb.get("tran_date").equals(date)){
                    jsonOb.remove("inout_type");
                    jsonOb.remove("tran_date");
                    withdrawalListByDay.add(jsonOb);
                }
            }
        }
        return withdrawalListByDay;
    }


    @ResponseBody
    @GetMapping("/getDepositAndWithdrawalListByDay")  // 날짜 별 입출금 내역
    public JSONArray getDepositAndWithdrawalListByDay(String date) throws ParseException {
        date = "20210430"; // 테스트용
        JSONArray[] allAccountTransactionLists = getAllAccountTransactionList();
        JSONArray depositAndWithdrawalListByDay = new JSONArray();

        for(int accountIndex=0; accountIndex< allAccountTransactionLists.length; accountIndex++){
            for(Object ob : allAccountTransactionLists[accountIndex]){
                JSONObject jsonOb = (JSONObject)ob;
                if(jsonOb.get("tran_date").equals(date)){
                    if(jsonOb.get("inout_type").equals("출금")){
                        int payment = Integer.parseInt((String) jsonOb.get("tran_amt"))*(-1);
                        jsonOb.replace("tran_amt",payment);
                    }
                    jsonOb.remove("inout_type");
                    jsonOb.remove("tran_date");
                    depositAndWithdrawalListByDay.add(jsonOb);
                }
            }
        }
        return depositAndWithdrawalListByDay;
    }


    @ResponseBody
    @GetMapping("/getTotalPaymentByDay")  // 날짜 별 총 입출금액
    public int getTotalPaymentByDay(String date) throws ParseException {
//        date = "20210430"; // 테스트용
        JSONArray depositAndWithdrawalListByDay = getDepositAndWithdrawalListByDay(date);
        int totalPayment = 0;

        for(Object ob : depositAndWithdrawalListByDay){
            JSONObject jsonOb = (JSONObject)ob;
            totalPayment += Integer.parseInt(String.valueOf(jsonOb.get("tran_amt")));
        }

        return totalPayment;
    }




    @ResponseBody
    @GetMapping("/getRankList") // flask에서 매장 랭크를 받아옴
    public JSONObject getRankList() throws  ParseException{

        JSONObject getRankList  = new JSONObject();

        String apiURL = "http://localhost:5000/getRank"; //flask 서버
        String result = goConnection(apiURL);
        logger.info(result);

        JSONParser jsonPar = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonPar.parse(result);
        JSONObject RankList = (JSONObject) jsonPar.parse("tran_amt");

        return RankList;

    }




}