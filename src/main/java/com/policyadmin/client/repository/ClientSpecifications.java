package com.policyadmin.client.repository;

import com.policyadmin.client.api.dto.ClientEnquiryCriteria;
import com.policyadmin.client.domain.Client;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification builder for client queries.
 */
public final class ClientSpecifications {

    private ClientSpecifications() {
    }

    public static Specification<Client> byEnquiryCriteria(ClientEnquiryCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addLikeIgnoreCase(predicates, root.get("clntidNo"), criteria.clntIdNo(), cb);
            addLikeIgnoreCase(predicates, root.get("surname"), criteria.surname(), cb);
            addLikeIgnoreCase(predicates, root.get("givname"), criteria.givname(), cb);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addLikeIgnoreCase(List<Predicate> predicates, Path<String> path, String rawValue,
                                          CriteriaBuilder cb) {
        String value = normalize(rawValue);
        if (value != null) {
            predicates.add(cb.like(cb.lower(path), "%" + value + "%"));
        }
    }

    private static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }
}
