package com.nhat.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Hotel {
    private String hotelId;
    private Integer destinationId;
    private String name;
    private Location location;
    private String description;
    private List<Amenity> amenities;
    private List<Image> images;
    private List<String> booking_conditions;
}
