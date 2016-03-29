package ru.abogatyrev.ut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Created by Hamster on 27.03.2016.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class UnitedTradersApp {
    public static void main(String[] args) {
        SpringApplication.run(UnitedTradersApp.class, args);
    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return (container -> {
            ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/WEB-INF/error/401");
            //ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, "/WEB-INF/error/403"); // так не передается Principal ...
            //container.addErrorPages(error401Page, error403Page);
            container.addErrorPages(error401Page);
        });
    }
}
