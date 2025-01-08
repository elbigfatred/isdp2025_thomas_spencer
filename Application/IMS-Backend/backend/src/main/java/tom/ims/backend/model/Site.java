package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "siteID", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "siteName", nullable = false, length = 50)
    @JsonProperty("siteName")
    private String siteName;

    @Column(name = "address", nullable = false, length = 50)
    @JsonProperty("address")
    private String address;

    @Column(name = "address2", length = 50)
    @JsonProperty("address2")
    private String address2;

    @Column(name = "city", nullable = false, length = 50)
    @JsonProperty("city")
    private String city;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "provinceID", nullable = false, referencedColumnName = "provinceID")
    @JsonProperty("province")
    private Province provinceID;

    @Column(name = "country", nullable = false, length = 50)
    @JsonProperty("country")
    private String country;

    @Column(name = "postalCode", nullable = false, length = 14)
    @JsonProperty("postalCode")
    private String postalCode;

    @Column(name = "phone", nullable = false, length = 14)
    @JsonProperty("phone")
    private String phone;

    @Column(name = "dayOfWeek", length = 50)
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;

    @Column(name = "distanceFromWH", nullable = false)
    @JsonProperty("distanceFromWH")
    private Integer distanceFromWH;

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

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Province getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(Province provinceID) {
        this.provinceID = provinceID;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDistanceFromWH() {
        return distanceFromWH;
    }

    public void setDistanceFromWH(Integer distanceFromWH) {
        this.distanceFromWH = distanceFromWH;
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

    // toString Method
    @Override
    public String toString() {
        return "Site{" +
                "id=" + id +
                ", siteName='" + siteName + '\'' +
                ", address='" + address + '\'' +
                ", address2='" + (address2 != null ? address2 : "null") + '\'' +
                ", city='" + city + '\'' +
                ", provinceID=" + (provinceID != null ? provinceID.getProvinceID() : "null") +
                ", country='" + country + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", phone='" + phone + '\'' +
                ", dayOfWeek='" + (dayOfWeek != null ? dayOfWeek : "null") + '\'' +
                ", distanceFromWH=" + distanceFromWH +
                ", notes='" + (notes != null ? notes : "null") + '\'' +
                ", active=" + active +
                '}';
    }
}