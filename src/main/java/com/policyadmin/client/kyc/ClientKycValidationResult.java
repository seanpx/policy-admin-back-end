package com.policyadmin.client.kyc;

import java.util.List;

public record ClientKycValidationResult(
        ClientKycStatus status,
        String reasonCode,
        String reasonMessage,
        List<Long> matchedClientIds
) {

    public ClientKycValidationResult {
        matchedClientIds = matchedClientIds == null ? List.of() : List.copyOf(matchedClientIds);
    }

    public boolean isOkToCreate() {
        return ClientKycStatus.OK_TO_CREATE_NEW.equals(status);
    }
}
