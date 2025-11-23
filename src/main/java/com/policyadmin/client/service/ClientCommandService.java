package com.policyadmin.client.service;

import com.policyadmin.client.api.dto.ClientCreateRequest;
import com.policyadmin.client.domain.Client;
import com.policyadmin.client.kyc.ClientKycValidationResult;
import com.policyadmin.client.kyc.ClientKycValidator;
import com.policyadmin.client.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that orchestrates KYC validation and client creation.
 */
@Service
public class ClientCommandService {

    private final ClientRepository clientRepository;
    private final ClientKycValidator clientKycValidator;

    public ClientCommandService(ClientRepository clientRepository, ClientKycValidator clientKycValidator) {
        this.clientRepository = clientRepository;
        this.clientKycValidator = clientKycValidator;
    }

    @Transactional(readOnly = true)
    public ClientKycValidationResult validate(ClientCreateRequest request) {
        return clientKycValidator.validate(request.toKycRequest());
    }

    @Transactional
    public ClientCreationResult create(ClientCreateRequest request) {
        ClientKycValidationResult validation = clientKycValidator.validate(request.toKycRequest());
        if (!validation.isOkToCreate()) {
            return ClientCreationResult.rejected(validation);
        }
        Client saved = clientRepository.save(request.toEntity());
        return ClientCreationResult.created(saved.getClntnum(), validation);
    }

    public record ClientCreationResult(Long clientId, ClientKycValidationResult validationResult) {
        public boolean created() {
            return validationResult != null && validationResult.isOkToCreate();
        }

        public static ClientCreationResult created(Long clientId, ClientKycValidationResult validationResult) {
            return new ClientCreationResult(clientId, validationResult);
        }

        public static ClientCreationResult rejected(ClientKycValidationResult validationResult) {
            return new ClientCreationResult(null, validationResult);
        }
    }
}
