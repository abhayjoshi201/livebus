package livebus.admin.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.util.UUID;

@Entity
@Table(name = "stops")
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private String stopName;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(nullable = false)
    private Integer stopSequence; 

    public Stop() {}

    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public Integer getStopSequence() { return stopSequence; }
    public void setStopSequence(Integer stopSequence) { this.stopSequence = stopSequence; }
}