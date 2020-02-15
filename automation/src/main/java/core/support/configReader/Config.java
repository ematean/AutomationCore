package core.support.configReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class Config {

	private static final String CONFIG_PREFIX = "config.";
	public static String RESOURCE_PATH = PropertiesReader.getLocalResourcePath();

	/**
	 * gets property value based on key from maven or properties file order: maven
	 * Then properties
	 * @param key key in properties file
	 * @param property
	 * @return string value of property file
	 */
	private static String getStringProperty(String key, Properties property) {
		if (!MavenReader.getStringProperty(key).isEmpty()) {
			return MavenReader.getStringProperty(key);
		}
		if (!PropertiesReader.getStringProperty(key, property).isEmpty()) {
			return PropertiesReader.getStringProperty(key, property);
		}

		return "";
	}

	/**
	 * git all files in given directory
	 * @param curDir target directory
	 */
	public static void getAllFiles(File curDir) {

		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				System.out.println("All files: " + f.getPath() + " : " + f.getName());
			}
		}
	}
	

	/**
	 * get all key values from property files in directory at path
	 * Fails if duplicate key exists. All keys need to be unique
	 * @param path path to proeprties file
	 * @return map of all key and values in all property files in given path
	 */
	public static Map<String, String> getAllKeys(String path) {
		Map<String, String> config = new ConcurrentHashMap<String, String>();

		try {
			List<Properties> properties = PropertiesReader.Property(path);

			for (Properties property : properties) {

				for (String key : property.stringPropertyNames()) {
					Helper.assertTrue("duplicate property/config key exists: " + key + " at folder: " + path, config.get(key) == null);
					String value = getStringProperty(key, property);
					config.put(key, value);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return config;
	}

	/**
	 * loads config And properties files to TestObject config map
	 * @param testId id of the test
	 */
	public static void loadConfig(String testId) {

		Map<String, Object> config = loadConfigProperties();
		TestObject.getTestInfo(testId).config.putAll(config);	
	}
	
	/**
	 * loads config And properties files to TestObject config map
	 * @param testId id of the test
	 * @return 
	 */
	public static Map<String, Object> loadConfigProperties() {

		Map<String, Object> config = new ConcurrentHashMap<String, Object>();
		// get all keys from resource path
		Map<String, String> propertiesMap = getAllKeys(RESOURCE_PATH);
		config.putAll(propertiesMap);

		// load config/properties values from entries with "config_" prefix
		for (Entry<String, String> entry : propertiesMap.entrySet()) {
			boolean isConfig = entry.getKey().toString().startsWith(CONFIG_PREFIX);
			if (isConfig) {
				propertiesMap = getAllKeys(PropertiesReader.getLocalRootPath() + entry.getValue());
				config.putAll(propertiesMap);
			}
		}
		return config;
	}
	
	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getValue(String key) {
		return getValue(key, false);
	}

	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getValue(String key, boolean isFailable) {

		Object value = TestObject.getTestInfo().config.get(key);
		if (value == null) {
			 if(isFailable) Helper.assertFalse("value not found, default empty: " + key);
			
			// keep track of missing config variables
			TestObject.getTestInfo().missingConfigVars.add(key);
			
			value = StringUtils.EMPTY;
			return value.toString();
		}
		List<String> items = Arrays.asList(value.toString().split("\\s*,\\s*"));
		if(items.size() == 0) {
			items = new ArrayList<String>();
			items.add(value.toString());
		}
		return items.get(0);
	}
	
	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getGlobalIntValue(String key) {
		String value = getValue(key, false);
		if (value.isEmpty()) {
			return -1;
		}
		return Integer.valueOf(value);
	}
	
	
	/**
	 * gets the object value from property key
	 * @param key key in properties file
	 * @return returns the object value of key from properties
	 */
	public static Object getGlobalObjectValue(String key) {
		if(TestObject.getDefaultTestInfo().config.get(key) == null)
			return StringUtils.EMPTY;
		Object value = TestObject.getTestInfo().config.get(key);
		return value;
	}
	
	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getGlobalBooleanValue(String key) {
		String value = getGlobalValue(key, false);
		if (value.isEmpty()) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getGlobalValue(String key) {
		return getGlobalValue(key, false);
	}
	
	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getGlobalValue(String key, boolean isFailable) {

		Object value = TestObject.getDefaultTestInfo().config.get(key);
		if (value == null) {
			 if(isFailable) Helper.assertFalse("value not found, default empty: " + key);
			value = StringUtils.EMPTY;
		}
		List<String> items = Arrays.asList(value.toString().split("\\s*,\\s*"));
		return items.get(0);
	}
	
	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getBooleanValue(String key, boolean isFailable) {
		String value = getValue(key,isFailable);
		if (value.isEmpty()) {
			 if(isFailable) Helper.assertFalse("value not found: " + key);
			return false;
		}
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * gets the object value from property key
	 * @param key key in properties file
	 * @return returns the object value of key from properties
	 */
	public static Object getObjectValue(String key) {
		if(TestObject.getTestInfo().config.get(key) == null) {
			return null;
		}
		Object value = TestObject.getTestInfo().config.get(key);
		return value;
	}
	
	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getIntValue(String key) {
		return getIntValue(key, false);
	}

	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getIntValue(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		if (value.isEmpty()) {
			 if(isFailable) Helper.assertFalse("value not found: " + key);
			return -1;
		}
		return Integer.valueOf(value);
	}
	
	/**
	 * gets double value from properties key
	 * 
	 * @param key key in properties file
	 * @return the double value of key from properties
	 */
	public static double getDoubleValue(String key) {
		return getDoubleValue(key, false);
	}
	
	/**
	 * gets double value from properties key
	 * 
	 * @param key key in properties file
	 * @return the double value of key from properties
	 */
	public static double getDoubleValue(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		if (value.isEmpty()) {
			if(isFailable) Helper.assertFalse("value not found: " + key);
			return -1;
		}
		return Double.valueOf(value);
	}
	
	/**
	 * returns a list from config value values separated by ","
	 * 
	 * @param key key in properties file
	 * @return the list of values from key separated by ","
	 */
	public static List<String> getValueList(String key) {
		return getValueList(key, true);
	}

	/**
	 * returns a list from config value values separated by ","
	 * 
	 * @param key key in properties file
	 * @return the list of values from key separated by ","
	 */
	public static List<String> getValueList(String key, boolean isFailable) {
		String value = (String) TestObject.getTestInfo().config.get(key);
		List<String> items = new ArrayList<String>();
		if (value == null) {
			if(isFailable) Helper.assertFalse("value not found in config files: " + key);
		}
		if(!value.isEmpty()) 
			items = Arrays.asList(value.split("\\s*,\\s*"));
		return items;
	}
	
	/**
	 * puts key value pair in config
	 * 
	 * @param key key in properties file
	 * @param value value associated with key
	 */
	public static void putValue(String key, Object value) {
		putValue(key, value, true);
	}

	/**
	 * puts key value pair in config
	 * 
	 * @param key key in properties file
	 * @param value value associated with key
	 */
	public static void putValue(String key, Object value, boolean isLog) {
		Object existingValue = TestObject.getTestInfo().config.get(key);
		
		// don't add if value already exists
		if(existingValue != null && existingValue.equals(value)) return;
		
		if(isLog)
			TestLog.logPass("storing in key: " + key + " value: " + value);
		TestObject.getTestInfo().config.put(key, value);
	}
	
	public static void putValue(String key, Object value, String info) {
		Object existingValue = TestObject.getTestInfo().config.get(key);

		// don't add if value already exists
		if(existingValue != null && existingValue.equals(value)) return;
				
		TestLog.logPass("storing in key: " + key + " value: " + info);
		TestObject.getTestInfo().config.put(key, value);
	}
	
	/**
	 * set parent config value
	 * @param key
	 * @param value
	 */
	public static void setParentValue(String key, Object value) {
		ServiceObject service = TestObject.getTestInfo().serviceObject;
		TestObject.getParentTestInfo(service).config.put(key, value);

	}
	
	/**
	 * set global config value
	 * @param key
	 * @param value
	 */
	public static void setGlobalValue(String key, Object value) {
		TestLog.logPass("storing in global key: " + key + " value: " + value);
		TestObject.getDefaultTestInfo().config.put(key, value);
	}
	
	/**
	 * get parent config value
	 * @param key
	 * @return 
	 * @return
	 */
	public static boolean getParentValue(String key) {
		ServiceObject service = TestObject.getTestInfo().serviceObject;
		Object value = TestObject.getParentTestInfo(service).config.get(key);
		if(value == null) return false;
		return (boolean) value;
	}
	
	/**
	 * print a list of missing config variables
	 */
	public static void printMissingConfigVariables() {
		List<String> variables = TestObject.getTestInfo().missingConfigVars;
		if(variables.size() > 0)
			TestLog.ConsoleLog("List of missing config variables. Please see latest version for updated config: " +  StringUtils.join(variables, ", "));
	}
}