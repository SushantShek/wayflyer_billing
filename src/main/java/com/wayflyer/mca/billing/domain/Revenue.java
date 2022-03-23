package com.wayflyer.mca.billing.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Data
public class Revenue {
    private String amount;
}
