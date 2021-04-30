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
@RequestMapping("/transaction")
public class TransactionPageController {

    OpenBankingController openBankingController = new OpenBankingController();
    MainPageController mainPageController = new MainPageController();

    @ResponseBody
    @GetMapping("")
    public JSONObject transfer() throws ParseException {

        JSONObject jsonObject = new JSONObject();
        String sumOfAllAccountWithdrawal = openBankingController.getSumOfAllAccountWithdrawal();
        jsonObject.put("month_total_comsumption",sumOfAllAccountWithdrawal);
        String sumOfAllAccountDeposit = openBankingController.getSumOfAllAccountDeposit();
        jsonObject.put("month_total_gain",sumOfAllAccountDeposit);
        JSONArray allAccountWithdrawalAndPercent = openBankingController.getAllAccountWithdrawalAndPercent();
        jsonObject.put("bank_consumptions",allAccountWithdrawalAndPercent);

        return jsonObject;
    }
}
