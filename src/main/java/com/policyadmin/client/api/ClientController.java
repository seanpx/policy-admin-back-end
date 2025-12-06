package com.policyadmin.client.api;

import com.policyadmin.audit.AuditEventData;
import com.policyadmin.audit.AuditEventService;
import com.policyadmin.client.api.dto.ClientCreateRequest;
import com.policyadmin.client.api.dto.ClientCreateResponse;
import com.policyadmin.client.api.dto.ClientKycValidateRequest;
import com.policyadmin.client.api.dto.IdTypeResponse;
import com.policyadmin.client.kyc.ClientKycValidationResult;
import com.policyadmin.client.service.ClientCommandService;
import com.policyadmin.client.service.ClientCommandService.ClientCreationResult;
import com.policyadmin.client.service.IdTypeQueryService;
import com.policyadmin.logging.CorrelationIdFilter;
import com.policyadmin.logging.SafeLogging;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final ClientCommandService clientCommandService;
    private final IdTypeQueryService idTypeQueryService;
    private final AuditEventService auditEventService;

    public ClientController(ClientCommandService clientCommandService, IdTypeQueryService idTypeQueryService,
            AuditEventService auditEventService) {
        this.clientCommandService = clientCommandService;
        this.idTypeQueryService = idTypeQueryService;
        this.auditEventService = auditEventService;
    }

    @PostMapping("/kyc/validate")
    public ResponseEntity<ClientKycValidationResult> validate(@Valid @RequestBody ClientKycValidateRequest request) {
        ClientKycValidationResult result = clientCommandService.validate(toCreateRequest(request));
        log.info("client.kyc.validate",
                StructuredArguments.keyValue("correlationId", MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY)),
                StructuredArguments.keyValue("idType", request.idType()),
                StructuredArguments.keyValue("ignorePossibleMatch", request.ignorePossibleMatch()),
                StructuredArguments.keyValue("status", "OK"));
        SafeLogging.debug(log, "client.kyc.validate.payload",
                StructuredArguments.keyValue("request", SafeLogging.sanitize(Map.of(
                        "surname", request.surname(),
                        "givname", request.givname(),
                        "dob", request.dateOfBirth(),
                        "gender", request.gender(),
                        "idType", request.idType(),
                        "idNumber", request.idNumber(),
                        "ignorePossibleMatch", request.ignorePossibleMatch()
                ))),
                StructuredArguments.keyValue("response", SafeLogging.sanitize(Map.of(
                        "result", result
                )))
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/id-types")
    public ResponseEntity<List<IdTypeResponse>> listIdTypes(HttpServletRequest request) {
        List<IdTypeResponse> idTypes = idTypeQueryService.listIdTypes();
        log.info("client.id-types",
                StructuredArguments.keyValue("correlationId", MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY)),
                StructuredArguments.keyValue("count", idTypes.size()),
                StructuredArguments.keyValue("status", "OK"));
        SafeLogging.debug(log, "client.id-types.payload",
                StructuredArguments.keyValue("request", SafeLogging.sanitize(Map.of(
                        "remoteIp", request.getRemoteAddr()
                ))),
                StructuredArguments.keyValue("response", SafeLogging.sanitize(Map.of(
                        "count", idTypes.size(),
                        "items", idTypes
                )))
        );
        auditEventService.record(new AuditEventData(
                "CLIENT_ID_TYPES_VIEWED",
                "ID_TYPE_LIST",
                null,
                "USER",
                currentUserId(),
                MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY),
                request.getRemoteAddr(),
                Map.of("count", idTypes.size())
        ));
        return ResponseEntity.ok(idTypes);
    }

    @PostMapping
    public ResponseEntity<ClientCreateResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        ClientCreationResult result = clientCommandService.create(request);
        ClientCreateResponse response = new ClientCreateResponse(result.clientId(), result.validationResult());
        log.info("client.create",
                StructuredArguments.keyValue("correlationId", MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY)),
                StructuredArguments.keyValue("clientId", result.clientId()),
                StructuredArguments.keyValue("created", result.created()),
                StructuredArguments.keyValue("status", result.created() ? HttpStatus.CREATED.value() : HttpStatus.CONFLICT.value()));
        SafeLogging.debug(log, "client.create.payload",
                StructuredArguments.keyValue("request", SafeLogging.sanitize(Map.of(
                        "surname", request.surname(),
                        "givname", request.givname(),
                        "dob", request.dateOfBirth(),
                        "gender", request.gender(),
                        "idType", request.idType(),
                        "idNumber", request.idNumber(),
                        "ignorePossibleMatch", request.ignorePossibleMatch()
                ))),
                StructuredArguments.keyValue("response", SafeLogging.sanitize(Map.of(
                        "clientId", response.clientId(),
                        "validationResult", response.validationResult(),
                        "created", result.created()
                )))
        );
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

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal)) {
                return authentication.getName();
            }
        }
        return null;
    }
}
