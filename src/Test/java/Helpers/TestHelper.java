package Helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestHelper {
    private static Reader csvReader;

    public static Response sendPostRequest(String URL, String endpoint, String requestBody) {
        return RestAssured.given()
                .baseUri(URL)
                .basePath(endpoint)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post();
    }

    public static CSVParser readCSV(String csvFilePath) throws IOException {
        csvReader = new FileReader(csvFilePath);
        return CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
    }

    public static void closeCSVReader() throws IOException {
        if (csvReader != null) {
            csvReader.close();
        }
    }

    public static void validateResponse(Response response) {
        response.then().statusCode(200);
    }

    public static void verifyDataNotNull(String data) {
        assertNotNull(data, "This data should not be null");
    }

    public static void verifyStringsEqual(String actualData, String expectedData) {
        assertEquals(actualData, expectedData, "Strings are not equal");
    }

    public static void verifyIntegersEqual(int actualData, int expectedData) {
        assertEquals(actualData, expectedData, "Integers are not equal");
    }

    public static void verifyFloatsEqual(float actualData, float expectedData) {
        assertEquals(actualData, expectedData, "Floats are not equal");
    }
}
