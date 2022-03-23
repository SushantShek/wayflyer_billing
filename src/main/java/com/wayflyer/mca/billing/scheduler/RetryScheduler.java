package com.wayflyer.mca.billing.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wayflyer.mca.billing.repo.BillingStorage;
import com.wayflyer.mca.billing.service.BillingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RetryScheduler {
    private BillingStorage storage;
    private BillingService service;

    public RetryScheduler(BillingStorage storage, BillingService service) {
        this.storage = storage;
        this.service = service;
    }

    @Scheduled(fixedRateString = "PT10S")
    public void runRetry() {
        Set<String> failedDates = storage.getFailedDatesToRetry();
        failedDates.stream().forEach(date -> {
            try {
                service.getCustomerAdvances(date);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            failedDates.remove(date);
        });
    }
    // TODO - For failed Advances
}
