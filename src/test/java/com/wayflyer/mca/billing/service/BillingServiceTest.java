package com.wayflyer.mca.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.business.Billing;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Customer;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.repo.BillingStorage;
import com.wayflyer.mca.billing.utils.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BillingServiceTest {

    @LocalServerPort
    int randomServerPort;

    BillingService service;
    BillingStorage storage= new BillingStorage();
    Billing billing = new Billing(storage);

    private RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setUp() {
        service = new BillingService(restTemplate,storage,billing);
    }

    @Test
    void getCustomerAdvances() throws JsonProcessingException {

        Advance ad1 = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        Advance ad2 = new Advance(3, 2, "2022-01-10", "300000.00", "14000.00", 1, "2022-01-10", 17);

        List<Advance> advances = Arrays.asList(ad1, ad2);
        Customer customer = new Customer();
        customer.setAdvances(advances);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Today", DateUtil.today().toString());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<Customer> response = new ResponseEntity(customer, HttpStatus.OK);

        List<Advance> resp= service.getCustomerAdvances();
        assertNotNull(resp);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(true, response.getBody().getAdvances().size()==2);
        assertEquals(resp.get(0).getFee(),response.getBody().getAdvances().get(0).getFee());

    }


    @Test
    void getRevenueForCustomer() throws JsonProcessingException {
        Advance ad1 = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);

        Revenue rev = new Revenue();
        rev.setAmount("100");
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Today", DateUtil.today().toString());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<Revenue> response = new ResponseEntity(rev, HttpStatus.OK);


       Revenue resp=  service.getRevenueForCustomer(ad1.getCustomer_id(),ad1.getRepayment_start_date());
        assertNotNull(resp);
        assertEquals(true, response.getBody().getAmount().equals("100"));
    }

    @Test
    void issueCharges() throws JsonProcessingException {
        Advance ad1 = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Today", DateUtil.today().toString());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<Advance> response = new ResponseEntity(ad1, HttpStatus.OK);

       ResponseEntity<String> resp= service.issueCharges(ad1.getCustomer_id(),"50000.00");
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(true, response.getBody().getTotal_advanced().equals("60000.00"));
    }

    @Test
    void issueBillingComplete() {
        Advance ad1 = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Today", DateUtil.today().toString());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<String> response = service.issueBillingComplete(ad1.getCustomer_id());

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Accepted"));
    }
}