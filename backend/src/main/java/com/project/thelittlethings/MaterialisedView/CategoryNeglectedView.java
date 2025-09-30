package com.project.thelittlethings.MaterialisedView;

import java.time.Instant;

public interface CategoryNeglectedView {
    Long getCategoryId();
    Long getUserId();
    String getName();
    String getDescription();
    Instant getLastWinAt(); 
    Long getNeglectDays(); 
}
