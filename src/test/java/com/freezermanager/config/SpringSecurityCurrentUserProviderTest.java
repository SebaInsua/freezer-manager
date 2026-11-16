package com.freezermanager.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityCurrentUserProviderTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

    private final SpringSecurityCurrentUserProvider currentUserProvider =
        new SpringSecurityCurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReturnAuthenticatedUsername(){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                    AUTHENTICATED_EMAIL,
                    null,
                    List.of()
            )
            );

        String result = currentUserProvider.getCurrentUsername();

        assertEquals(AUTHENTICATED_EMAIL, result);
    }

    @Test
    public void shouldThrowIllegalExceptionWhenAuthIsNull(){
        SecurityContextHolder.clearContext();

        IllegalStateException exception = assertThrows(IllegalStateException.class,() -> currentUserProvider.getCurrentUsername());
        
        assertEquals("No hay un usuario autenticado.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenAuthenticationIsNotAuthenticated(){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                    AUTHENTICATED_EMAIL,
                    "password"
            )
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,() -> currentUserProvider.getCurrentUsername());
        assertEquals("No hay un usuario autenticado.", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenAuthenticationIsAnonymous(){
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(
                "anonymous-key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
            )
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,() -> currentUserProvider.getCurrentUsername());
        assertEquals("No hay un usuario autenticado.", exception.getMessage());
    }

        @Test
    public void shouldThrowExceptionWhenAuthenticationNameItsBlank(){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                    "",
                    null,
                    List.of()
            )
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,() -> currentUserProvider.getCurrentUsername());
        assertEquals("No hay un usuario autenticado.", exception.getMessage());
    }

            @Test
    public void shouldThrowExceptionWhenAuthenticationNameItsNull(){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                    null,
                    null,
                    List.of()
            )
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,() -> currentUserProvider.getCurrentUsername());
        assertEquals("No hay un usuario autenticado.", exception.getMessage());
    }
}
