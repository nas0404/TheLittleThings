package com.project.thelittlethings.dto.challenges;

public enum ChallengeStatus {
    SUGGESTED,   // system created, waiting for user accept/decline
    ACTIVE,      // accepted and in progress
    COMPLETED,   // finished successfully
    DECLINED,    // user declined
    ARCHIVED     // hidden from active list but kept for history
}
