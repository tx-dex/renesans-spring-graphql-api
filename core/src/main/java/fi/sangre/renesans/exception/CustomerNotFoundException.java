package fi.sangre.renesans.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class CustomerNotFoundException extends RuntimeException implements GraphQLError {
    private Map<String, Object> extensions = new HashMap<>();

    public CustomerNotFoundException(UUID id) {
        super("Customer not found!");
        extensions.put("InvalidCustomerId", id);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return extensions;
    }
}
