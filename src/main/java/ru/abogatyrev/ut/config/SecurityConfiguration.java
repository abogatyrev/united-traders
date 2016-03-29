package ru.abogatyrev.ut.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

/**
 * Created by Hamster on 27.03.2016.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    static final String MEMBER_ROLE_NAME = "MEMBER";
    static final String ADMIN_ROLE_NAME = "ADMIN";

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("admin").password("admin").roles(MEMBER_ROLE_NAME, ADMIN_ROLE_NAME)
                .and()
                .withUser("user").password("user").roles(MEMBER_ROLE_NAME, ADMIN_ROLE_NAME)
                .and()
                .withUser("other").password("other").roles(MEMBER_ROLE_NAME);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(digestEntryPoint())
                .and().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/error/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").hasRole(MEMBER_ROLE_NAME)
                .antMatchers(HttpMethod.POST, "/").hasRole(ADMIN_ROLE_NAME)
                .and().exceptionHandling().accessDeniedPage("/WEB-INF/error/403")
                .and().addFilter(digestAuthenticationFilter());
    }

    public DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey("key");
        digestAuthenticationEntryPoint.setRealmName("realm");
        digestAuthenticationEntryPoint.setNonceValiditySeconds(60*60*24*14); // 14 days
        return digestAuthenticationEntryPoint;
    }

    public DigestAuthenticationFilter digestAuthenticationFilter() throws Exception {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
        digestAuthenticationFilter.setUserDetailsService(userDetailsServiceBean());
        return digestAuthenticationFilter;
    }

}
