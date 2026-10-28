
package com.freezermanager.dto;

public record AuthResponse(
    String token,
    String tokenType
) {
}
