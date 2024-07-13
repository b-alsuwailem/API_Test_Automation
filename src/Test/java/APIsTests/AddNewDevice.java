package APIsTests;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import io.restassured.response.Response;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import Resources.Constant;
import Helpers.TestHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddNewDevice {

    private static ExtentHtmlReporter htmlReporter;
    private static ExtentReports extent;
    private static ExtentTest test;
    private static Map<String, Response> deviceResponses;

    @BeforeSuite
    public void setUp() {
        // Initialize ExtentHtmlReporter
        htmlReporter = new ExtentHtmlReporter("target/TestReport.html");

        // Configuration of the report
        htmlReporter.config().setDocumentTitle("Automation Test Report");
        htmlReporter.config().setReportName("REST API Test Report");
        htmlReporter.config().setEncoding("UTF-8");

        // Create instance of ExtentReports and attach the reporter
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);

        // Initialize and prepare deviceResponses
        deviceResponses = new HashMap<>();
        prepareDeviceResponses(); // Ensure responses are prepared before any tests run
    }

    private void prepareDeviceResponses() {
        if (!deviceResponses.isEmpty()) {
            // Responses are already prepared
            return;
        }

        CSVParser csvParser = null;
        try {
            csvParser = TestHelper.readCSV(Constant.CSV_FILE_PATH);

            // Iterate over CSV records and send POST requests
            for (CSVRecord csvRecord : csvParser) {
                // Extract data from CSV
                String name = csvRecord.get("name");
                int year = Integer.parseInt(csvRecord.get("year"));
                float price = Float.parseFloat(csvRecord.get("price"));
                String cpuModel = csvRecord.get("CPU model");
                String hardDiskSize = csvRecord.get("Hard disk size");

                // Request Payload
                String requestBody = "{\n" +
                        " \"name\": \"" + name + "\",\n" +
                        " \"data\": {\n" +
                        " \"year\": " + year + ",\n" +
                        " \"price\": " + price + ",\n" +
                        " \"CPU model\": \"" + cpuModel + "\",\n" +
                        " \"Hard disk size\": \"" + hardDiskSize + "\"\n" +
                        " }\n" +
                        "}";

                // Send POST request
                Response response = TestHelper.sendPostRequest(Constant.BASE_URL, Constant.ENDPOINT, requestBody);

                // Store the response in the map using a key for identification
                String key = name + "-" + year + "-" + price;
                deviceResponses.put(key, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (csvParser != null) {
                try {
                    csvParser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                TestHelper.closeCSVReader();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    // TC_01: Validate the ID and CreatedDate received values are not NULL
    public void testIdAndCreatedDateNotNull() {
        test = extent.createTest("Validate ID and Created Date", "Test to ensure 'id' and 'createdAt' are not null");

        for (Map.Entry<String, Response> entry : deviceResponses.entrySet()) {
            String key = entry.getKey();
            Response response = entry.getValue();

            try {
                // Validate response details
                String id = response.jsonPath().getString("id");
                String createdAt = response.jsonPath().getString("createdAt");

                // Assert that id and createdAt are not null
                TestHelper.verifyDataNotNull(id);
                TestHelper.verifyDataNotNull(createdAt);

                test.pass("ID and Created Date validation passed for " + key);

                // Log the received id and created date
                test.log(Status.INFO, MarkupHelper.createLabel("Received ID:", ExtentColor.GREEN));
                test.log(Status.INFO, id);
                test.log(Status.INFO, MarkupHelper.createLabel("Received Created Date:", ExtentColor.GREEN));
                test.log(Status.INFO, createdAt);

            } catch (AssertionError e) {
                test.log(Status.FAIL, "Validation failed: ID or Created Date is null for " + key);
                test.log(Status.FAIL, e); // Log the assertion error message
                Assert.fail("Validation failed due to assertion error: " + e.getMessage());
            }
        }
    }

    @Test
    //TC_02: Validate the newly added device received values in the response
    public void testResponseData() {
        test = extent.createTest("Validate Response Data", "Test to validate response data against expected values");

        for (Map.Entry<String, Response> entry : deviceResponses.entrySet()) {
            String key = entry.getKey();
            Response response = entry.getValue();

            try {
                // Extract data from key
                String[] parts = key.split("-");
                String name = parts[0];
                int year = Integer.parseInt(parts[1]);
                float price = Float.parseFloat(parts[2]);

                TestHelper.validateResponse(response);

                // Extract and validate response details
                String id = response.jsonPath().getString("id");
                String createdAt = response.jsonPath().getString("createdAt");

                TestHelper.verifyStringsEqual(response.jsonPath().getString("name"), name);
                TestHelper.verifyIntegersEqual(response.jsonPath().getInt("data.year"), year);
                TestHelper.verifyFloatsEqual(response.jsonPath().getFloat("data.price"), price);
                TestHelper.verifyStringsEqual(response.jsonPath().getString("data['CPU model']"), response.jsonPath().getString("data['CPU model']"));
                TestHelper.verifyStringsEqual(response.jsonPath().getString("data['Hard disk size']"), response.jsonPath().getString("data['Hard disk size']"));

                // Log test steps to Extent Report
                test.pass("Response data validation passed for " + key);
                test.log(Status.INFO, MarkupHelper.createLabel("Request Payload:", ExtentColor.BLUE));
                test.log(Status.INFO, "{\n" +
                        " \"name\": \"" + name + "\",\n" +
                        " \"data\": {\n" +
                        " \"year\": " + year + ",\n" +
                        " \"price\": " + price + ",\n" +
                        " \"CPU model\": \"" + response.jsonPath().getString("data['CPU model']") + "\",\n" +
                        " \"Hard disk size\": \"" + response.jsonPath().getString("data['Hard disk size']") + "\"\n" +
                        " }\n" +
                        "}");
                test.log(Status.INFO, MarkupHelper.createLabel("Response Details:", ExtentColor.BLUE));
                test.log(Status.INFO, "New Device Added:");
                test.log(Status.INFO, "ID: " + id);
                test.log(Status.INFO, "Name: " + response.jsonPath().getString("name"));
                test.log(Status.INFO, "Created At: " + createdAt);
                test.log(Status.INFO, "Year: " + response.jsonPath().getInt("data.year"));
                test.log(Status.INFO, "Price: " + response.jsonPath().getFloat("data.price"));
                test.log(Status.INFO, "CPU model: " + response.jsonPath().getString("data['CPU model']"));
                test.log(Status.INFO, "Hard disk size: " + response.jsonPath().getString("data['Hard disk size']"));

            } catch (AssertionError e) {
                // Handle assertion failure
                test.log(Status.FAIL, "Validation failed: Response data does not match expected values for " + key);
                test.log(Status.FAIL, e); // Log the assertion error message
                Assert.fail("Validation failed due to assertion error: " + e.getMessage());
            }
        }
    }

    @AfterSuite
    public void tearDown() {
        // Closing the extent report
        extent.flush();
    }
}
