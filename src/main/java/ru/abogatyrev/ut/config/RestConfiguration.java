package ru.abogatyrev.ut.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Created by Hamster on 28.03.2016.
 */
@Configuration
public class RestConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setLimitParamName("size"); // название параметра для передачи числа записей на странице
        super.configureRepositoryRestConfiguration(config);
    }
}
