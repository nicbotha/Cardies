package au.web;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

/**
 * RestAsured examples: https://github.com/rest-assured/rest-assured/blob/master/examples/rest-assured-itest-java/src/test/java/io/restassured/itest/java/JSONPostITest.java
 * */
public class ODataIT {

	@Before
	public void setUp() throws Exception{
		RestAssured.baseURI = "http://127.0.0.1";
		RestAssured.basePath = "/web";
		RestAssured.port = 7777;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}
	
	@Test
	public void somemoreTest() throws Exception {		
		try {			
			//Test create
			given().body("{ \"Name\": \"Entity_001\" }").with().contentType(ContentType.JSON).then().expect().body(is("{\"@odata.context\":\"$metadata#Somemore\",\"ID\":100000000,\"Name\":\"Entity_001\"}")).statusCode(201).when().post("/odata/Somemore");
			
			//Test read
			expect().body(is("{\"@odata.context\":\"$metadata#Somemore/$entity\",\"ID\":100000000,\"Name\":\"Entity_001\"}")).statusCode(200).when().get("/odata/Somemore(100000000)?$format=json");
			
			//Test update
			given().body("{\"ID\": 100000000,\"Name\":\"Entity_001\"}").with().contentType(ContentType.JSON).then().expect().statusCode(204).when().put("/odata/Somemore(100000000)");	
			
			//Test delete
			expect().statusCode(204).when().delete("/odata/Somemore(100000000)");
			
			//Test deleted
			expect().body(is("{\"error\":{\"code\":null,\"message\":\"No entity found for this key\"}}")).statusCode(404).when().get("/odata/Somemore(100000000)?$format=json");
			
		} finally {
			RestAssured.reset();
		}
	}
}