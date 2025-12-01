package com.policyadmin.client.service;

import com.policyadmin.config.ClientIdTypeProperties;
import com.policyadmin.client.api.dto.IdTypeResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IdTypeQueryService {

    private final ClientIdTypeProperties properties;

    public IdTypeQueryService(ClientIdTypeProperties properties) {
        this.properties = properties;
    }

    public List<IdTypeResponse> listIdTypes() {
        return properties.getIdTypes().stream()
                .map(type -> new IdTypeResponse(type.getCode(), type.getDescription()))
                .toList();
    }
}
