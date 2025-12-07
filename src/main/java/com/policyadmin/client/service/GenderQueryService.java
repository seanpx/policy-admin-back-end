package com.policyadmin.client.service;

import com.policyadmin.client.api.dto.GenderResponse;
import com.policyadmin.config.ClientGenderProperties;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GenderQueryService {

    private final ClientGenderProperties properties;

    public GenderQueryService(ClientGenderProperties properties) {
        this.properties = properties;
    }

    public List<GenderResponse> listGenders() {
        return properties.getGenders().stream()
                .map(gender -> new GenderResponse(gender.getCode(), gender.getDescription()))
                .toList();
    }
}
