package com.project.thelittlethings.View;

import java.time.Instant;

// src/main/java/com/project/thelittlethings/repositories/views/CategoryNeglectView.java
public interface CategoryNeglectView {
  Long getCategoryId();
  Long getUserId();
  String getName();
  String getDescription();
  Instant getLastWinAt();  // or Instant if you prefer
  Long getRecentWins();                     // wins in last N days
  Long getNeglectDays();                    // days since lastWinAt (or since category created if never won)
}