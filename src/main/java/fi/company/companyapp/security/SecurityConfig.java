package fi.company.companyapp.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import fi.company.companyapp.views.auth.LoginView;
import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends VaadinWebSecurity {

    private final OAuth2UserSuccessHandler oAuth2UserSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(auth -> auth
            .antMatchers("/images/**", "/icons/**", "/favicon.ico",
                         "/register", "/register/**", "/error").permitAll()
        );

        setLoginView(http, LoginView.class);
        super.configure(http);

        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .successHandler(oAuth2UserSuccessHandler)
        );
    }
}
