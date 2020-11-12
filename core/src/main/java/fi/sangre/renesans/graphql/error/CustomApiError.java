package fi.sangre.renesans.graphql.error;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class CustomApiError implements GraphQLError {
    private final String message;
    private final Map<String, Object> extensions;
    private final List<SourceLocation> locations;
    private final List<Object> path;

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    public String getMessage() {
        return message;
    }
}