package fi.sangre.test.graphql;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class GraphqlResult implements MvcResult {

    private final GraphqlRequest request;
    private final GraphqlResponse response;

    GraphqlResult(final GraphqlRequest request, final GraphqlResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public MockHttpServletRequest getRequest() {
        return request;
    }

    @Override
    public MockHttpServletResponse getResponse() {
        return response;
    }

    @Override
    public Object getHandler() {
        return null;
    }

    @Override
    public HandlerInterceptor[] getInterceptors() {
        return new HandlerInterceptor[0];
    }

    @Override
    public ModelAndView getModelAndView() {
        return null;
    }

    @Override
    public Exception getResolvedException() {
        return null;
    }

    @Override
    public FlashMap getFlashMap() {
        return null;
    }

    @Override
    public Object getAsyncResult() {
        return null;
    }

    @Override
    public Object getAsyncResult(long timeToWait) {
        return null;
    }
}
