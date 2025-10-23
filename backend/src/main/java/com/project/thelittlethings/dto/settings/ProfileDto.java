package com.project.thelittlethings.dto.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileDto(
  @NotBlank @Size(max=100) String displayName,
  @Size(max=5000) String bio,
  String avatarUrl
) {}

