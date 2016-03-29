package ru.abogatyrev.ut;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.abogatyrev.ut.config.RestConfiguration;
import ru.abogatyrev.ut.config.SecurityConfiguration;
import ru.abogatyrev.ut.exceptions.LogItemValidationRestException;
import ru.abogatyrev.ut.model.Item;

/**
 * Created by Hamster on 27.03.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {UnitedTradersApp.class, RestConfiguration.class, SecurityConfiguration.class})
@WebIntegrationTest("server.port:8080")
public class ApplicationIntegrationTests {
    private static final String TEST_URI = "http://localhost:8080/";
    private static final String ACCESS_DENIED_MSG = "Access denied";

    private static final String CONTENT_TEMPLATE = "{\"dt\": \"%s\",\"level\": \"%s\",\"message\": \"this is '%s' message\"}";

    private HttpHost target;
    private HttpClientContext localContext;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void beforeClass() {
        mapper = new ObjectMapper();
    }

    @Before
    public void setUp() {
        target = new HttpHost("localhost", 8080, "http");

        AuthCache authCache = new BasicAuthCache();
        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", "realm");
        digestAuth.overrideParamter("nonce", "whatever");
        authCache.put(target, digestAuth);

        localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
    }

    private CloseableHttpClient closeableHttpClient(String login, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(login, password));
        return HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
    }

    private HttpGet httpGet() {
        return new HttpGet(TEST_URI);
    }

    private HttpPost httpPost() {
        return new HttpPost(TEST_URI);
    }

    private <T extends LogItemValidationRestException.Error> void assertMessage(String expected, String content, Class<T> clazz) throws Exception {
        T responseBody = mapper.readValue(content, clazz);
        Assert.assertEquals(responseBody.getMessage(), expected);
    }

    @Test
    public void rejectGetLogItemsForUnauthorized() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("", "");
        try {
            HttpGet httpGet = httpGet();
            CloseableHttpResponse response = httpclient.execute(target, httpGet, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.UNAUTHORIZED.value());

                String content = EntityUtils.toString(response.getEntity());
                assertMessage(ACCESS_DENIED_MSG, content, LogItemValidationRestException.Error.class);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void rejectPostLogItemForUnauthorized() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("", "");
        try {
            HttpPost httpPost = httpPost();
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.UNAUTHORIZED.value());

                String content = EntityUtils.toString(response.getEntity());
                assertMessage(ACCESS_DENIED_MSG, content, LogItemValidationRestException.Error.class);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void rejectPostLogItemForOtherUser() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("other", "other");
        try {
            HttpPost httpPost = httpPost();
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.FORBIDDEN.value());

                String content = EntityUtils.toString(response.getEntity());
                assertMessage("User 'other' does not have access", content, LogItemValidationRestException.Error.class);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void allowsPostAndGetLogItemsForAdminUser() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("admin", "admin");
        try {
            HttpPost httpPost = httpPost();
            httpPost.setEntity(new StringEntity(String.format(CONTENT_TEMPLATE, "2016-03-26T19:20:30+01:00", "INFO", "info"), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.OK.value());

                String content = EntityUtils.toString(response.getEntity());
                Item item = mapper.readValue(content, Item.class);
                Assert.assertTrue(item.getId() > 0);

                response = httpclient.execute(target, httpGet(), localContext);
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.OK.value());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void allowsPostAndGetLogItemsForUser() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("user", "user");
        try {
            HttpPost httpPost = httpPost();
            httpPost.setEntity(new StringEntity(String.format(CONTENT_TEMPLATE, "2016-03-26T19:20:30+01:00", "INFO", "info"), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.OK.value());

                String content = EntityUtils.toString(response.getEntity());
                Item item = mapper.readValue(content, Item.class);
                Assert.assertTrue(item.getId() > 0);

                response = httpclient.execute(target, httpGet(), localContext);
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.OK.value());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void allowsGetLogItemsForOtherUser() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("other", "other");
        try {
            CloseableHttpResponse response = httpclient.execute(target, httpGet(), localContext);
            try {
                response = httpclient.execute(target, httpGet(), localContext);
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.OK.value());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void postLogItemsWithWrongLevel() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("user", "user");
        try {
            HttpPost httpPost = httpPost();
            httpPost.setEntity(new StringEntity(String.format(CONTENT_TEMPLATE, "2016-03-26T19:20:30+01:00", "WRONG-LEVEL", "wrong"), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());

                String content = EntityUtils.toString(response.getEntity());
                LogItemValidationRestException error = mapper.readValue(content, LogItemValidationRestException.class);
                Assert.assertTrue(error.getErrors().size() == 1);
                Assert.assertEquals(error.getErrors().get(0).getField(), "level");

            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void postLogItemsWithWrongDt() throws Exception {
        CloseableHttpClient httpclient = closeableHttpClient("user", "user");
        try {
            HttpPost httpPost = httpPost();
            httpPost.setEntity(new StringEntity(String.format(CONTENT_TEMPLATE, "WRONG-DT", "ERROR", "error"), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);
            try {
                Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.BAD_REQUEST.value());

                String content = EntityUtils.toString(response.getEntity());
                LogItemValidationRestException error = mapper.readValue(content, LogItemValidationRestException.class);
                Assert.assertTrue(error.getErrors().size() == 1);
                Assert.assertEquals(error.getErrors().get(0).getField(), "dt");

            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}
