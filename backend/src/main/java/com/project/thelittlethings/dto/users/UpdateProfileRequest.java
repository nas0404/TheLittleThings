package com.project.thelittlethings.dto.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String displayName;  // optional if you let user rename display name
    private String bio;          // maps to 'bio' column
    private String avatarUrl;    // maps to 'avatar_url' column
}
