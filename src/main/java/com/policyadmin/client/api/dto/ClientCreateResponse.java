package com.policyadmin.client.api.dto;

import com.policyadmin.client.kyc.ClientKycValidationResult;

public record ClientCreateResponse(Long clientId, ClientKycValidationResult validationResult) {
}
