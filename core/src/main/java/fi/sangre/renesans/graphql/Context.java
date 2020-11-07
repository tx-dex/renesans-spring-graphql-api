package fi.sangre.renesans.graphql;

import graphql.servlet.context.GraphQLContext;
import graphql.servlet.context.GraphQLServletContext;
import lombok.Getter;
import lombok.Setter;
import org.dataloader.DataLoaderRegistry;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Setter
@Getter
public class Context implements GraphQLContext {
    private final GraphQLServletContext context;
    private String languageCode;

    public Context(final GraphQLServletContext context, final String languageCode) {
        this.context = context;
        this.languageCode = languageCode;
    }

    @Override
    public Optional<Subject> getSubject() {
        return context.getSubject();
    }

    @Override
    public Optional<DataLoaderRegistry> getDataLoaderRegistry() {
        return context.getDataLoaderRegistry();
    }
}
