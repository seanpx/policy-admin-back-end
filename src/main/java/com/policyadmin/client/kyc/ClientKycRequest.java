package com.policyadmin.client.kyc;

import java.time.LocalDate;

public record ClientKycRequest(
        String surname,
        String givname,
        LocalDate dateOfBirth,
        String gender,
        String idType,
        String idNumber,
        boolean ignorePossibleMatch
) { }
