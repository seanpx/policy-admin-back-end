package com.policyadmin.client.validation;

import com.policyadmin.config.ClientGenderProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ValidGenderValidator implements ConstraintValidator<ValidGender, String> {

    private final Set<String> allowedCodes;

    public ValidGenderValidator(ClientGenderProperties properties) {
        this.allowedCodes = properties.getGenders() == null
                ? Set.of()
                : properties.getGenders().stream()
                        .map(ClientGenderProperties.Gender::getCode)
                        .filter(code -> code != null && !code.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (allowedCodes.contains(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("must be one of " + allowedCodes)
                .addConstraintViolation();
        return false;
    }
}
