package com.wayflyer.mca.billing.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.domain.Advance;
import com.wayflyer.mca.billing.domain.Customer;
import com.wayflyer.mca.billing.domain.Revenue;
import com.wayflyer.mca.billing.repo.BillingStorage;
import com.wayflyer.mca.billing.service.BillingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BillingTest {

    @Mock
    BillingStorage storage;

    Billing billing;
    @Mock
    BillingService service;
    Customer customer;

    ResponseEntity<String> response = new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);

    Advance adv;
    private final String sample = "[{\"id\":1,\"customer_id\":1,\"created\":\"2022-01-02\",\"total_advanced\":\"60000.00\",\"fee\":\"2500.00\",\"mandate_id\":2,\"repayment_start_date\":\"2022-01-05\",\"repayment_percentage\":11}," +
            "{\"id\":2,\"customer_id\":1,\"created\":\"2022-02-02\",\"total_advanced\":\"100000.00\",\"fee\":\"4000.00\",\"mandate_id\":2,\"repayment_start_date\":\"2022-02-05\",\"repayment_percentage\":16}," +
            "{\"id\":3,\"customer_id\":2,\"created\":\"2022-01-10\",\"total_advanced\":\"300000.00\",\"fee\":\"14000.00\",\"mandate_id\":1,\"repayment_start_date\":\"2022-01-10\",\"repayment_percentage\":17}," +
            "{\"id\":4,\"customer_id\":3,\"created\":\"2022-02-18\",\"total_advanced\":\"700000.00\",\"fee\":\"40000.00\",\"mandate_id\":4,\"repayment_start_date\":\"2022-03-01\",\"repayment_percentage\":11}," +
            "{\"id\":5,\"customer_id\":4,\"created\":\"2022-03-04\",\"total_advanced\":\"35000.00\",\"fee\":\"2000.00\",\"mandate_id\":3,\"repayment_start_date\":\"2022-03-06\",\"repayment_percentage\":8}]";

    @BeforeEach
    void setUp() {
        billing = new Billing(storage);
        customer = new Customer();
        adv = new Advance(1, 1, "2022-01-02", "60000.00", "2500.00", 2, "2022-01-05", 11);
        customer.setAdvances(List.of(adv));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void doBilling_normalFlow() throws JsonProcessingException {
        boolean resp = false;

        Revenue revenue = new Revenue();
        revenue.setAmount("1234.56");
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(revenue);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(response);
        when(storage.updateAdvance(any())).thenReturn(adv);

        try {
            resp = billing.doBilling(customer, service);
        } catch (Exception ex) {
            System.out.println(" Exception" + ex);
        }
        assertTrue(resp);
    }

    @Test
    void doBilling_totalIsSettled_feeIsNot() throws JsonProcessingException {

        boolean resp = false;

        adv.setTotal_advanced("0");
        Revenue revenue = new Revenue();
        revenue.setAmount("1234.56");
        Mockito.lenient().when(service.getCustomerAdvances()).thenReturn(customer.getAdvances());
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(revenue);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(response);
        when(storage.updateAdvance(any())).thenReturn(adv);

        try {
            resp = billing.doBilling(customer, service);
        } catch (Exception ex) {
            System.out.println(" Exception" + ex);
        }
        assertTrue(resp);
    }

    @Test
    void doBilling_whenTotal_LessThan_daysCharges() throws JsonProcessingException {
        boolean resp = false;

        adv.setTotal_advanced("34.56");
        Revenue revenue = new Revenue();
        revenue.setAmount("1234.56");

        Mockito.lenient().when(service.getCustomerAdvances()).thenReturn(customer.getAdvances());
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(revenue);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(response);
        when(storage.updateAdvance(any())).thenReturn(adv);

        try {
            resp = billing.doBilling(customer, service);
        } catch (Exception ex) {
            System.out.println(" Exception" + ex);
        }
        assertTrue(resp);

    }

    @Test
    void doBilling_TotalIsZero_FeeIsLessThan_dayCharge() throws JsonProcessingException {
        boolean resp = false;

        adv.setTotal_advanced("0");
        adv.setFee("120");
        Revenue revenue = new Revenue();
        revenue.setAmount("1234.56");

        Mockito.lenient().when(service.getCustomerAdvances()).thenReturn(customer.getAdvances());
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(revenue);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(response);
        when(service.issueBillingComplete(anyInt())).thenReturn(response);
        when(storage.updateAdvance(any())).thenReturn(adv);

        try {
            resp = billing.doBilling(customer, service);
        } catch (Exception ex) {
            System.out.println(" Exception" + ex);
        }
        assertTrue(resp);
    }

    @Test
    void doBilling_dayCharge_greaterThan_10000() throws JsonProcessingException {
        boolean resp = false;
        Revenue revenue = new Revenue();
        revenue.setAmount("1234000.56");

        Mockito.lenient().when(service.getCustomerAdvances()).thenReturn(customer.getAdvances());
        when(service.getRevenueForCustomer(anyInt(), anyString())).thenReturn(revenue);
        when(service.issueCharges(anyInt(), anyString())).thenReturn(response);
        Mockito.lenient().when(service.issueBillingComplete(anyInt())).thenReturn(response);
        when(storage.updateAdvance(any())).thenReturn(adv);

        try {
            resp = billing.doBilling(customer, service);
        } catch (Exception ex) {
            System.out.println(" Exception" + ex);
        }
        assertTrue(resp);
    }
}