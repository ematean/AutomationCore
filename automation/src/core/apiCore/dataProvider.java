package core.apiCore;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.testng.annotations.DataProvider;

import core.apiCore.helpers.csvReader;
import core.support.configReader.Config;

public class dataProvider {

	public static String TEST_DATA_PATH;
	public static final String TEST_DATA_TEMPLATE_PATH = "api.templatePath";

	public static final String TEST_DATA_PARALLEL_PATH = "api.parallelTestcasePath";
	public static final String TEST_DATA_SEQUENTIAL_PATH = "api.sequentialTestcasePath";
	public static final String TEST_DATA_NONE_OPTIMISED_PATH = "api.nonOptimisedTecasePath";
	public static final String TEST_CASE_FILE = "api.testCaseFile";
	public static final String TEST_CASE = "api.testCase";
	public static final String API_KEYWORD_PATH = "api.keywordPath"; // directory to keyword csv files

	public static AtomicInteger csvFileIndex = new AtomicInteger(0);
	public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();

	@DataProvider(name = "parallelRun")
	public synchronized Iterator<Object[]> providerParallel() {
		dataProvider.TEST_DATA_PATH = Config.getValue(dataProvider.TEST_DATA_PARALLEL_PATH);
		return csvReader.getTestCasesFromCsvFile().iterator();
	}

	@DataProvider(name = "sequentialRun")
	public synchronized Iterator<Object[]> providerSequential() {
		dataProvider.TEST_DATA_PATH = Config.getValue(dataProvider.TEST_DATA_PARALLEL_PATH);
		return csvReader.getTestCasesFromCsvFile().iterator();
	}
}