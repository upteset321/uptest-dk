package com.uptake.Automation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.util.Random;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jackson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.umass.cs.benchlab.har.tools.*;
import edu.umass.cs.benchlab.har.*;



public class Utilities {

	private SoftAssertion myAssert;

	WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
	Xls_Reader datatable = new Xls_Reader(System.getProperty("user.dir")+"/Data/RegressionData.xls");

	public static Logger logger = Logger.getLogger(Utilities.class.getName());
	
	public Utilities(SoftAssertion sr) {
		myAssert = sr;
	}
	
    

	
/****************************************************************************************************
 * Method: navigateToUrl
 * Description: 
 * @author 
 * @return boolean
 ***************************************************************************************************/
	public boolean navigateToUrl(String strUrl) {
		WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
		String browserType = SetupHelper.configProp.getProperty("RunningEnviroment");
		try {
			if(strUrl.contains("uptake")){
				driver.get("http://www.uptake.com");
			
			}
				syncBrowser();
				StaticMethods.pauseExecution(3000);
				String currUrl = driver.getCurrentUrl();
				if (!currUrl.contains("uptake")) {
					myAssert.setGblPassFailMessage("fail", "Unable to open '" + browserType + "' browser for url: "
							+ strUrl + "With Actual Url: " + currUrl + "Re Trying Driver Get again");
					// Second Attempt to get url with Driver
					logger.info("Unable to open '" + browserType + "' browser for url: " + strUrl
							+ " With Actual Url Displayed: " + currUrl +  "Re Trying Driver Get again");
					driver.get(strUrl);
					syncBrowser();
				} else {
					myAssert.setGblPassFailMessage("pass", "'" + browserType + "' browser is opened with Expected URL: "
							+ strUrl + "Actual Url Opened:" + currUrl);
				}
			}
		} catch (Exception e) {
			myAssert.setGblPassFailMessage("fail",
					"Unable to open '" + browserType + "' browser for url: " + strUrl + ", Error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		myAssert.setGblPassFailMessage("pass", "Successfully navigated to: " + strUrl);
		return true;
	}

/****************************************************************************************************
 * Method: syncBrowser
 * Description: 
 * @author 
 * @return boolean
 ***************************************************************************************************/
	public boolean syncBrowser() {
		WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
		long previous;
		long current = 0;
		long timeSliceMs = 2000; // wait for 2 second
		int timeOutSecond = 130;
		String script = "return document.getElementsByTagName('*').length";
		
		boolean surveyDisabled = surveyPopup();
		boolean chatDisabled = chatBoxPopup();
		boolean closeCATActive = closeCATActive();
		if (surveyDisabled)
			logger.info("Survey Pop up Detected and Clicked Decline Button");
		if (chatDisabled)
			logger.info("Chat Help Pop up Detected and Clicked Decline BUtton");
		if (closeCATActive)
			logger.info("CAT Activate Pop up Detected and Clicked Dont Activate Link");
		
		do {
			previous = current;
			try {
				Thread.sleep(timeSliceMs);
			} catch (Exception e) {
				logger.error("Unable to sleep in syncBrowser method, "
						+ e.toString());
			}
			timeOutSecond -= (timeSliceMs / 1000);
			surveyPopup();
			chatBoxPopup();
			closeCATActive();
			try {
				current = (long) ((JavascriptExecutor) driver)
						.executeScript(script);

			} catch (Exception e) {
				System.out.println(e.toString());
				previous = -1;
			}
		} while (current > previous && timeOutSecond > 0);
		SeleniumDriver.getJavaScriptErrors();
		if (timeOutSecond > 0)
			return true;
		else
			return false;
	}

}	
