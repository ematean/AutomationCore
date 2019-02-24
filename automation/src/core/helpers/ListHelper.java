package core.helpers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class ListHelper {

	/**
	 * selects an element in list by its index value
	 * 
	 * @param list
	 * @param index
	 */
	public void selectElementInList(EnhancedBy list, int index) {

		EnhancedWebElement listElement = Element.findElements(list);
		listElement.click(index);
	}

	/**
	 * selects an element in list by its index value and waits for expected element
	 * 
	 * @param list
	 * @param index
	 * @param expected
	 */
	public void selectElementInList(EnhancedBy list, int index, EnhancedBy expected) {

		Helper.click.clickAndExpect(list, index, expected, true);
	}

	/**
	 * enters value into the search field and selects enter waits for the loading
	 * spinner to be removed
	 * 
	 * @param searchQuery
	 * @param byTarget
	 * @param spinner
	 */
	public void searchAndWaitForResults(String searchQuery, EnhancedBy byTarget, EnhancedBy spinner) {

		Helper.form.setFieldAndEnter(byTarget, searchQuery);
		Helper.wait.waitForElementToBeRemoved(spinner);
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public void selectListItemEqualsByName(EnhancedBy list, String option) {

		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * finds target element which is in the same container and has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index by name, and use that to find the target element
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public void selectListItemEqualsByName(EnhancedBy list, String option, EnhancedBy target) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		selectElementInList(target, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * selects list item containing string eg. a list of athletes names containing a
	 * delete button
	 * 
	 * @param list
	 * @param option
	 * @param target
	 */
	public void selectListItemContainsByName(EnhancedBy list, String option, EnhancedBy target) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		selectElementInList(target, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * Selects list item from a parent container eg. delete button in a list defined
	 * by name find the container containing the name and then finds the delete
	 * button in that container as target
	 * 
	 * @param list
	 * @param option selectListItemContainsFromContainer
	 * @param target
	 */
	public void selectElementContainedInList(EnhancedBy list, String option, EnhancedBy target) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		EnhancedWebElement containerList = Element.findElements(list);
		EnhancedWebElement targetElement = Element.findElements(target, containerList.get(index));

		targetElement.click();
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * finds target element which is in the same container and has the same index as
	 * the parent eg. delete button in the list of customers, both having index 2.
	 * we find the index containing name, and use that to find the target element
	 * 
	 * @param list
	 * @param option
	 */
	public void selectListItemContainsByName(EnhancedBy list, String option) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		TestLog.logPass("I select list option '" + option + "' from list '" + list.name + "'");
	}

	/**
	 * selects list item by the string option provided
	 * 
	 * @param list
	 * @param option
	 */
	public void selectListItemByIndex(EnhancedBy list, int index) {
		;
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
		selectElementInList(list, index);
		// TestLog.logPass("I select list option at index'" + index + "' from list '" +
		// list.name + "'");
	}

	/**
	 * returns the number of elements in list
	 * 
	 * @param list
	 * @return
	 */
	public int getListCount(EnhancedBy list) {
		EnhancedWebElement listElements = Element.findElements(list);
		return listElements.count();
	}

	/**
	 * returns the index of text value in a list normalizes the list option when
	 * comparing using Helper.stringNormalize() returns first visible element index
	 * 
	 * @param list
	 * @param option
	 * @return
	 */

	public int getElementIndexEqualsByText(EnhancedBy list, String option) {

		int index = -1;
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		do {
			EnhancedWebElement listElements = Element.findElements(list);
			List<String> stringList = listElements.getTextList();

			index = getStringIndexEqualsByText(list, stringList, option);

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
			if (index != -1)
				break;
		} while (passedTimeInSeconds < AbstractDriver.TIMEOUT_SECONDS);
		return index;
	}

	/**
	 * retuns index of element in list which contains in text
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public int getElementIndexContainByText(EnhancedBy list, String option) {

		int index = -1;
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		do {
			EnhancedWebElement listElements = Element.findElements(list);
			List<String> stringList = listElements.getTextList();

			index = getStringIndexContainByText(list, stringList, option);
			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
			if (index != -1)
				break;
		} while (passedTimeInSeconds < AbstractDriver.TIMEOUT_SECONDS);
		return index;
	}

	public int getVisibleElementIndex(EnhancedBy list, List<Integer> indexValues) {
		EnhancedWebElement listElements = Element.findElements(list);

		for (Integer index : indexValues) {
			if (listElements.isExist(index)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * returns the index of string value in list of strings
	 * 
	 * @param stringList
	 *            normalized
	 * @param option
	 *            normalized
	 * @return
	 */
	public int getStringIndexContainByText(EnhancedBy list, List<String> stringList, String option) {
		EnhancedWebElement listElements = Element.findElements(list);

		String value = null;
		int listCount = stringList.size();
		for (int i = 0; i < listCount; i++) {
			value = Helper.stringNormalize(stringList.get(i));
			option = UtilityHelper.stringNormalize(option);

			if (value.contains(option)) {
				listElements.scrollToView(i);
				if (listElements.isExist(i)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * returns the index of string value in list of strings
	 * 
	 * @param stringList
	 *            normalized
	 * @param option
	 *            normalized
	 * @return
	 */
	public int getStringIndexContainByText(List<String> stringList, String option) {

		String value = null;
		int listCount = stringList.size();
		for (int i = 0; i < listCount; i++) {
			value = Helper.stringNormalize(stringList.get(i));
			option = UtilityHelper.stringNormalize(option);
			if (value.contains(option)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * returns the index of string value in list of strings
	 * 
	 * @param stringList
	 *            normalized
	 * @param option
	 *            normalized
	 * @return
	 */
	public int getStringIndexEqualsByText(EnhancedBy list, List<String> stringList, String option) {
		EnhancedWebElement listElements = Element.findElements(list);
		String listValue;

		int listCount = stringList.size();
		for (int i = 0; i < listCount; i++) {
			listValue = UtilityHelper.stringNormalize(stringList.get(i));
			option = UtilityHelper.stringNormalize(option);
			if (listValue.equalsIgnoreCase(option)) {
				listElements.scrollToView(i);
				if (listElements.isExist(i)) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * returns the index of string value in list of strings
	 * 
	 * @param stringList
	 *            normalized
	 * @param option
	 *            normalized
	 * @return
	 */
	public int getStringIndexEqualsByText(List<String> stringList, String option) {

		String listValue;

		int listCount = stringList.size();
		for (int i = 0; i < listCount; i++) {
			listValue = UtilityHelper.stringNormalize(stringList.get(i));
			option = UtilityHelper.stringNormalize(option);
			if (listValue.equalsIgnoreCase(option)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public void verifyContainsIsInList(EnhancedBy list, String option) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
	}

	/**
	 * verifies if option value is in the list index = -1 indicates the value is not
	 * in list
	 * 
	 * @param list
	 * @param option
	 */
	public void verifyIsInList(EnhancedBy list, String option) {
		Helper.wait.waitForElementToLoad(list);
		int index = getElementIndexEqualsByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);
	}

	/**
	 * verify text option in list based on key value in the list
	 * 
	 * @param list
	 * @param indicator
	 * @param option
	 */
	public void verifyIsInList(EnhancedBy list, String indicator, String option) {
		int index = getElementIndexEqualsByText(list, option);
		EnhancedWebElement listElements = Element.findElements(list);
		boolean isInList = listElements.getText(index).contains(option);
		AssertHelper.assertTrue("option not found in list: " + list.name, isInList);
	}

	/**
	 * return if element is contained in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public boolean isContainedInList(EnhancedBy list, String option) {
		EnhancedWebElement listElements = Element.findElements(list);
		List<String> stringList = listElements.getTextList();

		int index = getStringIndexContainByText(stringList, option);
		if (index == -1)
			return false;

		return index != -1;
	}

	/**
	 * return if element is an exact match in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public boolean isExactMatchInList(EnhancedBy list, String option) {
		int index = getElementIndexEqualsByText(list, option);
		return index != -1;
	}

	/**
	 * returns the list of values in a list
	 * 
	 * @param list
	 * @return
	 */
	public List<String> getListValues(EnhancedBy list) {
		EnhancedWebElement listElements = Element.findElements(list);
		return listElements.getTextList();
	}

	/**
	 * get list of text values
	 * 
	 * @param list
	 * @return
	 */
	public List<String> getTextList(EnhancedBy list) {
		EnhancedWebElement listElements = Element.findElements(list);
		List<String> stringList = listElements.getTextList();
		return stringList;

	}
	/*
	 * 
	 * public static int getFirstClickableElementIndex(EnhancedBy elements) {
	 * FluentWait wait = new
	 * FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(100,
	 * TimeUnit.MILLISECONDS).pollingEvery(5,
	 * TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class); Object
	 * isClickable = false;
	 * 
	 * EnhancedWebElement ElementList = Element.findElements(elements); for(int
	 * index=0; index < ElementList.count(); index++) { isClickable =
	 * wait.until(ExpectedConditions.elementToBeClickable(ElementList.get(index)));
	 * if(isClickable!=null) return index; }
	 * Helper.assertTrue("clickable element not found", false); return 0; }
	 */
}