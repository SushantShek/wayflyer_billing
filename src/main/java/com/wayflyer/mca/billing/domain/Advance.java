package com.wayflyer.mca.billing.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advance {

    private int id;
    private int customer_id;
    private String created;
    private String total_advanced;
    private String fee;
    private int mandate_id;
    private String repayment_start_date;
    private int repayment_percentage;
}
