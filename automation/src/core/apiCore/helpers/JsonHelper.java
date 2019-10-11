package core.apiCore.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import io.restassured.response.Response;

public class JsonHelper {

	/**
	 * replaces output parameter with response values eg. $token with id form values
	 * are in form of list separated by ";"
	 * 
	 * @param response
	 * @param outputParam
	 */
	public static void saveOutboundJsonParameters(Response response, String outputParam) {
		if (response == null || outputParam.isEmpty())
			return;
		configMapJsonKeyValues(response, outputParam);
	}

	/**
	 * map key value to config eg.features.features.id:1:<$id>
	 * 
	 * @param response
	 * @param keyValue
	 */
	public static void configMapJsonKeyValues(Response response, String keyValue) {

		if (keyValue.isEmpty())
			return;

		// replace parameters for outputParam
		keyValue = DataHelper.replaceParameters(keyValue);

		List<KeyValue> keywords = DataHelper.getValidationMap(keyValue);
		for (KeyValue keyword : keywords) {
			
			// fail if value is wrong format
			if(!keyword.value.toString().startsWith("<") || !keyword.value.toString().contains("$")|| !keyword.value.toString().endsWith(">"))
				Helper.assertFalse("variable placement must of format <$variable>: " + keyword.value.toString());
			
			String key = (String) keyword.value;
			key = key.replace("$", "").replace("<", "").replace(">", "").trim();
			// gets json value. if list, returns string separated by comma
			String value = getJsonValue(response, keyword.key);

			if (!keyword.position.isEmpty()) {
				value = value.split(",")[Integer.valueOf(keyword.position) - 1];
			}
			Config.putValue(key, value);
			TestLog.logPass("replacing value " + key + " with: " + value);
		}
	}

	/**
	 * gets json value as list if applicable, or string if single item converts to
	 * string separated by ","
	 * 
	 * @param response
	 * @param path
	 * @return
	 */
	public static String getJsonValue(Response response, String path) {
		String value = StringUtils.EMPTY;
		List<String> values = getJsonListValueResponse(response, path);

		if (values == null || values.isEmpty()) {
			value = getJsonStringResponse(response, path);
		}

		if (values != null && !values.isEmpty())
			value = DataHelper.listToString(values);
		return value;
	}

	/**
	 * gets json value as list if applicable, or string if single item converts to
	 * string separated by "," https://github.com/json-path/JsonPath
	 * 
	 * @param path
	 *            https://github.com/json-path/JsonPath
	 * @return value string list separated by ","
	 */
	public static String getJsonValue(String json, String path) {
		String value = StringUtils.EMPTY;
		List<String> values = new ArrayList<String>();
		
		ReadContext ctx = JsonPath.parse(json);
		
		try {
			values = ctx.read("$." + path);
		}catch(Exception e) {
			value = ctx.read("$." + path);
		}

		if (values != null && !values.isEmpty())
			value = DataHelper.listToString(values);
		return value;
	}

	/**
	 * get json path value from xml string
	 * 
	 * @param xml
	 * @param path
	 * @return
	 */
	public static String getJsonValueFromXml(String xml, String path) {
		String value = StringUtils.EMPTY;

		// convert xml stirng to json string
		String json = XMLToJson(xml);

		// parse json string and get path value
		ReadContext ctx = JsonPath.parse(json);
		List<String> values = ctx.read("$." + path);

		if (values != null && !values.isEmpty())
			value = DataHelper.listToString(values);
		return value;
	}

	/**
	 * convert xml string to json string
	 * 
	 * @param xml
	 *            string
	 * @return json string
	 */
	public static String XMLToJson(String xml) {
		int printIndentFactor = 4;
		String jsonString = StringUtils.EMPTY;
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(xml);
			jsonString = xmlJSONObj.toString(printIndentFactor);
		} catch (JSONException je) {
			je.toString();
		}
		return jsonString;
	}

	public static String getResponseValue(Response response) {
		return response.getBody().asString();
	}

	private static List<String> getJsonListValueResponse(Response response, String path) {
		List<String> values = new ArrayList<String>();
		try {
			values = response.jsonPath().getList(path);
		} catch (Exception e) {
			e.getMessage();
		}

		if (values == null || values.isEmpty()) {
			// logWarn causes warning in the report
			TestLog.ConsoleLog("path: <" + path + "> returned empty results");
		}
		return values;
	}

	private static String getJsonStringResponse(Response response, String path) {
		String value = "";

		if (response.path(path) instanceof String) {
			value = response.path(path);
		}

		if (response.path(path) instanceof Integer) {
			value = Integer.toString(response.path(path));

		}

		if (response.path(path) instanceof Boolean) {
			value = Boolean.toString(response.path(path));
		}
		if (value == null)
			value = "";
		return value;
	}

	/**
	 * validates the json maps against the keyword requirements examples:
	 * "person.roles.name": hasItems("admin"), "person.lastName":
	 * equalTo("Administrator"), "person.lastName": isNotEmpty, "person.roles.name":
	 * contains("admin"), "person.roles.name": containsInAnyOrder(admin),
	 * "person.roles": nodeSizeGreaterThan(0), "person.sites.": nodeSizeExact(0)
	 * "person.roles.name": sequence("admin"),
	 * 
	 * 
	 * @param jsonMap
	 * @param response
	 */
	public static List<String> validateJsonKeywords(List<KeyValue> keywords, Response response) {
		List<String> errorMessages = new ArrayList<String>();
		for (KeyValue keyword : keywords) {
			String jsonPath = Helper.stringNormalize(keyword.key);
			String expectedValue = Helper.stringNormalize((String) keyword.value);
			String command = "";

			String[] expected = expectedValue.split("[\\(\\)]");
			// get value in between parenthesis
			if (expected.length > 1) {
				command = expected[0];
				expectedValue = expected[1];
			} else if (expected.length == 1) {
				command = expectedValue;
				expectedValue = "";
			}

			// get response string from json path (eg. data.user.id) would return "2"
			String responseString = getJsonValue(response, jsonPath);

			// validate response
			String errorMessage =  DataHelper.validateCommand(command, responseString, expectedValue, keyword.position);
			errorMessages.add(errorMessage);
		}
		return errorMessages;
	}

	/**
	 * validates json string
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isJSONValid(String test, boolean printError) {
		String error = StringUtils.EMPTY;
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				error = ex.getMessage();
				new JSONArray(test);
			} catch (JSONException ex1) {
				if(error.isEmpty()) error = ex1.getMessage();
				if(printError) TestLog.ConsoleLog("Invalid Json error: " + error);
				return false;
			}
		}
		return true;
	}

	/**
	 * validates expected json string against json body from response
	 * 
	 * @param expectedJson
	 * @param actualJson
	 * @return 
	 */
	public static String validateByJsonBody(String expectedJson, String response) {
		expectedJson = Helper.stringRemoveLines(expectedJson);
		if (JsonHelper.isJSONValid(expectedJson, true)) {
			TestLog.logPass("expected: " + Helper.stringRemoveLines(expectedJson));
			try {
				JSONCompareResult result = JSONCompare.compareJSON(expectedJson, response, JSONCompareMode.LENIENT);
				 if (result.failed()) {
					 return result.getMessage();
				 }
				//JSONAssert.assertEquals(expectedJson, response, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return StringUtils.EMPTY;
	}

	public static boolean isValidExpectation(String expectedJson) {
		if (JsonHelper.isJSONValid(expectedJson, false)) {
			return true;
		}
		expectedJson = Helper.stringNormalize(expectedJson);
		if (expectedJson.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR) || expectedJson.startsWith("_NOT_EMPTY_")
				|| expectedJson.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)) {
			return true;
		}
		return false;
	}

	/**
	 * validates json response against hamcrest keywords
	 * 
	 * @param expectedJson
	 * @param response
	 */
	public static List<String> validateByKeywords(String expectedJson, Response response) {
		List<String> errorMessages = new ArrayList<String>();
		
		expectedJson = Helper.stringNormalize(expectedJson);
		if (!JsonHelper.isJSONValid(expectedJson, false)) {
			if (expectedJson.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR)) {
				// get hashmap of json path And verification
				List<KeyValue> keywords = DataHelper.getValidationMap(expectedJson);
				// validate based on keywords
				errorMessages = JsonHelper.validateJsonKeywords(keywords, response);
				
				// response is not empty
			} else if (expectedJson.startsWith("_NOT_EMPTY_")) {
				String resonseBody = response.getBody().asString();
				if(response == null || resonseBody.isEmpty())
					errorMessages.add("response is empty");
			}
		}
		return errorMessages;
	}

	/**
	 * validates response body this is validating the response body as text
	 * 
	 * @param expected
	 * @param response
	 * @return 
	 */
	public static String validateResponseBody(String expected, Response response) {
		if (!expected.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)) {
			return StringUtils.EMPTY;
		}
		// remove the indicator _VERIFY.RESPONSE.BODY_
		expected = removeResponseIndicator(expected);

		String actual = JsonHelper.getResponseValue(response);
		String[] expectedArr = expected.split("[\\(\\)]");
		// get value in between parenthesis
		String command = expectedArr[0].trim();
		String expectedValue = expectedArr[1].trim();

		return DataHelper.validateCommand(command, actual, expectedValue, "1");

	}

	/**
	 * remove response indicators
	 * 
	 * @param expected
	 * @return
	 */
	public static String removeResponseIndicator(String expected) {
		List<String> indicator = new ArrayList<String>();
		indicator.add(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR);
		indicator.add(DataHelper.VERIFY_JSON_PART_INDICATOR);

		for (String value : indicator) {
			expected = expected.replace(value, "");
		}

		return expected;
	}
}
