package com.policyadmin.client.api.dto;

import com.policyadmin.client.domain.Client;
import com.policyadmin.client.kyc.ClientKycRequest;
import com.policyadmin.client.validation.ValidGender;
import com.policyadmin.client.validation.ValidIdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.lang.NonNull;

/**
 * Payload for creating a client (runs through KYC first).
 */
public record ClientCreateRequest(
        @NotBlank String surname,
        @NotBlank String givname,
        @NotNull LocalDate dateOfBirth,
        @NotBlank @Size(min = 1, max = 1) @ValidGender String gender,
        @NotBlank @Size(min = 1, max = 3) @ValidIdType String idType,
        @NotBlank String idNumber,
        boolean ignorePossibleMatch
) {
    public @NonNull ClientKycRequest toKycRequest() {
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

    public @NonNull Client toEntity() {
        return new Client(
                surname,
                givname,
                dateOfBirth,
                gender,
                idType,
                idNumber
        );
    }
}
