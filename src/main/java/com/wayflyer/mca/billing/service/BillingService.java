package com.wayflyer.mca.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayflyer.mca.billing.business.Billing;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Customer;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.repo.BillingStorage;
import com.wayflyer.mca.billing.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class BillingService {

    private BillingStorage storage;
    private Billing billing;
    private RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();

    public BillingService() {
    }

    @Autowired
    public BillingService(RestTemplate restTemplate, BillingStorage storage, Billing billing) {
        this.restTemplate = restTemplate;
        this.storage = storage;
        this.billing = billing;
    }

    public List<Advance> getCustomerAdvances(String ... dates) throws JsonProcessingException {

        String localDate = dates.length > 0 ? dates[0] : DateUtil.today().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Today", localDate);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> response = restTemplate.exchange("https://billing.eng-test.wayflyer.com/advances",
                HttpMethod.GET, entity, String.class);
        Customer customer = om.readValue(response.getBody(), Customer.class);
        if(response.getStatusCode().is5xxServerError()){
            storage.addFailedDatesToRetry(localDate);
            throw new RuntimeException("Data for "+ localDate +" not found. Retry later");
        }

        log.info(customer.toString());
        storage.saveAdvance(customer.getAdvances());
        try {
             if(!billing.doBilling(customer, this)){
               return Collections.EMPTY_LIST;
            };
        }catch(Exception ex){
            customer.getAdvances().forEach( adv ->storage.addFailedBillingsToRetry(adv));
        }
        return customer.getAdvances();
    }


    public Revenue getRevenueForCustomer(int id, String date) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Today", DateUtil.today().toString());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<String> revenue = restTemplate.exchange(
                "https://billing.eng-test.wayflyer.com/customers/" + id + "/revenues/" + date, HttpMethod.GET, entity, String.class);
        if(revenue.getStatusCode().is5xxServerError()){
            throw new RuntimeException();
        }
        log.info(revenue.getBody());
        return om.readValue(revenue.getBody(), Revenue.class);
    }

    public ResponseEntity<String> issueCharges(int id, String amount) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Today", DateUtil.today().toString());

        HttpEntity<String> entity = new HttpEntity<>(om.writeValueAsString(Collections.singletonMap("amount", amount)), headers);
        ResponseEntity<String> charges = restTemplate.exchange("https://billing.eng-test.wayflyer.com/mandates/" + id + "/charge",
                HttpMethod.POST, entity, String.class);
        log.info(""+charges.getBody().toString());

        if(charges.getStatusCode().is5xxServerError()){
            throw new RuntimeException();
        }

        return charges;
    }

    public ResponseEntity<String> issueBillingComplete(int id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Today", DateUtil.today().toString());

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> isComplete = restTemplate.exchange(
                "https://billing.eng-test.wayflyer.com/advances/" + id + "/billing_complete", HttpMethod.POST, entity, String.class);
        if(isComplete.getStatusCode().is5xxServerError()){
            storage.addFailedCompletionsToRetry(id);
        }
        log.info(isComplete.toString());
        return isComplete;
    }

    public ResponseEntity<List<Advance>> startBilling(List<String> dates) {

        List<Advance> advance = new ArrayList<>();
        for (String date : dates)
            try {
                advance =  getCustomerAdvances(date);
            } catch (JsonProcessingException e) {
                storage.addFailedDatesToRetry(date);
                e.printStackTrace();
            }
        return (ResponseEntity<List<Advance>>) advance;
    }

}
