package com.policyadmin.client.service;

import com.policyadmin.client.api.dto.ClientEnquiryCriteria;
import com.policyadmin.client.api.dto.ClientSummaryDto;
import com.policyadmin.client.domain.Client;
import com.policyadmin.client.repository.ClientRepository;
import com.policyadmin.client.repository.ClientSpecifications;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientEnquiryService {

    private final ClientRepository clientRepository;

    public ClientEnquiryService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClientSummaryDto> enquiry(ClientEnquiryCriteria criteria, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        ClientEnquiryCriteria safeCriteria = criteria == null
                ? new ClientEnquiryCriteria(null, null, null)
                : criteria;
        Specification<Client> spec = ClientSpecifications.byEnquiryCriteria(safeCriteria);
        Page<Client> results = clientRepository.findAll(spec, pageable);
        return results.map(this::toSummaryDto);
    }

    private ClientSummaryDto toSummaryDto(Client client) {
        return new ClientSummaryDto(
                client.getClntnum(),
                client.getClntidNo(),
                client.getSurname(),
                client.getGivname()
        );
    }
}
