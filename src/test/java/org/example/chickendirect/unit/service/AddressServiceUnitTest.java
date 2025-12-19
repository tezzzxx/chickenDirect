package org.example.chickendirect.unit.service;


import org.example.chickendirect.dtos.AddressDto;
import org.example.chickendirect.entities.Address;
import org.example.chickendirect.entities.Customer;
import org.example.chickendirect.repos.AddressRepo;
import org.example.chickendirect.repos.CustomerRepo;
import org.example.chickendirect.repos.OrderRepo;
import org.example.chickendirect.services.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AddressServiceUnitTest {

    @Mock
    private CustomerRepo customerRepo;

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private AddressRepo addressRepo;

    @InjectMocks
    private AddressService addressService;

    Customer customer;
    Address address;
    AddressDto aDto;

    @BeforeEach
    void setup(){
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setAddressList(new ArrayList<>());

        address = new Address();
        address.setAddressId(10L);
        address.setApartmentNumber("11B");
        address.setAddress("Bergveien");
        address.setZipCode("0182");
        address.setCity("Oslo");
        address.setCountry("Norway");
        address.setCustomerList(new ArrayList<>());

        aDto = new AddressDto(
                null,
                "2A",
                "Vannveien",
                "0182",
                "Oslo",
                "Norway",
                null
        );
    }

    @Nested
    class AddNewAddressTests{

        @Test
        void addNewAddress_customerIdExists_success(){
            when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
            when(addressRepo.save(any(Address.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Address result = addressService.addNewAddress(1L, aDto);

            verify(addressRepo).save(any(Address.class));
            assertThat(customer.getAddressList()).contains(result);
            assertThat(result.getCustomerList()).contains(customer);

        }

        @Test
        void addNewAddress_customerIdNotFound(){
            when(customerRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> addressService.addNewAddress(1L, aDto)
            );

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(addressRepo, never()).save(any());
        }
    }

    @Nested
    class UpdateAddressTests{

        @Test
        void updateAddress_customerIdNotFound(){
            when(customerRepo.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> addressService.updateAddress(1L, 10L, aDto)
            );

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void updateAddress_whenAddressIdNotFound(){
            when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
            when(addressRepo.findById(10L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> addressService.updateAddress(1L, 10L, aDto)
            );

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void updateAddress_whenAddressNotBelongToCustomerId(){

            Long customerId = customer.getCustomerId();
            Long addressId = address.getAddressId();

            Customer otherCustomer = new Customer();
            address.setCustomerList(List.of(otherCustomer));

            when(customerRepo.findById(customerId)).thenReturn(Optional.of(customer));
            when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> addressService.updateAddress(customerId, addressId, aDto)
            );

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getReason()).isEqualTo("Address does not belong to this customer");

        }

        @Test
        void updateAddress_semiDoneDto_updatesFieldsCorrectly(){
            address.getCustomerList().add(customer);

            when(customerRepo.findById(1L)).thenReturn(Optional.of(customer));
            when(addressRepo.findById(10L)).thenReturn(Optional.of(address));
            when(addressRepo.save(any(Address.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            AddressDto semiDoneDto = new AddressDto(
                    null,
                    null,
                    null,
                    null,
                    "Milano",
                    null,
                    null
            );

            Address updated = addressService.updateAddress(1L, 10L, semiDoneDto);

            assertThat(updated.getAddress()).isEqualTo("Bergveien");
            assertThat(updated.getCity()).isEqualTo("Milano");
            assertThat(updated.getCountry()).isEqualTo("Norway");
            assertThat(updated.getApartmentNumber()).isEqualTo("11B");
            assertThat(updated.getZipCode()).isEqualTo("0182");

            verify(addressRepo).save(address);

        }

        @Nested
        class FindAddressByIdTests{

            @Test
            void findAddressById_addressExists_success(){
                when(addressRepo.findById(10L)).thenReturn(Optional.of(address));

                Address result = addressService.findAddressById(10L);

                assertThat(result).isEqualTo(address);

            }

            @Test
            void findAddressById_addressIdNotFound(){
                when(addressRepo.findById(10L)).thenReturn(Optional.empty());

                ResponseStatusException ex = assertThrows(
                        ResponseStatusException.class,
                        () -> addressService.findAddressById(10L)
                );

                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            }
        }

        @Nested
        class DeleteAddressByIdTests{

            @Test
            void deleteAddressById_addressNotUsedInOrders_success(){
                address.getCustomerList().add(customer);
                customer.getAddressList().add(address);

                when(addressRepo.findById(10L)).thenReturn(Optional.of(address));
                when(orderRepo.existsByAddress_AddressId(10L)).thenReturn(false);

                addressService.deleteAddressById(10L);

                assertThat(customer.getAddressList()).doesNotContain(address);
                verify(addressRepo).delete(address);
            }

            @Test
            void deleteAddressById_addressNotFound(){
                when(addressRepo.findById(10L)).thenReturn(Optional.empty());

                ResponseStatusException ex = assertThrows(
                        ResponseStatusException.class,
                        () -> addressService.deleteAddressById(10L)
                );

                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            }

            @Test
            void deleteAddressById_addressUsedInOrders(){
                when(addressRepo.findById(10L)).thenReturn(Optional.of(address));
                when(orderRepo.existsByAddress_AddressId(10L)).thenReturn(true);

                ResponseStatusException ex = assertThrows(
                        ResponseStatusException.class,
                        () -> addressService.deleteAddressById(10L)
                );

                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                verify(addressRepo, never()).delete(any());
            }

            @Test
            void deleteAddressById_removesAddressFromAllAssociatedCustomers(){
                Customer c1 = new Customer();
                Customer c2 = new Customer();

                c1.setAddressList(new ArrayList<>(List.of(address)));
                c2.setAddressList(new ArrayList<>(List.of(address)));

                address.setCustomerList(new ArrayList<>(List.of(c1, c2)));

                when(addressRepo.findById(10L)).thenReturn(Optional.of(address));
                when(orderRepo.existsByAddress_AddressId(10L)).thenReturn(false);

                addressService.deleteAddressById(10L);

                assertThat(c1.getAddressList()).isEmpty();
                assertThat(c2.getAddressList()).isEmpty();

            }
        }

    }
}
