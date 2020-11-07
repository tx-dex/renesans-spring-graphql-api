package fi.sangre.test.graphql;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;

class GraphqlResponse extends MockHttpServletResponse {

    private final ResponseEntity<String> response;

    GraphqlResponse(final ResponseEntity<String> response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatusCodeValue();
    }

    @Override
    public String getContentAsString() throws UnsupportedEncodingException {
        return response.getBody();
    }
}
