package com.policyadmin.client.kyc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.policyadmin.client.domain.Client;
import com.policyadmin.client.repository.ClientRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientKycValidatorTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientKycValidator validator;

    private final LocalDate dob = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setUp() {
        validator = new ClientKycValidator(clientRepository);
    }

    @Test
    void okToCreateWhenNoMatches() {
        ClientKycRequest request = new ClientKycRequest("Doe", "John", dob, "M", "PAS", "123", false);

        when(clientRepository.findByClntidTypAndClntidNo("PAS", "123")).thenReturn(List.of());
        when(clientRepository.findByNameDobAndGenderIgnoreCase("Doe", "John", dob, "M")).thenReturn(List.of());

        ClientKycValidationResult result = validator.validate(request);

        assertEquals(ClientKycStatus.OK_TO_CREATE_NEW, result.status());
        assertEquals("OK", result.reasonCode());
        assertTrue(result.matchedClientIds().isEmpty());
        assertTrue(result.isOkToCreate());
    }

    @Test
    void duplicateIdSamePerson() {
        Client existing = client(1L, "Doe", "John", dob, "M", "PAS", "123");
        ClientKycRequest request = new ClientKycRequest("Doe", "John", dob, "M", "PAS", "123", false);

        when(clientRepository.findByClntidTypAndClntidNo("PAS", "123")).thenReturn(List.of(existing));

        ClientKycValidationResult result = validator.validate(request);

        assertEquals(ClientKycStatus.DUPLICATE_ID_SAME_PERSON, result.status());
        assertEquals("DUPLICATE_ID", result.reasonCode());
        assertFalse(result.isOkToCreate());
        assertEquals(List.of(1L), result.matchedClientIds());
        verify(clientRepository, never()).findByNameDobAndGenderIgnoreCase(any(), any(), any(), any());
    }

    @Test
    void possibleSamePersonIdMismatchWhenIgnoreFalse() {
        Client possible = client(2L, "Doe", "John", dob, "M", "DL", "999");
        ClientKycRequest request = new ClientKycRequest("Doe", "John", dob, "M", "PAS", "123", false);

        when(clientRepository.findByClntidTypAndClntidNo("PAS", "123")).thenReturn(List.of());
        when(clientRepository.findByNameDobAndGenderIgnoreCase("Doe", "John", dob, "M")).thenReturn(List.of(possible));

        ClientKycValidationResult result = validator.validate(request);

        assertEquals(ClientKycStatus.POSSIBLE_SAME_PERSON_ID_MISMATCH, result.status());
        assertEquals("POSSIBLE_MATCH_ID_MISMATCH", result.reasonCode());
        assertFalse(result.isOkToCreate());
        assertEquals(List.of(2L), result.matchedClientIds());
    }

    @Test
    void okToCreateWhenOverridePossibleMatch() {
        Client possible = client(3L, "Doe", "John", dob, "M", "DL", "999");
        ClientKycRequest request = new ClientKycRequest("Doe", "John", dob, "M", "PAS", "123", true);

        when(clientRepository.findByClntidTypAndClntidNo("PAS", "123")).thenReturn(List.of());
        when(clientRepository.findByNameDobAndGenderIgnoreCase("Doe", "John", dob, "M")).thenReturn(List.of(possible));

        ClientKycValidationResult result = validator.validate(request);

        assertEquals(ClientKycStatus.OK_TO_CREATE_NEW, result.status());
        assertEquals("OK_OVERRIDE_POSSIBLE_MATCH", result.reasonCode());
        assertTrue(result.isOkToCreate());
        assertEquals(List.of(3L), result.matchedClientIds());
    }

    private Client client(Long id, String surname, String givenName, LocalDate dateOfBirth, String gender, String idType, String idNumber) {
        Client client = new Client(surname, givenName, dateOfBirth, gender, idType, idNumber);
        client.setClntnum(id);
        return client;
    }
}
