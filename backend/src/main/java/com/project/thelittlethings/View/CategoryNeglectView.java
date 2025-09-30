package com.project.thelittlethings.View;

import java.time.Instant;

public interface CategoryNeglectView {
  Long getCategoryId();
  Long getUserId();
  String getName();
  String getDescription();
  Instant getLastWinAt();  
  Long getRecentWins();                     
  Long getNeglectDays();                   
}