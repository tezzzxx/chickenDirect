package org.example.chickendirect.integration.repo;


import org.example.chickendirect.BaseIntegrationTest;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.repos.AddressRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Optional;
@ActiveProfiles("integration-test")
public class AddressRepoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AddressRepo addressRepo;

    @Test
    void testFindByApartmentNumberAndAddressAndZipCodeAndCity(){
        Address address = new Address("12B", "Skogsveien", "0182", "Oslo");
        addressRepo.save(address);

        Optional<Address> found = addressRepo.findByApartmentNumberAndAddressAndZipCodeAndCity("12B", "Skogsveien", "0182", "Oslo");

        assertThat(found).isPresent();
        assertThat(found.get().getCity()).isEqualTo("Oslo");
    }

}
