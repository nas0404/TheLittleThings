package com.project.thelittlethings.MaterialisedView;

import java.time.Instant;

public interface CategoryNeglectedView {
    Long getCategoryId();
    Long getUserId();
    String getName();
    String getDescription();
    Instant getLastWinAt(); // may be the created_at when no wins
    Long getNeglectDays();  // computed days since last activity
}
