package com.example.demo.src.openBanking;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/open")
public class OpenBankingController { // 가계부 기능의 모든은행의 계좌조회 관련 Class ( 금융결제원 Open API 사용 )

    private String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIxMTAwNzcyMDgyIiwic2NvcGUiOlsiaW5xdWlyeSIsImxvZ2luIiwidHJhbnNmZXIiXSwiaXNzIjoiaHR0cHM6Ly93d3cub3BlbmJhbmtpbmcub3Iua3IiLCJleHAiOjE2MjcwNTUxODUsImp0aSI6IjkyZWU3NzZjLTIyMDUtNGIxMS1hNTJkLTJiY2Y3MmVjOTY3OSJ9.8vBKGflc8RtuqSAEIaP2DynZG4RabYYywGHVedGxfqg";
    private String user_seq_no = "1100772082";

    @ResponseBody
    @GetMapping("/user")
    public void userInfo(){
        String header = "Bearer " + token;

        try {
            String apiURL = "https://developers.kftc.or.kr/proxy/user/me";
            apiURL = apiURL + "?user_seq_no=" + user_seq_no;
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
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}