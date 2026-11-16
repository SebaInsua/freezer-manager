
package com.freezermanager.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.freezermanager.service.CurrentUserProvider;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "No hay un usuario autenticado."
            );
        }

        return authentication.getName();
    }
}
