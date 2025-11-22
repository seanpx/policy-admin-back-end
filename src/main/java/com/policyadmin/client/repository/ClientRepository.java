package com.policyadmin.client.repository;

import com.policyadmin.client.domain.Client;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByClntidTypAndClntidNo(String idType, String idNumber);

    @Query("""
            select c from Client c
            where lower(c.surname) = lower(:surname)
              and lower(c.givname) = lower(:givenName)
              and c.cltdob = :dateOfBirth
              and c.cltsex = :gender
            """)
    List<Client> findByNameDobAndGenderIgnoreCase(@Param("surname") String surname,
                                                  @Param("givenName") String givenName,
                                                  @Param("dateOfBirth") LocalDate dateOfBirth,
                                                  @Param("gender") String gender);
}
