package com.policyadmin.client.kyc;

public enum ClientKycStatus {
    OK_TO_CREATE_NEW,
    DUPLICATE_ID_SAME_PERSON,
    DUPLICATE_ID_CONFLICTING_DETAILS,
    POSSIBLE_SAME_PERSON_ID_MISMATCH
}
