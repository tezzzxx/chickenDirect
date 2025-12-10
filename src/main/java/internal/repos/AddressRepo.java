package internal.repos;

import internal.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepo extends JpaRepository<Address, Long> {
    Optional<Address> findByApartment_numberAndAddressAndZip_codeAndCity(
            String apartment_number,
            String address,
            String zip_code,
            String city
    );
}
