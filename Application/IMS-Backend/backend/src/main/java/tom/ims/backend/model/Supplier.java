package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplierID", nullable = false)
    @JsonProperty("supplierid")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    @JsonProperty("name")
    private String name;

    @Column(name = "address1", nullable = false, length = 50)
    @JsonProperty("address1")
    private String address1;

    @Column(name = "address2", length = 50)
    @JsonProperty("address2")
    private String address2;

    @Column(name = "city", nullable = false, length = 50)
    @JsonProperty("city")
    private String city;

    @Column(name = "country", nullable = false, length = 50)
    @JsonProperty("country")
    private String country;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "province", nullable = false)
    @JsonProperty("province")
    private Province province;

    @Column(name = "postalcode", nullable = false, length = 11)
    @JsonProperty("postalcode")
    private String postalcode;

    @Column(name = "phone", nullable = false, length = 14)
    @JsonProperty("phone")
    private String phone;

    @Column(name = "contact", length = 100)
    @JsonProperty("contact")
    private String contact;

    @Column(name = "notes")
    @JsonProperty("notes")
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    @JsonProperty("active")
    private Byte active;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
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

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Byte getActive() {
        return active;
    }

    public void setActive(Byte active) {
        this.active = active;
    }

}