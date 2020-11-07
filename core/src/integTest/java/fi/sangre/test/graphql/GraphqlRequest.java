package fi.sangre.test.graphql;

import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;

class GraphqlRequest extends MockHttpServletRequest {

    private final HttpEntity<String> requestEntity;

    GraphqlRequest(HttpEntity<String> requestEntity) {
        this.requestEntity = requestEntity;
    }

    HttpEntity<String> getRequestEntity() {
        return requestEntity;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(requestEntity.getHeaders().keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(requestEntity.getHeaders().get(name));
    }
}
