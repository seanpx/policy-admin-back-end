package com.policyadmin.client.validation;

import com.policyadmin.config.ClientIdTypeProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ValidIdTypeValidator implements ConstraintValidator<ValidIdType, String> {

    private final Set<String> allowedCodes;

    public ValidIdTypeValidator(ClientIdTypeProperties properties) {
        this.allowedCodes = properties.getIdTypes() == null
                ? Set.of()
                : properties.getIdTypes().stream()
                        .map(ClientIdTypeProperties.IdType::getCode)
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
