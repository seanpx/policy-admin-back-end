package com.policyadmin.client.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.policyadmin.config.ClientIdTypeProperties;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidIdTypeValidatorTest {

    private ValidIdTypeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        ClientIdTypeProperties.IdType ic = new ClientIdTypeProperties.IdType();
        ic.setCode("IC");
        ClientIdTypeProperties.IdType ip = new ClientIdTypeProperties.IdType();
        ip.setCode("IP");

        ClientIdTypeProperties properties = new ClientIdTypeProperties();
        properties.setIdTypes(List.of(ic, ip));

        validator = new ValidIdTypeValidator(properties);

        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void acceptsConfiguredCodes() {
        assertTrue(validator.isValid("IC", context));
        assertTrue(validator.isValid("IP", context));
    }

    @Test
    void rejectsUnknownCode() {
        assertFalse(validator.isValid("PAS", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("must be one of [IC, IP]");
    }

    @Test
    void skipsNullOrBlankValuesAndLetsOtherValidatorsHandleThem() {
        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("", context));
        assertTrue(validator.isValid("   ", context));
        verify(context, never()).disableDefaultConstraintViolation();
    }
}
