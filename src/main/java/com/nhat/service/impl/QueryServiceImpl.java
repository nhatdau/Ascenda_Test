package com.nhat.service.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.nhat.model.Amenity;
import com.nhat.model.Hotel;
import com.nhat.model.Image;
import com.nhat.model.Link;
import com.nhat.model.Location;
import com.nhat.service.QueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class QueryServiceImpl implements QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    public static final String DESTINATION_ID = "destinationId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String LOCATION_ADDRESS = "location.address";
    public static final String LOCATION_CITY = "location.city";
    public static final String LOCATION_COUNTRY = "location.country";
    public static final String LOCATION_LAT = "location.lat";
    public static final String LOCATION_LNG = "location.lng";
    public static final String AMENITY_TYPE = "amenity.type";
    public static final String AMENITY_OPTIONS = "amenity.options";
    public static final String IMAGE_TYPE = "image.type";
    public static final String IMAGE_LINK_LINK = "image.link.link";
    public static final String IMAGE_LINK_DESCRIPTION = "image.link.description";
    public static final String BOOKING_CONDITIONS = "booking_conditions";
    public static final String DOT_REGEX = "\\.";
    public static final String SEMICOLON_PREFIX = ":";

    @Value("#{${hotelId.table.map}}")
    private Map<String, String> hotelIdTableMap;
    @Value("#{${amenity.map}}")
    private Map<String, String> amenityMap;
    private MongoTemplate mongoTemplate;
    private Environment environment;

    public QueryServiceImpl(MongoTemplate mongoTemplate, Environment environment) {
        this.mongoTemplate = mongoTemplate;
        this.environment = environment;
    }

    @Override
    public List<Hotel> searchHotels(List<String> hotelIds, Integer destinationId) {
        Assert.isTrue(!CollectionUtils.isEmpty(hotelIds) || Objects.nonNull(destinationId),
                "Must input list of hotel Ids or a destination Id to search hotels");
        List<Hotel> hotels = new ArrayList<>();

        hotelIdTableMap.forEach((hotelIdName, table) -> {
            String fieldMap = environment.getProperty(table + ".field.map");
            if (Objects.nonNull(fieldMap)) {
                try {
                    JsonNode fieldsNode = new ObjectMapper().readTree(fieldMap);
                    String destinationFieldName = fieldsNode.get(DESTINATION_ID)
                            .asText();
                    List<Document> documents;
                    if (CollectionUtils.isEmpty(hotelIds)) {
                        documents = mongoTemplate
                                .find(query(where(destinationFieldName)
                                        .is(destinationId)), Document.class, table);
                    } else if (Objects.isNull(destinationId)) {
                        documents = mongoTemplate
                                .find(query(where(hotelIdName).in(hotelIds)),
                                        Document.class, table);
                    } else {
                        documents = mongoTemplate.find(query(
                                where(hotelIdName).in(hotelIds)
                                        .and(destinationFieldName)
                                        .is(destinationId)),
                                Document.class, table);
                    }
                    documents.forEach(document -> {
                        Optional<Hotel> optionalHotel = hotels.stream()
                                .filter(hotel -> hotel.getHotelId()
                                        .equals(document.getString(hotelIdName))).findFirst();
                        // if existed hotel in the list has same Id with document, we merge data from document with existed hotel. Otherwise, create new hotel data in the list
                        if (optionalHotel.isPresent()) {
                            Hotel currentHotel = optionalHotel.get();
                            mergeWithCurrentHotel(fieldsNode, document, currentHotel);
                        } else {
                            Hotel hotel = populateNewHotel(hotelIdName, fieldsNode,
                                    destinationFieldName, document);
                            hotels.add(hotel);
                        }
                    });
                } catch (JsonProcessingException e) {
                    logger.error("Error in processing data in table: " + table, e);
                }
            }
        });
        return hotels;
    }

    protected void mergeWithCurrentHotel(JsonNode fieldsNode, Document document,
            Hotel currentHotel) {
        String name = StringUtils.isEmpty(fieldsNode.get(NAME).asText()) ? null
                : document.getString(fieldsNode.get(NAME).asText());
        if (StringUtils.isEmpty(currentHotel.getName()) || (
                Objects.nonNull(name) && name.length() > currentHotel.getName()
                        .length())) {
            currentHotel.setName(name);
        }
        String description =
                StringUtils.isEmpty(fieldsNode.get(DESCRIPTION).asText()) ? null
                        : document.getString(
                                fieldsNode.get(DESCRIPTION).asText());
        if (StringUtils.isEmpty(currentHotel.getDescription()) || (
                Objects.nonNull(description)
                        && description.length() > currentHotel.getDescription()
                        .length())) {
            currentHotel.setDescription(description);
        }
        mergeWithCurrentLocation(fieldsNode, document, currentHotel);
        mergeWithCurrentAmenities(fieldsNode, document, currentHotel);
        mergeWithCurrentImages(fieldsNode, document, currentHotel);
        List<String> bookingConditions = StringUtils
                .isEmpty(fieldsNode.get(BOOKING_CONDITIONS).asText()) ? null
                : document
                        .getList(fieldsNode.get(BOOKING_CONDITIONS).asText(),
                                String.class);
        if (CollectionUtils.isEmpty(currentHotel.getBooking_conditions())
                || !CollectionUtils.isEmpty(bookingConditions)
                && bookingConditions.size() > currentHotel.getBooking_conditions().size()) {
            currentHotel.setBooking_conditions(bookingConditions);
        }
    }

    private void mergeWithCurrentImages(JsonNode fieldsNode, Document document,
            Hotel currentHotel) {
        List<Image> currentImages = currentHotel.getImages();
        String imageTypeField = fieldsNode.get(IMAGE_TYPE).asText();
        if (imageTypeField.startsWith(SEMICOLON_PREFIX)) {
            Document documentImages = document
                    .get(imageTypeField.substring(1), Document.class);
            documentImages.keySet().forEach(key -> {
                Optional<Image> optionalImage = currentImages.stream()
                        .filter(image -> image.getType().equals(key)).findFirst();
                String imageLinkField = fieldsNode.get(IMAGE_LINK_LINK)
                        .asText();
                String linkDescriptionField = fieldsNode.get(IMAGE_LINK_DESCRIPTION)
                        .asText();
                if (optionalImage.isPresent()) {
                    if (!StringUtils.isEmpty(imageLinkField) && imageLinkField
                            .startsWith(SEMICOLON_PREFIX)) {
                        documentImages.getList(key, Document.class)
                                .forEach(documentLink -> {
                                    if (!optionalImage.get().getLinks().stream()
                                            .anyMatch(link -> link.getLink().equals(documentLink
                                                    .getString(
                                                            imageLinkField.split(DOT_REGEX)[1])))) {
                                        Link link = new Link();
                                        link.setLink(documentLink
                                                .getString(imageLinkField.split(DOT_REGEX)[1]));
                                        link.setDescription(StringUtils.isEmpty(
                                                linkDescriptionField) ? null
                                                : documentLink.getString(
                                                        linkDescriptionField
                                                                .split(DOT_REGEX)[1]));
                                        optionalImage.get().getLinks().add(link);
                                    }
                                });
                    }
                } else {
                    Image image = new Image();
                    image.setType(key);
                    List<Link> links = new ArrayList<>();
                    if (!StringUtils.isEmpty(imageLinkField) && imageLinkField
                            .startsWith(SEMICOLON_PREFIX)) {
                        documentImages.getList(key, Document.class)
                                .forEach(documentLink -> {
                                    Link link = new Link();
                                    link.setLink(documentLink
                                            .getString(imageLinkField.split(DOT_REGEX)[1]));
                                    link.setDescription(StringUtils.isEmpty(
                                            linkDescriptionField) ? null
                                            : documentLink.getString(
                                                    linkDescriptionField
                                                            .split(DOT_REGEX)[1]));
                                    links.add(link);
                                });
                    }
                    image.setLinks(links);
                    currentImages.add(image);
                }
            });
        }
    }

    private void mergeWithCurrentAmenities(JsonNode fieldsNode, Document document,
            Hotel currentHotel) {
        List<Amenity> currentAmenities = currentHotel.getAmenities();
        String amenityTypeField = fieldsNode.get(AMENITY_TYPE).asText();
        if (StringUtils.isEmpty(amenityTypeField) && !StringUtils
                .isEmpty(fieldsNode.get(AMENITY_OPTIONS).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(AMENITY_OPTIONS).asText());
            if (Objects.nonNull(object)) {
                List<String> options = (List<String>) object;
                options.forEach(option -> {
                    for (Entry<String, String> entry : amenityMap.entrySet()) {
                        if (entry.getValue().contains(option.toLowerCase())) {
                            Optional<Amenity> optionalAmenity = currentAmenities
                                    .stream()
                                    .filter(amenity -> amenity.getType()
                                            .equals(entry.getKey()))
                                    .findFirst();
                            if (optionalAmenity.isPresent()) {
                                if (!optionalAmenity.get().getOptions().stream()
                                        .anyMatch(option2 -> option2
                                                .contains(option.toLowerCase()))) {
                                    optionalAmenity.get().getOptions().add(option.toLowerCase());
                                }
                            } else {
                                Set<String> options2 = new HashSet<>();
                                options2.add(option.toLowerCase());
                                currentAmenities.add(new Amenity(entry.getKey(),
                                        options2));
                            }
                            break;
                        }
                    }
                });
            }
        } else if (amenityTypeField.startsWith(SEMICOLON_PREFIX)) {
            Document documentAmenities = document
                    .get(amenityTypeField.substring(1), Document.class);
            documentAmenities.keySet().forEach(key -> {
                Optional<Amenity> optionalAmenity = currentAmenities.stream()
                        .filter(amenity -> amenity.getType().equals(key))
                        .findFirst();
                if (optionalAmenity.isPresent()) {
                    ((List<String>) documentAmenities.get(key)).forEach(option -> {
                        if (!optionalAmenity.get().getOptions().stream()
                                .anyMatch(option2 -> option2
                                        .contains(option.toLowerCase()))) {
                            optionalAmenity.get().getOptions().add(option.toLowerCase());
                        }
                    });
                } else {
                    Amenity amenity = new Amenity(key,
                            ((List<String>) documentAmenities.get(key)).stream()
                                    .map(s -> s.toLowerCase()).collect(
                                    Collectors.toSet()));
                    currentAmenities.add(amenity);
                }
            });
        }
    }

    private void mergeWithCurrentLocation(JsonNode fieldsNode, Document document,
            Hotel currentHotel) {
        Location currentLocation = currentHotel.getLocation();
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_ADDRESS).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_ADDRESS).asText());
            if (StringUtils.isEmpty(currentLocation.getAddress())) {
                currentLocation.setAddress(Objects.nonNull(object) ? object.toString() : null);
            } else if (Objects.nonNull(object) && object.toString().length() > currentLocation
                    .getAddress().length()) {
                currentLocation.setAddress(object.toString());
            }
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_CITY).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_CITY).asText());
            if (StringUtils.isEmpty(currentLocation.getCity())) {
                currentLocation.setCity(Objects.nonNull(object) ? object.toString() : null);
            } else if (Objects.nonNull(object) && object.toString().length() > currentLocation
                    .getCity().length()) {
                currentLocation.setCity(object.toString());
            }
        }

        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_COUNTRY).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_COUNTRY).asText());
            if (StringUtils.isEmpty(currentLocation.getCountry())) {
                currentLocation.setCountry(Objects.nonNull(object) ? object.toString() : null);
            } else if (Objects.nonNull(object) && object.toString().length() > currentLocation
                    .getCountry().length()) {
                currentLocation.setCountry(object.toString());
            }
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_LAT).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_LAT).asText());
            if (Objects.isNull(currentLocation.getLat())) {
                currentLocation.setLat(Objects.nonNull(object) ? (Double) object : null);
            } else if (Objects.nonNull(object) && object.toString().length() > currentLocation
                    .getLat().toString().length()) {
                currentLocation.setLat((Double) object);
            }
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_LNG).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_LNG).asText());
            if (Objects.isNull(currentLocation.getLng())) {
                currentLocation.setLng(Objects.nonNull(object) ? (Double) object : null);
            } else if (Objects.nonNull(object) && object.toString().length() > currentLocation
                    .getLng().toString().length()) {
                currentLocation.setLng((Double) object);
            }
        }
    }

    protected Hotel populateNewHotel(String hotelIdName, JsonNode fieldsNode,
            String destinationFieldName, Document document) {
        Hotel hotel = new Hotel();
        hotel.setHotelId(document.getString(hotelIdName));
        hotel.setDestinationId(document.getInteger(destinationFieldName));
        hotel.setName(StringUtils.isEmpty(fieldsNode.get(NAME).asText()) ? null
                : document.getString(fieldsNode.get(NAME).asText()));
        hotel.setDescription(
                StringUtils.isEmpty(fieldsNode.get(DESCRIPTION).asText()) ? null
                        : document.getString(
                                fieldsNode.get(DESCRIPTION).asText()));
        populateNewLocation(fieldsNode, document, hotel);
        populateNewAmenities(fieldsNode, document, hotel);
        populateNewImages(fieldsNode, document, hotel);
        hotel.setBooking_conditions(StringUtils
                .isEmpty(fieldsNode.get(BOOKING_CONDITIONS).asText()) ? null
                : document
                        .getList(fieldsNode.get(BOOKING_CONDITIONS).asText(),
                                String.class));
        return hotel;
    }

    private void populateNewImages(JsonNode fieldsNode, Document document, Hotel hotel) {
        List<Image> images = new ArrayList<>();
        String imageTypeField = fieldsNode.get(IMAGE_TYPE).asText();
        if (imageTypeField.startsWith(SEMICOLON_PREFIX)) {
            Document documentImages = document
                    .get(imageTypeField.substring(1), Document.class);
            documentImages.keySet().forEach(key -> {
                Image image = new Image();
                image.setType(key);
                List<Link> links = new ArrayList<>();
                String imageLinkField = fieldsNode.get(IMAGE_LINK_LINK)
                        .asText();
                if (!StringUtils.isEmpty(imageLinkField) && imageLinkField
                        .startsWith(SEMICOLON_PREFIX)) {
                    documentImages.getList(key, Document.class)
                            .forEach(documentLink -> {
                                Link link = new Link();
                                link.setLink(documentLink
                                        .getString(imageLinkField.split(DOT_REGEX)[1]));
                                String linkDescriptionField = fieldsNode.get(IMAGE_LINK_DESCRIPTION)
                                        .asText();
                                link.setDescription(StringUtils.isEmpty(
                                        linkDescriptionField) ? null
                                        : documentLink.getString(
                                                linkDescriptionField
                                                        .split(DOT_REGEX)[1]));
                                links.add(link);
                            });
                }
                image.setLinks(links);
                images.add(image);
            });
        }
        hotel.setImages(images);
    }

    private void populateNewAmenities(JsonNode fieldsNode, Document document, Hotel hotel) {
        List<Amenity> amenities = new ArrayList<>();
        String amenityTypeField = fieldsNode.get(AMENITY_TYPE).asText();
        if (StringUtils.isEmpty(amenityTypeField) && !StringUtils
                .isEmpty(fieldsNode.get(AMENITY_OPTIONS).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(AMENITY_OPTIONS).asText());
            if (Objects.nonNull(object)) {
                List<String> options = (List<String>) object;
                options.forEach(option -> {
                    for (Entry<String, String> entry : amenityMap.entrySet()) {
                        if (entry.getValue().contains(option.toLowerCase())) {
                            Optional<Amenity> optionalAmenity = amenities.stream()
                                    .filter(amenity -> amenity.getType().equals(entry.getKey()))
                                    .findFirst();
                            if (optionalAmenity.isPresent()) {
                                optionalAmenity.get().getOptions().add(option.toLowerCase());
                            } else {
                                Set<String> options2 = new HashSet<>();
                                options2.add(option.toLowerCase());
                                amenities.add(new Amenity(entry.getKey(),
                                        options2));
                            }
                            break;
                        }
                    }
                });
            }
        } else if (amenityTypeField.startsWith(SEMICOLON_PREFIX)) {
            Document documentAmenities = document
                    .get(amenityTypeField.substring(1), Document.class);
            documentAmenities.keySet().forEach(key -> {
                Amenity amenity = new Amenity(key,
                        ((List<String>) documentAmenities.get(key)).stream()
                                .map(s -> s.toLowerCase()).collect(
                                Collectors.toSet()));

                amenities.add(amenity);
            });
        }
        hotel.setAmenities(amenities);
    }

    private void populateNewLocation(JsonNode fieldsNode, Document document, Hotel hotel) {
        Location location = new Location();
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_ADDRESS).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_ADDRESS).asText());
            location.setAddress(Objects.nonNull(object) ? object.toString() : null);
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_CITY).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_CITY).asText());
            location.setCity(Objects.nonNull(object) ? object.toString() : null);
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_COUNTRY).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_COUNTRY).asText());
            location.setCountry(Objects.nonNull(object) ? object.toString() : null);
        }

        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_LAT).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_LAT).asText());
            location.setLat(Objects.nonNull(object) && Double.class.isInstance(object) ? (Double)object : null);
        }
        if (!StringUtils.isEmpty(fieldsNode.get(LOCATION_LNG).asText())) {
            Object object = getWithDotNotation(document,
                    fieldsNode.get(LOCATION_LNG).asText());
            location.setLng(Objects.nonNull(object) && Double.class.isInstance(object) ? (Double) object : null);
        }
        hotel.setLocation(location);
    }

    private Object getWithDotNotation(Document document, String dots)
            throws MongoException {
        String[] keys = dots.split(DOT_REGEX);
        Document doc = document;

        for (int i = 0; i < keys.length - 1; i++) {
            Object o = doc.get(keys[i]);
            if (Objects.isNull(o) || !(o instanceof Document)) {
                throw new MongoException(String.format(
                        "Field '%s' does not exist or is not a Document", keys[i]));
            }
            doc = (Document) o;
        }
        return doc.get(keys[keys.length - 1]);
    }
}
