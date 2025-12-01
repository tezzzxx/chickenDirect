package internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import internal.entities.Customer;

import jakarta.persistence.*;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @SequenceGenerator(name = "address_seq", sequenceName = "address_seq", allocationSize = 1)
    private long address_id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String apartment_Number;
    private String address;
    private String zip_code;
    private String city;
    private String country;

    public Address(Customer customer, String apartment_Number, String address, String zip_code, String city, String country) {
        this.customer = customer;
        this.apartment_Number = apartment_Number;
        this.address = address;
        this.zip_code = zip_code;
        this.city = city;
        this.country = country;
    }

    public Address() {
    }

    public long getAddress_id() {
        return address_id;
    }

    public void setAddress_id(long address_id) {
        this.address_id = address_id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getApartment_Number() {
        return apartment_Number;
    }

    public void setApartment_Number(String apartment_Number) {
        this.apartment_Number = apartment_Number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
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
                "address_id=" + address_id +
                ", customer=" + customer +
                ", apartment_Number='" + apartment_Number + '\'' +
                ", address='" + address + '\'' +
                ", zip_code='" + zip_code + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
