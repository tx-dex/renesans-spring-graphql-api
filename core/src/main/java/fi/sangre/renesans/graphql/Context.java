package fi.sangre.renesans.graphql;

import graphql.servlet.context.GraphQLContext;
import graphql.servlet.context.GraphQLServletContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dataloader.DataLoaderRegistry;
import org.springframework.security.core.userdetails.UserDetails;

import javax.security.auth.Subject;
import java.util.Optional;

@AllArgsConstructor
@Setter
@Getter
public class Context implements GraphQLContext {
    private final GraphQLServletContext context;
    private final UserDetails principal;
    private String languageCode;

    @Override
    public Optional<Subject> getSubject() {
        return context.getSubject();
    }

    @Override
    public Optional<DataLoaderRegistry> getDataLoaderRegistry() {
        return context.getDataLoaderRegistry();
    }
}
