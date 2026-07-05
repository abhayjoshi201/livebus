package livebus.admin.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "buses")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String busType; 

    public Bus() {}

    public Bus(String licensePlate, Integer capacity, String busType) {
        this.licensePlate = licensePlate;
        this.capacity = capacity;
        this.busType = busType;
    }

    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getBusType() { return busType; }
    public void setBusType(String busType) { this.busType = busType; }
}