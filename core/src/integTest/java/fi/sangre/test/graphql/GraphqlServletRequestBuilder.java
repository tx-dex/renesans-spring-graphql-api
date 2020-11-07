package fi.sangre.test.graphql;

import com.google.api.client.util.Throwables;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.Map;


public class GraphqlServletRequestBuilder {

    private final RestTemplateBuilder restTemplateBuilder;
    private final String method;
    private String content;
    private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

    GraphqlServletRequestBuilder(String method) {
        this.restTemplateBuilder = new RestTemplateBuilder();
        this.method = method;
    }

    /**
     * @param content the body content
     */
    public GraphqlServletRequestBuilder content(String content) {
        this.content = content;
        return this;
    }

    /**
     * Add a header to the request. Values are always added.
     * @param name the header name
     * @param values one or more header values
     */
    public GraphqlServletRequestBuilder header(String name, String... values) {
        addToMultiValueMap(this.headers, name, values);
        return this;
    }

    private static <T> void addToMultiValueMap(MultiValueMap<String, T> map, String name, T[] values) {
        Assert.hasLength(name, "'name' must not be empty");
        Assert.notEmpty(values, "'values' must not be empty");
        for (T value : values) {
            map.add(name, value);
        }
    }

    GraphqlRequest buildRequest() {
        final HttpEntity<String> requestEntity = new HttpEntity<>(generateRequest(content, null), headers);

        return new GraphqlRequest(requestEntity);
    }

    private String generateRequest(String query, Map variables) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("query", query);

            jsonObject.put("variables", variables != null ? Collections.singletonMap("input", variables) : JSONObject.NULL);

            return jsonObject.toString();
        } catch (final Exception e) {
            Throwables.propagate(e);
            return null;
        }
    }
}
