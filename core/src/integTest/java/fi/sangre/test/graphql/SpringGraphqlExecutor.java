package fi.sangre.test.graphql;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.client.RestTemplate;

public class SpringGraphqlExecutor {

    private Long port;
    private String endpoint;


    public SpringGraphqlExecutor(Long port, String endpoint) {
        this.port = port;
        this.endpoint = endpoint;
    }

    public ResultActions perform(GraphqlServletRequestBuilder requestBuilder) {

        final GraphqlRequest request = requestBuilder.buildRequest();
        final RestTemplate restTemplate = new RestTemplate();
        final GraphqlResponse response = new GraphqlResponse(restTemplate.postForEntity("http://localhost:" + port + "/" + endpoint, request.getRequestEntity(), String.class));

        final MvcResult mvcResult = new GraphqlResult(request, response);

        return new ResultActions() {
            @Override
            public ResultActions andExpect(ResultMatcher matcher) throws Exception {
                matcher.match(mvcResult);
                return this;
            }
            @Override
            public ResultActions andDo(ResultHandler handler) throws Exception {
                handler.handle(mvcResult);
                return this;
            }
            @Override
            public MvcResult andReturn() {
                return mvcResult;
            }
        };
    }
}
