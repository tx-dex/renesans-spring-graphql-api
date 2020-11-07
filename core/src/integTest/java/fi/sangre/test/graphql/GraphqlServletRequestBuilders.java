package fi.sangre.test.graphql;


import org.springframework.http.HttpMethod;

public class GraphqlServletRequestBuilders {

    public static GraphqlServletRequestBuilder post() {
        return new GraphqlServletRequestBuilder(HttpMethod.POST.name());
    }

}
