package com.wayflyer.mca.billing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Charges;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api")
public class BillingController {

    private BillingService service;

    public BillingController(BillingService service) {
        this.service = service;
    }

    @GetMapping("advances")
    public List<Advance> getAdvances() throws JsonProcessingException {
        return service.getCustomerAdvances();
    }

    @GetMapping("customers/{id}/revenues/{for_date}")
    public Revenue getRevenueForUser(@PathVariable int id, @PathVariable String for_date) throws JsonProcessingException {
        return service.getRevenueForCustomer(id, for_date);
    }

    @PostMapping("mandates/{id}/charge")
    public ResponseEntity<String> issueRepaymentCharge(@RequestBody Charges charge, @PathVariable int id) throws JsonProcessingException {
        return service.issueCharges(id, charge.getAmount());
    }

    @PostMapping("advances/{id}/billing_complete")
    public ResponseEntity<String> registerBillingComplete(@PathVariable int id) {
        ResponseEntity<String> complete = service.issueBillingComplete(id);
        if (complete.getStatusCode().is2xxSuccessful())
            return complete;

        return new ResponseEntity<>("Error while finalizing", HttpStatus.PAYMENT_REQUIRED);
    }

    @PostMapping("billing")
    public ResponseEntity<List<Advance>> startBillingForDates(@RequestBody List<String> dates) {
        // TODO - Verify Response
        ResponseEntity<List<Advance>> response = service.startBilling(dates);
        if (response.getStatusCode().is2xxSuccessful())
            return response;
        return new ResponseEntity<List<Advance>>(new ArrayList(), HttpStatus.PAYMENT_REQUIRED);
    }
}
