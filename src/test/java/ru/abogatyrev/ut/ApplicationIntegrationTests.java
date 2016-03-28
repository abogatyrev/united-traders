package ru.abogatyrev.ut;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.abogatyrev.ut.config.RestConfiguration;
import ru.abogatyrev.ut.config.SecurityConfiguration;
import ru.abogatyrev.ut.exceptions.LogItemValidationRestException;
import ru.abogatyrev.ut.model.Item;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by Hamster on 27.03.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {UnitedTradersApp.class, RestConfiguration.class, SecurityConfiguration.class})
public class ApplicationIntegrationTests {
    private static final String CONTENT_TEMPLATE = "{\"dt\": \"%s\",\"level\": \"%s\",\"message\": \"this is '%s' message\"}";

    @Autowired
    WebApplicationContext context;
    @Autowired
    FilterChainProxy filterChain;

    MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = webAppContextSetup(context).addFilters(filterChain).build();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectGetLogItemsForUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        mvc.perform(get("/")
                .content(CONTENT_TEMPLATE)
                .headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void rejectPostLogItemForUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        mvc.perform(post("/")
                .content(CONTENT_TEMPLATE)
                .headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void rejectPostLogItemForOtherUser() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("other:other").getBytes())));
        mvc.perform(post("/")
                .content(CONTENT_TEMPLATE)
                .headers(headers))
                .andExpect(status().isForbidden());
    }

    @Test
    public void allowsPostAndGetLogItemsForAdminUser() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("admin:admin").getBytes())));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String content = mvc.perform(post("/")
                .content(String.format(CONTENT_TEMPLATE, "2016-03-26T19:20:30+01:00", "INFO", "info"))
                .headers(headers))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Item item = mapper.readValue(content, Item.class);
        Assert.assertTrue(item.getId() > 0);

        mvc.perform(get("/").
                headers(headers)).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")).
                andExpect(status().isOk()).
                andDo(print());
    }

    @Test
    public void allowsPostAndGetLogItemsForUser() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("user:user").getBytes())));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String content = mvc.perform(post("/")
                .content(String.format(CONTENT_TEMPLATE, "2016-03-26T20:20:30+01:00", "DEBUG", "debug"))
                .headers(headers))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Item item = mapper.readValue(content, Item.class);
        Assert.assertTrue(item.getId() > 0);

        mvc.perform(get("/").
                headers(headers)).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")).
                andExpect(status().isOk()).
                andDo(print());
    }

    @Test
    public void allowsGetLogItemsForOtherUser() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("other:other").getBytes())));

        mvc.perform(get("/").
                headers(headers)).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")).
                andExpect(status().isOk()).
                andDo(print());
    }

    @Test
    public void postLogItemsWithWrongLevel() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("user:user").getBytes())));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String content = mvc.perform(post("/")
                .content(String.format(CONTENT_TEMPLATE, "2016-03-26T20:20:30+01:00", "WRONG-LEVEL", "debug"))
                .headers(headers))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        LogItemValidationRestException error = mapper.readValue(content, LogItemValidationRestException.class);
        Assert.assertTrue(error.getErrors().size() == 1);
        Assert.assertEquals(error.getErrors().get(0).getField(), "level");
    }

    @Test
    public void postLogItemsWithWrongDt() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(("user:user").getBytes())));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String content = mvc.perform(post("/")
                .content(String.format(CONTENT_TEMPLATE, "WRONG_DT", "ERROR", "error"))
                .headers(headers))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        LogItemValidationRestException error = mapper.readValue(content, LogItemValidationRestException.class);
        Assert.assertTrue(error.getErrors().size() == 1);
        Assert.assertEquals(error.getErrors().get(0).getField(), "dt");
    }

}
