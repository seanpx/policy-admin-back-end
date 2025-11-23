package com.policyadmin.client.api;

import com.policyadmin.client.api.dto.ClientCreateRequest;
import com.policyadmin.client.api.dto.ClientCreateResponse;
import com.policyadmin.client.api.dto.ClientKycValidateRequest;
import com.policyadmin.client.kyc.ClientKycValidationResult;
import com.policyadmin.client.service.ClientCommandService;
import com.policyadmin.client.service.ClientCommandService.ClientCreationResult;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientCommandService clientCommandService;

    public ClientController(ClientCommandService clientCommandService) {
        this.clientCommandService = clientCommandService;
    }

    @PostMapping("/kyc/validate")
    public ResponseEntity<ClientKycValidationResult> validate(@Valid @RequestBody ClientKycValidateRequest request) {
        ClientKycValidationResult result = clientCommandService.validate(toCreateRequest(request));
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ClientCreateResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        ClientCreationResult result = clientCommandService.create(request);
        ClientCreateResponse response = new ClientCreateResponse(result.clientId(), result.validationResult());
        if (result.created()) {
            URI location = Objects.requireNonNull(URI.create("/api/clients/" + result.clientId()));
            return ResponseEntity.created(location).body(response);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private ClientCreateRequest toCreateRequest(ClientKycValidateRequest request) {
        return new ClientCreateRequest(
                request.surname(),
                request.givname(),
                request.dateOfBirth(),
                request.gender(),
                request.idType(),
                request.idNumber(),
                request.ignorePossibleMatch()
        );
    }
}
