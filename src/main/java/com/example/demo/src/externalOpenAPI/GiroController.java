package com.example.demo.src.externalOpenAPI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class GiroController { // 일단 코드 작성하던 도중 보류

    private String token = "5ee315ef5aa3ed99a602af7c52eeaf27be8e45dcc77fce8e54365ec0e4fa5d2e";
//    private String header = "Bearer " + token;
    private String Iscd = "000921";

    private static final Logger logger = LogManager.getLogger(GiroController.class.getName());

    @ResponseBody
    @GetMapping("/getSewageFarePayment")
    public void getSewageFarePayment(){ // 상하수도 납부금액 조회

        String ApiNm = "InquireSewageFarePaymentHistory";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String Tsymd = simpleDateFormat.format(new Date());
        String Trtm = "112428";
        String FintechApsno = "001";
        String ApiSvcCd = "13E_002_00";
        int Min = 1111;
        int Max = 9999;
        String IsTuno = Integer.toString(Min + (int)(Math.random() * ((Max - Min))));

        String ElecPayNo = "2632001709000428753";
        String PageNo = "1";
        String Insymd = "20191108";
        String Ineymd = "20191108";

        String apiURL = "https://developers.nonghyup.com/InquireSewageFarePaymentHistory.nh";

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json charset=utf-8");
            con.setDoOutput(true);
//            String parameters = "{'Authorization': token, 'Iscd': Iscd , 'ApiNm': ApiNm, " +
//                    "'Tsymd': Tsymd , 'Trtm': Trtm, 'FintechApsno': FintechApsno , 'ApiSvcCd': ApiSvcCd, " +
//                    "'IsTuno': IsTuno , 'ElecPayNo': ElecPayNo, 'PageNo': PageNo, 'Insymd': Insymd, 'Ineymd': Ineymd }";

//            String parameters = "AccessToken="+token+"&Iscd="+Iscd+"&ApiNm="+ApiNm+"&Tsymd="+Tsymd+
//                    "&Trtm="+Trtm+"&FintechApsno="+FintechApsno+"&ApiSvcCd="+ApiSvcCd+"&IsTuno="+ IsTuno+
//                    "&ElecPayNo="+ElecPayNo+"&PageNo="+PageNo+"&Insymd="+Insymd+"&Ineymd="+Ineymd;

//            String parameters = "ElecPayNo="+ElecPayNo+"&PageNo="+PageNo+"&Insymd="+Insymd+"&Ineymd="+Ineymd;
            String parameters = "{'ElecPayNo': ElecPayNo, 'PageNo': PageNo, 'Insymd': Insymd, 'Ineymd': Ineymd }";


            con.setRequestProperty("AccessToken", token);
            con.setRequestProperty("Iscd", Iscd);
            con.setRequestProperty("ApiNm", ApiNm);
            con.setRequestProperty("Tsymd", Tsymd);
            con.setRequestProperty("Trtm", Trtm);
            con.setRequestProperty("FintechApsno", FintechApsno);
            con.setRequestProperty("ApiSvcCd", ApiSvcCd);
            con.setRequestProperty("IsTuno", IsTuno);
//            con.setRequestProperty("ElecPayNo", ElecPayNo);
//            con.setRequestProperty("PageNo", PageNo);
//            con.setRequestProperty("Insymd", Insymd);
//            con.setRequestProperty("Ineymd", Ineymd);

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
        } catch (Exception e) {
            System.out.println(e);
        }




    }


}
