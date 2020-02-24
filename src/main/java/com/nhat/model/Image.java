package com.nhat.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Image {
    private String type;
    private List<Link> links;
}
