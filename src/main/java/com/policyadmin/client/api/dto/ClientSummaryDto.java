package com.policyadmin.client.api.dto;

/**
 * Lightweight projection returned by the client enquiry endpoint.
 */
public record ClientSummaryDto(Long clntnum, String clntIdNo, String surname, String givname) {
}
