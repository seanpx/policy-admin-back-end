package com.policyadmin.client.api.dto;

/**
 * Criteria for client enquiry search. All fields are optional and support partial match.
 */
public record ClientEnquiryCriteria(String clntIdNo, String surname, String givname) {

    public boolean isAllEmpty() {
        return isBlank(clntIdNo) && isBlank(surname) && isBlank(givname);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
