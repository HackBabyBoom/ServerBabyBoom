package com.example.demo.src.wooriOpenAPI;

import com.example.demo.src.externalOpenAPI.OpenBankingController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moneybookdetail")
public class MoneyBookDetailPageController {

    OpenBankingController openBankingController = new OpenBankingController();

    @ResponseBody
    @GetMapping("")
    public JSONObject moneybookdetail(String date) throws ParseException { // default 는 4월 30일 기준

//        date = "20210430";
        JSONObject jsonObject = new JSONObject();
        String totalPayment = openBankingController.getTotalPaymentByDay(date);
        jsonObject.put("totalPayment",totalPayment);
        JSONArray paymentList = openBankingController.getDepositAndWithdrawalListByDay(date);
        jsonObject.put("paymentList",paymentList);

        return jsonObject;
    }
}
