package com.wayflyer.mca.billing.repo;

import com.wayflyer.mca.billing.domain.Advance;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BillingStorage {
    private static final Map<Integer, Advance> billing = new ConcurrentHashMap<>();
    private static Set<String> failedDates = new TreeSet<>();
    private static Deque<Advance> failedBilling = new LinkedList();
    private static Set<Integer> failedCompletions = new TreeSet<>();

    public void saveAdvance(List<Advance> advances) {
        for (Advance adv : advances){
            billing.put(adv.getId(), adv);
        }
    }
    public Advance updateAdvance(Advance adv) {
           return billing.put(adv.getId(), adv);
    }

    public Map<Integer, Advance> getStorage(){
        return billing;
    }

    public void addFailedDatesToRetry(String date) {
        failedDates.add(date);
    }
    public Set<String> getFailedDatesToRetry() {
        return failedDates;
    }
    public Set<String> updatesFailedDates(String Date) {
        return failedDates;
    }

    public void addFailedBillingsToRetry(Advance billing) {
        failedBilling.add(billing);
    }

    public void addFailedCompletionsToRetry(int id) {
        failedCompletions.add(id);
    }
    public Set<Integer> getFailedCompletionsToRetry() {
        return failedCompletions;
    }
}
