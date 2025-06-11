package fr.larousso.graphql.utils;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class DateTimeScalar {

    public static GraphQLScalarType localDateTimeScalarType() {
        return GraphQLScalarType.newScalar()
                .name("LocalDateTime")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) dataFetcherResult);
                    }

                    
                    @Override
                    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                        return parseString(
                                LocalDateTime.class,
                                input,
                                stringValue -> LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        );
                    }

                    
                    @Override
                    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        return parseStringLiteral(
                                LocalDateTime.class,
                                input, stringValue -> LocalDateTime.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        );
                    }
                })
                .build();
    }

    private static <T> T parseString(Class<T> clazz, Object input, Function<String, T> parser) {
        requireNonNull(clazz);
        requireNonNull(input);
        requireNonNull(parser);

        if (input instanceof String) {
            final var stringValue = String.valueOf(input);
            try {
                return parser.apply(stringValue);
            } catch (Exception e) {
                throw new CoercingParseValueException(getExceptionMessage(clazz, stringValue));
            }
        }
        throw new CoercingParseValueException(getExceptionMessage(clazz, input));
    }

    private static <T> T parseStringLiteral(Class<T> clazz, Object input, Function<String, T> parser) {
        requireNonNull(clazz);
        requireNonNull(input);
        requireNonNull(parser);

        if (input instanceof StringValue) {
            final var stringValue = ((StringValue) input).getValue();
            try {
                return parser.apply(stringValue);
            } catch (Exception e) {
                throw new CoercingParseValueException(getExceptionMessage(clazz, stringValue));
            }
        }
        if (clazz.isAssignableFrom(input.getClass())) {
            return clazz.cast(input);
        }
        throw new CoercingParseValueException(getExceptionMessage(clazz, input));
    }

    private static String getExceptionMessage(Class<?> clazz, Object input) {
        return String.format("Invalid %s format [%s]", clazz.getSimpleName(), input);
    }

    public static GraphQLScalarType localDateScalarType() {
        return GraphQLScalarType.newScalar()
                .name("LocalDate")
                .coercing(new Coercing<LocalDate, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return DateTimeFormatter.ISO_LOCAL_DATE.format((LocalDate) dataFetcherResult);
                    }

                    
                    @Override
                    public LocalDate parseValue(Object input) throws CoercingParseValueException {
                        return parseString(
                                LocalDate.class,
                                input,
                                stringValue -> LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE)
                        );
                    }

                    
                    @Override
                    public LocalDate parseLiteral(Object input) throws CoercingParseLiteralException {
                        return parseStringLiteral(
                                LocalDate.class,
                                input,
                                stringValue -> LocalDate.parse(stringValue, DateTimeFormatter.ISO_LOCAL_DATE)
                        );
                    }
                })
                .build();
    }

    public static GraphQLScalarType yearScalarType() {
        return GraphQLScalarType.newScalar()
                .name("Year")
                .coercing(new Coercing<>() {
                    @Override
                    public Integer serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return ((Year) dataFetcherResult).getValue();
                    }

                    
                    @Override
                    public Year parseValue(Object input) throws CoercingParseValueException {
                        if (input instanceof Integer) {
                            final var stringValue = String.valueOf(input);
                            try {
                                return Year.parse(stringValue);
                            } catch (Exception e) {
                                throw new CoercingParseValueException(getExceptionMessage(Year.class, stringValue));
                            }
                        }
                        if (input instanceof Year) {
                            return (Year) input;
                        }
                        throw new CoercingParseValueException(getExceptionMessage(Year.class, input));
                    }

                    
                    @Override
                    public Year parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof IntValue) {
                            final var stringValue = String.valueOf(((IntValue) input).getValue());
                            try {
                                return Year.parse(stringValue);
                            } catch (Exception e) {
                                throw new CoercingParseValueException(getExceptionMessage(Year.class, stringValue));
                            }
                        }
                        throw new CoercingParseValueException(getExceptionMessage(Year.class, input));
                    }
                })
                .build();

    }
}
