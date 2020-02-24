package com.nhat.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private Double lat;
    private Double lng;
    private String address;
    private String city;
    private String country;
}
