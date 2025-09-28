package com.project.thelittlethings.MaterialisedView;

import java.time.Instant;

public interface CategoryNeglectedView {
    Long getCategoryId();
    String getName();
    Instant getLastWinAt(); // can be null
}
