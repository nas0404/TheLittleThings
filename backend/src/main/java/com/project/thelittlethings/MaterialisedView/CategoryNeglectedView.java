package com.project.thelittlethings.MaterialisedView;

import java.time.Instant;

public interface CategoryNeglectedView {

    // Interface to map results from the CategoryNeglected materialized view
    Long getCategoryId();
    Long getUserId();
    String getName();
    String getDescription();
    Instant getLastWinAt(); 
    Long getNeglectDays(); 
}
