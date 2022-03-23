package com.wayflyer.mca.billing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Charges;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class BillingControllerTest {

    @Mock
    BillingService service;

    @InjectMocks
    BillingController controller;
    private WebTestClient client;

    private List<Advance> advanceList;

    @BeforeEach
    void setUp() {
        Advance adv = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        advanceList = List.of(adv);
    }

    @Test
    void getAdvances() throws JsonProcessingException {
        when(service.getCustomerAdvances()).thenReturn(advanceList);
        List<Advance> response = controller.getAdvances();
        assertEquals(1, response.size());
    }

    @Test
    void getRevenueForUser() throws JsonProcessingException {
        Revenue rev = new Revenue();
        rev.setAmount("100");
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(rev);
        Revenue response = controller.getRevenueForUser(1, "2022-03-19");

        assertEquals("100", response.getAmount());
    }

    @Test
    void issueRepaymentCharge() throws JsonProcessingException {
        Charges charges = new Charges();
        charges.setAmount("60000.00");

        ResponseEntity<String> entity = new ResponseEntity<>("Successful", HttpStatus.ACCEPTED);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(entity);
        ResponseEntity<String> response = controller.issueRepaymentCharge(charges, 1);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Successful", response.getBody());
    }

    @Test
    void registerBillingComplete() {
        ResponseEntity<String> entity = new ResponseEntity<>("Accepted", HttpStatus.OK);
        when(service.issueBillingComplete(anyInt())).thenReturn(entity);
        ResponseEntity<String> response = controller.registerBillingComplete(1);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("Accepted", response.getBody());
    }

    @Test
    void startBillingForDates() {
        ResponseEntity<List<Advance>> entity = new ResponseEntity<List<Advance>>(new ArrayList(), HttpStatus.PAYMENT_REQUIRED);
        List<String> dates = Arrays.asList("2022-03-17","2022-03-18","2022-03-19","2022-03-20");
        when(service.startBilling(anyList())).thenReturn(entity);
        ResponseEntity<List<Advance>> response = controller.startBillingForDates(dates);

        assertTrue(response.getStatusCode().is4xxClientError());
        assertTrue(response.getBody().isEmpty());
    }
}