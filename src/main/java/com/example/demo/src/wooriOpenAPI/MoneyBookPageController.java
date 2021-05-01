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
@RequestMapping("/moneybook")
public class MoneyBookPageController {

    OpenBankingController openBankingController = new OpenBankingController();

    @ResponseBody
    @GetMapping("")
    public JSONObject moneybook() throws ParseException { // default 는 4월 30일 기준

        String date = "20210430";
        JSONObject jsonObject = new JSONObject();
        JSONArray allmoneybookList = openBankingController.getList();
        jsonObject.put("moneybookList",allmoneybookList);
        JSONArray totalPaymentAndListByDay = openBankingController.getTotalPaymentAndListByDay();
        jsonObject.put("totalPaymentList",totalPaymentAndListByDay);

        return jsonObject;
    }

}
