package livebus.security.dto;

import livebus.security.model.Role;

public record LoginResponse(String username, Role role) {
}
