package com.nhat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
//@TestPropertySource(properties = "app.scheduling.enable=false")
public class StoringServiceTest {

    public static final String HOTELS_1 = "hotels_1";
    public static final String IDField = "Id";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StoringService storingService;

    @BeforeEach
    public void prepareEachTest() {
        mongoTemplate.getCollection(HOTELS_1).drop();
    }

    @DisplayName("Test storing json data bases on hotel Id name, in case data is not exist")
    @Test
    public void testStoringHotelsJsonData() throws JsonProcessingException {
        String jsonData = "[{\n"
                + "    \"Id\": \"iJhz\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"Beach Villas Singapore\",\n"
                + "    \"Latitude\": 1.264751,\n"
                + "    \"Longitude\": 103.824006,\n"
                + "    \"Address\": \" 8 Sentosa Gateway, Beach Villas \",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"098269\",\n"
                + "    \"Description\": \"  This 5 star hotel is located on the coastline of Singapore.\",\n"
                + "    \"Facilities\": [\"Pool\", \"BusinessCenter\", \"WiFi \", \"DryCleaning\", \" Breakfast\"]\n"
                + "}, {\n"
                + "    \"Id\": \"SjyX\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"InterContinental Singapore Robertson Quay\",\n"
                + "    \"Latitude\": null,\n"
                + "    \"Longitude\": null,\n"
                + "    \"Address\": \" 1 Nanson Road\",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"238909\",\n"
                + "    \"Description\": \"Enjoy sophisticated waterfront living at the new InterContinental® Singapore Robertson Quay, luxury's preferred address nestled in the heart of Robertson Quay along the Singapore River, with the CBD just five minutes drive away. Magnifying the comforts of home, each of our 225 studios and suites features a host of thoughtful amenities that combine modernity with elegance, whilst maintaining functional practicality. The hotel also features a chic, luxurious Club InterContinental Lounge.\",\n"
                + "    \"Facilities\": [\"Pool\", \"WiFi \", \"Aircon\", \"BusinessCenter\", \"BathTub\", \"Breakfast\", \"DryCleaning\", \"Bar\"]\n"
                + "}]";
        List<JsonNode> jsonNodes = new ObjectMapper().readValue(jsonData,
                new TypeReference<List<JsonNode>>() {
                });
        storingService.storeHotelsData(IDField, jsonNodes);
        MongoCollection collection = mongoTemplate.getCollection(HOTELS_1);
        assertThat(collection.countDocuments()).isEqualTo(2);
        MongoCursor<Document> iterator = collection.find().iterator();
        try {
            while (iterator.hasNext()) {
                assertThat(iterator.next().getString(IDField)).isIn("iJhz", "SjyX");
            }
        } finally {
            iterator.close();
        }
    }

    @DisplayName("Test storing json data bases on hotel Id name, in case data existed")
    @Test
    public void testStoringHotelsJsonDataExisted() throws JsonProcessingException {
        String jsonData = "{\n"
                + "    \"Id\": \"iJhz\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"Beach Villas Singapore\",\n"
                + "    \"Latitude\": 1.264751,\n"
                + "    \"Longitude\": 103.824006,\n"
                + "    \"Address\": \" 8 Sentosa Gateway, Beach Villas \",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"098269\",\n"
                + "    \"Description\": \"  This 5 star hotel is located on the coastline of Singapore.\",\n"
                + "    \"Facilities\": [\"Pool\", \"BusinessCenter\", \"WiFi \", \"DryCleaning\", \" Breakfast\"]\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_1);
        jsonData = "{\n"
                + "    \"Id\": \"SjyX\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"InterContinental Singapore Robertson Quay\",\n"
                + "    \"Latitude\": null,\n"
                + "    \"Longitude\": null,\n"
                + "    \"Address\": \" 1 Nanson Road\",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"238909\",\n"
                + "    \"Description\": \"Enjoy sophisticated waterfront living at the new InterContinental® Singapore Robertson Quay, luxury's preferred address nestled in the heart of Robertson Quay along the Singapore River, with the CBD just five minutes drive away. Magnifying the comforts of home, each of our 225 studios and suites features a host of thoughtful amenities that combine modernity with elegance, whilst maintaining functional practicality. The hotel also features a chic, luxurious Club InterContinental Lounge.\",\n"
                + "    \"Facilities\": [\"Pool\", \"WiFi \", \"Aircon\", \"BusinessCenter\", \"BathTub\", \"Breakfast\", \"DryCleaning\", \"Bar\"]\n"
                + "}";
        mongoTemplate.insert(Document.parse(jsonData), HOTELS_1);
        jsonData = "[{\n"
                + "    \"Id\": \"iJhz\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"Beach Villas Singapore 2\",\n"
                + "    \"Latitude\": 1.264751,\n"
                + "    \"Longitude\": 103.824006,\n"
                + "    \"Address\": \" 8 Sentosa Gateway, Beach Villas \",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"098269\",\n"
                + "    \"Description\": \"  This 5 star hotel is located on the coastline of Singapore 2.\",\n"
                + "    \"Facilities\": [\"Pool\", \"BusinessCenter\", \"WiFi \", \"DryCleaning\", \" Breakfast\"]\n"
                + "}, {\n"
                + "    \"Id\": \"SjyX\",\n"
                + "    \"DestinationId\": 5432,\n"
                + "    \"Name\": \"InterContinental Singapore Robertson Quay 2\",\n"
                + "    \"Latitude\": null,\n"
                + "    \"Longitude\": null,\n"
                + "    \"Address\": \" 1 Nanson Road\",\n"
                + "    \"City\": \"Singapore\",\n"
                + "    \"Country\": \"SG\",\n"
                + "    \"PostalCode\": \"238909\",\n"
                + "    \"Description\": \"Enjoy sophisticated waterfront living at the new InterContinental® Singapore Robertson Quay, luxury's preferred address nestled in the heart of Robertson Quay along the Singapore River, with the CBD just five minutes drive away. Magnifying the comforts of home, each of our 225 studios and suites features a host of thoughtful amenities that combine modernity with elegance, whilst maintaining functional practicality. The hotel also features a chic, luxurious Club InterContinental Lounge 2.\",\n"
                + "    \"Facilities\": [\"Pool\", \"WiFi \", \"Aircon\", \"BusinessCenter\", \"BathTub\", \"Breakfast\", \"DryCleaning\", \"Bar\"]\n"
                + "}]";
        List<JsonNode> jsonNodes = new ObjectMapper().readValue(jsonData,
                new TypeReference<List<JsonNode>>() {
                });
        storingService.storeHotelsData(IDField, jsonNodes);
        MongoCollection collection = mongoTemplate.getCollection(HOTELS_1);
        assertThat(collection.countDocuments()).isEqualTo(2);
        MongoCursor<Document> iterator = collection.find().iterator();
        try {
            while (iterator.hasNext()) {
                assertThat(iterator.next().getString("Name")).isIn("Beach Villas Singapore 2",
                        "InterContinental Singapore Robertson Quay 2");
            }
        } finally {
            iterator.close();
        }
    }
}
