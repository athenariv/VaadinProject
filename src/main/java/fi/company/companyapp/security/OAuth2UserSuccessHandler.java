package fi.company.companyapp.security;

import fi.company.companyapp.service.AppUserService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2UserSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final AppUserService appUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = authentication.getAuthorities().toString().contains("SCOPE") ? "google" : "github";
        String providerId = oAuth2User.getName();

        appUserService.findOrCreateOAuthUser(provider, providerId, email, name);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
