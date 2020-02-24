package com.nhat.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Amenity {
    private String type;
    private Set<String> options;
}
