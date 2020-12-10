package fi.sangre.renesans.graphql;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
public class Scalars {

    @Bean
    public GraphQLScalarType dateScalar() {
        return new GraphQLScalarType("Date", "Built-in Java Date", new Coercing() {
            @Override
            public String serialize(Object input) {
                if (input instanceof Date) {
                    Date date = (Date) input;
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
                    return DateTimeFormatter.ISO_DATE_TIME.format(localDateTime);
                }
                return null;
            }

            // TODO THIS ONE HAS NOT BEEN TESTED - TEST WHEN ADDING INPUT OBJECTS WITH DATES
            @Override
            public Date parseValue(Object input) {
                if (input instanceof String) {
                    return fromString((String) input);
                }
                return null;
            }

            // TODO THIS ONE HAS NOT BEEN TESTED - TEST WHEN ADDING INPUT OBJECTS WITH DATES
            @Override
            public Date parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    String dateString = ((StringValue) input).getValue();
                    return fromString(dateString);
                }
                return null;
            }

            private Date fromString(String dateString) {
                DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime date = LocalDateTime.parse(dateString, format);
                return Date.from(date.atZone(ZoneId.of("UTC")).toInstant());
            }
        });
    }
}
