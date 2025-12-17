package org.example.chickendirect.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @SequenceGenerator(name = "address_seq", sequenceName = "address_seq", allocationSize = 1)

    @Column(name = "address_id")
    private long addressId;
    @Column(name = "apartment_number")
    private String apartmentNumber;
    private String address;
    @Column(name = "zip_code")
    private String zipCode;
    private String city;
    private String country;

    @ManyToMany (mappedBy = "addressList")
    @JsonIgnoreProperties("addressList")
    private List<Customer> customerList = new ArrayList<>();

    public Address(String apartmentNumber, String address, String zipCode, String city, String country, List<Customer> customerList) {
        this.apartmentNumber = apartmentNumber;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
        this.customerList = customerList;
    }

    public Address(List<Customer> customerList, String apartmentNumber, String address, String zipCode, String city, String country) {
        this.apartmentNumber = apartmentNumber;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
    }

    public Address(String apartmentNumber, String address, String zipCode, String city) {
        this.apartmentNumber = apartmentNumber;
        this.address = address;
        this.zipCode = zipCode;
        this.city = city;
    }

    public Address() {
    }

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long address_id) {
        this.addressId = address_id;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customers) {
        this.customerList = customers;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartment_number) {
        this.apartmentNumber = apartment_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zip_code) {
        this.zipCode = zip_code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address{" +
                "address_id=" + addressId +
                ", apartment_number='" + apartmentNumber + '\'' +
                ", address='" + address + '\'' +
                ", zip_code='" + zipCode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", customerList=" + customerList +
                '}';
    }
}
