package com.policyadmin.client.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.policyadmin.config.ClientGenderProperties;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidGenderValidatorTest {

    private ValidGenderValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        ClientGenderProperties.Gender male = new ClientGenderProperties.Gender();
        male.setCode("M");
        ClientGenderProperties.Gender female = new ClientGenderProperties.Gender();
        female.setCode("F");

        ClientGenderProperties properties = new ClientGenderProperties();
        properties.setGenders(List.of(male, female));

        validator = new ValidGenderValidator(properties);

        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
                ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void acceptsConfiguredCodes() {
        assertTrue(validator.isValid("M", context));
        assertTrue(validator.isValid("F", context));
    }

    @Test
    void rejectsUnknownCode() {
        assertFalse(validator.isValid("X", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("must be one of [M, F]");
    }

    @Test
    void skipsNullOrBlankValuesAndLetsOtherValidatorsHandleThem() {
        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("", context));
        assertTrue(validator.isValid("   ", context));
        verify(context, never()).disableDefaultConstraintViolation();
    }
}
