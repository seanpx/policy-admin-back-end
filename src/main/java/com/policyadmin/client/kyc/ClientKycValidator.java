package com.policyadmin.client.kyc;

import com.policyadmin.client.domain.Client;
import com.policyadmin.client.repository.ClientRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientKycValidator {

    private final ClientRepository clientRepository;

    public ClientKycValidator(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public ClientKycValidationResult validate(ClientKycRequest request) {
        List<Client> idMatches = clientRepository.findByClntidTypAndClntidNo(request.idType(), request.idNumber());
        if (!idMatches.isEmpty()) {
            boolean detailsMatch = idMatches.stream().anyMatch(client -> matchesPersonDetails(client, request));
            ClientKycStatus status = detailsMatch
                    ? ClientKycStatus.DUPLICATE_ID_SAME_PERSON
                    : ClientKycStatus.DUPLICATE_ID_CONFLICTING_DETAILS;
            String reasonCode = detailsMatch ? "DUPLICATE_ID" : "ID_CONFLICT";
            String reasonMessage = detailsMatch
                    ? "Existing client with same ID and matching personal details."
                    : "Existing client with same ID but conflicting personal details.";
            return new ClientKycValidationResult(status, reasonCode, reasonMessage, extractClientIds(idMatches));
        }

        List<Client> possibleMatches = clientRepository.findByNameDobAndGenderIgnoreCase(
                request.surname(),
                request.givname(),
                request.dateOfBirth(),
                request.gender()
        );
        if (!possibleMatches.isEmpty()) {
            if (request.ignorePossibleMatch()) {
                return new ClientKycValidationResult(
                        ClientKycStatus.OK_TO_CREATE_NEW,
                        "OK_OVERRIDE_POSSIBLE_MATCH",
                        "Possible match ignored after manual verification.",
                        extractClientIds(possibleMatches)
                );
            }
            return new ClientKycValidationResult(
                    ClientKycStatus.POSSIBLE_SAME_PERSON_ID_MISMATCH,
                    "POSSIBLE_MATCH_ID_MISMATCH",
                    "Client with matching personal details but different identification found.",
                    extractClientIds(possibleMatches)
            );
        }

        return new ClientKycValidationResult(
                ClientKycStatus.OK_TO_CREATE_NEW,
                "OK",
                "No duplicates or possible matches found.",
                List.of()
        );
    }

    private boolean matchesPersonDetails(Client client, ClientKycRequest request) {
        return equalsIgnoreCase(client.getSurname(), request.surname())
                && equalsIgnoreCase(client.getGivname(), request.givname())
                && Objects.equals(client.getCltdob(), request.dateOfBirth())
                && equalsIgnoreCase(client.getCltsex(), request.gender());
    }

    private boolean equalsIgnoreCase(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return first.equalsIgnoreCase(second);
    }

    private List<Long> extractClientIds(List<Client> clients) {
        return clients.stream()
                .map(Client::getClntnum)
                .filter(Objects::nonNull)
                .toList();
    }
}
