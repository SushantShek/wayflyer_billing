package com.wayflyer.mca.billing.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Customer;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.repo.BillingStorage;
import com.wayflyer.mca.billing.service.BillingService;
import com.wayflyer.mca.billing.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Billing {

//    @Autowired
    BillingStorage storage;

    BigDecimal deductionThreshold = new BigDecimal(10000);
    BigDecimal zero = new BigDecimal(0);

    public Billing(BillingStorage storage){
        this.storage = storage;
    }
    public boolean doBilling(Customer customer, BillingService service) throws JsonProcessingException {
//        this.service = serv;
        for (Advance rev : customer.getAdvances()) {

            if (validateTransaction(rev)) {
                Revenue revenue = service.getRevenueForCustomer(rev.getCustomer_id(), DateUtil.backDate(2).toString());
                BigDecimal toDeduct = getDaysSalesChargeableAmount(revenue.getAmount(), rev.getRepayment_percentage());
                if (toDeduct.compareTo(deductionThreshold) > 0) {
                    toDeduct = deductionThreshold;
                }
                ResponseEntity<String> response = service.issueCharges(rev.getMandate_id(), String.valueOf(toDeduct));

                if (response.getStatusCode().is2xxSuccessful()) {
                    float carry = calculateDeductions(rev, toDeduct);
//                    rev.setTotal_advanced(String.valueOf(new BigDecimal(rev.getTotal_advanced()).subtract(toDeduct)));
//                  TODO: Add logic for deduction from map  service & Update remote
                    if (carry > 0.0) {
                        // TODO - Call Billing complete
                        ResponseEntity<String> closed = service.issueBillingComplete(rev.getId());
                        if(!closed.getStatusCode().is2xxSuccessful()){
                            System.out.println("Log Billing complete update failed ");
                        }
                    }
                    storage.updateAdvance(rev);
                    return true;
                } else {
                    // TODO : ADD TO RETRY
                    return false;
                }
            }
        }
        return false;
    }

    private BigDecimal getDaysSalesChargeableAmount(String sales, int percent) {
        var sale = new BigDecimal(sales);
        var rate = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100));
        return rate.multiply(sale);
    }

    private boolean validateTransaction(Advance advance) {
        var totalAdvance = new BigDecimal(advance.getTotal_advanced());
        var serviceFee = new BigDecimal(advance.getFee());
        return totalAdvance.compareTo(zero) > 0 || serviceFee.compareTo(zero) > 0;
    }

    private float calculateDeductions(Advance adv, BigDecimal chargeForTheDay) {
        var totalAdvance = new BigDecimal(adv.getTotal_advanced());
        var serviceFee = new BigDecimal(adv.getFee());
        if (totalAdvance.compareTo(chargeForTheDay) > 0) {
            adv.setTotal_advanced(totalAdvance.subtract(chargeForTheDay).toString());
            //TODO - Map set totalAdvance to totalAdv - dayChargeable
        } else {
            //make total Advance ==0
            chargeForTheDay = chargeForTheDay.subtract(totalAdvance);
            adv.setTotal_advanced(zero.toString());
            if (serviceFee.compareTo(chargeForTheDay) > 0) {
                adv.setFee(serviceFee.subtract(chargeForTheDay).toString());
            } else {
                chargeForTheDay = chargeForTheDay.subtract(serviceFee);
                adv.setFee(zero.toString());

                // TODO - UPDATE the Service call with Actual amount charged
                return chargeForTheDay.floatValue();
            }
            // Check for the fees value and deduct from there
        }
        return 0;
    }
}
