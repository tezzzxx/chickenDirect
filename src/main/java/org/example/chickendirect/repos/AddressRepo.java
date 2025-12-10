package org.example.chickendirect.repos;

import org.example.chickendirect.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepo extends JpaRepository<Address, Long> {
    Optional<Address> findByApartmentNumberAndAddressAndZipCodeAndCity(
            String apartmentNumber,
            String address,
            String zipCode,
            String city
    );
}
