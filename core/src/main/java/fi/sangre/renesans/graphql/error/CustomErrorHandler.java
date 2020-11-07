package fi.sangre.renesans.graphql.error;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.core.GraphQLErrorHandler;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Component
public class CustomErrorHandler implements GraphQLErrorHandler {
    private static final String ERROR_CODE_EXTENSION = "errorCode";

    private final Function<ExceptionWhileDataFetching, GraphQLError> unknownExceptionProcessor = this::processUnknownException;

    @Override
    public List<GraphQLError> processErrors(@Nullable final List<GraphQLError> errors) {
        if (errors != null) {
            return errors.stream()
                    .filter(Objects::nonNull)
                    .map(this::process)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return ImmutableList.of();
        }
    }

    private boolean isClientError(@NonNull final GraphQLError error) {
        if (error instanceof ExceptionWhileDataFetching) {
            return ((ExceptionWhileDataFetching) error).getException() instanceof GraphQLError;
        }
        return !(error instanceof Throwable);
    }

    @NonNull
    private GraphQLError process(@NonNull final GraphQLError error) {
        if (isClientError(error)) {
            return error;
        } else if (error instanceof ExceptionWhileDataFetching){
            final ExceptionWhileDataFetching dataFetchingError = (ExceptionWhileDataFetching) error;
            if (dataFetchingError.getException() != null) {
                return unknownExceptionProcessor.apply(dataFetchingError);
            } else {
                return error;
            }
        } else {
            return error;
        }
    }

    @NonNull
    private  GraphQLError processUnknownException(@NonNull final ExceptionWhileDataFetching error) {
        return new CustomApiError(error.getException().getMessage(),
                ImmutableMap.of(ERROR_CODE_EXTENSION, ErrorCodes.UNKNOWN_ERROR.getValue()),
                error.getLocations());
    }
}
