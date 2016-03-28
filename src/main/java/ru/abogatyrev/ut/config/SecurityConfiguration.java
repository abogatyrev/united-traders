package ru.abogatyrev.ut.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
                .httpBasic().and().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/error/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").hasRole(MEMBER_ROLE_NAME)
                .antMatchers(HttpMethod.POST, "/").hasRole(ADMIN_ROLE_NAME)
                //.and().exceptionHandling().accessDeniedPage("/error/401")
        ;
    }

}
