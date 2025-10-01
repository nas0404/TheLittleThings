package com.project.thelittlethings.dto.friends;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FriendshipResponse {
    private Long id;
    private Long friendId;
    private String friendUsername;
    private String status;
    private boolean outgoing;
    private OffsetDateTime requestedAt;
}
