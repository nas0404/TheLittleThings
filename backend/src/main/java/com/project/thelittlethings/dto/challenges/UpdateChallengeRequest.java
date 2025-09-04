package com.project.thelittlethings.dto.challenges;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
public class UpdateChallengeRequest {

    @Min(0)
    private Integer current;

    private Integer delta;

    public Integer getCurrent() { return current; }
    public void setCurrent(Integer current) { this.current = current; }

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }

    @AssertTrue(message = "Either current or delta must be provided")
    public boolean isValid() {
        return current != null || delta != null;
    }
}
