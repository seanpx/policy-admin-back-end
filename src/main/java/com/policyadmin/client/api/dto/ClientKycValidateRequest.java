package com.policyadmin.client.api.dto;

import com.policyadmin.client.kyc.ClientKycRequest;
import com.policyadmin.client.validation.ValidGender;
import com.policyadmin.client.validation.ValidIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Payload for performing a KYC-only validation.
 */
public record ClientKycValidateRequest(
        @NotBlank String surname,
        @NotBlank String givname,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Size(min = 1, max = 1) @ValidGender String gender,
        @NotBlank @Size(min = 1, max = 3) @ValidIdType String idType,
        @NotBlank String idNumber,
        boolean ignorePossibleMatch
) {
    public ClientKycRequest toDomainRequest() {
        return new ClientKycRequest(
                surname,
                givname,
                dateOfBirth,
                gender,
                idType,
                idNumber,
                ignorePossibleMatch
        );
    }
}
