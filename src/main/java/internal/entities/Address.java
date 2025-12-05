package internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @SequenceGenerator(name = "address_seq", sequenceName = "address_seq", allocationSize = 1)

    private long address_id;
    private String apartment_number;
    private String address;
    private String zip_code;
    private String city;
    private String country;

    @ManyToMany (mappedBy = "address")
    @JsonIgnoreProperties("address")
    private List<Customer> customerList;

    public Address(String apartment_number, String address, String zip_code, String city, String country, List<Customer> customerList) {
        this.apartment_number = apartment_number;
        this.address = address;
        this.zip_code = zip_code;
        this.city = city;
        this.country = country;
        this.customerList = customerList;
    }

    public Address(List<Customer> customerList, String apartment_number, String address, String zip_code, String city, String country) {
        this.apartment_number = apartment_number;
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

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customers) {
        this.customerList = customers;
    }

    public String getApartment_number() {
        return apartment_number;
    }

    public void setApartment_number(String apartment_number) {
        this.apartment_number = apartment_number;
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
                ", apartment_number='" + apartment_number + '\'' +
                ", address='" + address + '\'' +
                ", zip_code='" + zip_code + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", customerList=" + customerList +
                '}';
    }
}
