package dev.henan.ecommerce.auth.dto;

import dev.henan.ecommerce.auth.Role;
import dev.henan.ecommerce.auth.User;

import java.util.List;

public record UserResponse(Long id, String name, String email, List<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(Role::name).sorted().toList());
    }
}
