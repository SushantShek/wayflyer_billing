package com.wayflyer.mca.billing.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
public class Charges {
    private String amount;
}
