package com.grainger.Automation;

public class AllActions {

	private SoftAssertion myAssert;
	private SeleniumDriver sd = new SeleniumDriver();
	private String executionEnv = SetupHelper.configProp
			.getProperty("RunningEnviroment");
	private String sessionId;
	private FabCart fabcart;

	public AllActions(SoftAssertion sr) {
		myAssert = sr;
	}

	public static Logger log = Logger.getLogger(AllActions.class.getName());
	
/****************************************************************************************************
 * Method: runKeyword
 * Description: 
 * @author ysxg070
 * @return 
 ***************************************************************************************************/    
	public boolean runKeyword(ArrayList<String> argList, String className, String methodName) {
		int argListSize = 0;
		if(argList != null)
			argListSize = argList.size();
		boolean keywordResult = false;
		Class<?> clName = null;
		Parameter[] parameters;
		Class<?>[] paramType;
		Object[] methodArguments;
		int methodParameters = 0;
		try {
			clName = Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Class<?>[] argTypes = {SoftAssertion.class};
		Constructor<?> constructor = null;
		Object classInstance = null;
		try {
			constructor = clName.getDeclaredConstructor(argTypes);
		} catch (NoSuchMethodException | SecurityException e2) {
			e2.printStackTrace();
		}
		Object[] arguments = {myAssert};
		try {
			classInstance = constructor.newInstance(arguments);
		} catch (InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e2) {
			e2.printStackTrace();
		}
		Method[] methods = clName.getDeclaredMethods();
		for(Method meth: methods){
			if(meth.getName().equalsIgnoreCase(methodName)){
				parameters = meth.getParameters();
				methodParameters = parameters.length;
				methodArguments = new Object[methodParameters];
				paramType = new Class<?>[methodParameters];
				Class<?>[] types = meth.getParameterTypes();
				if(argListSize != methodParameters){
					myAssert.setGblPassFailMessage("fail", "Expected " + methodParameters + " argument found "
									+ argListSize + " arguments");
					return false;
				}
				 for(int i=0; i<types.length;i++){
					 paramType[i] = types[i];
				 }
				 if(methodParameters != 0){
					 for(int i=0; i<argListSize; i++){
						 methodArguments[i] = (Object) argList.get(i);
					 }
				 }
				try {
					meth = clName.getDeclaredMethod(meth.getName(), paramType);
					keywordResult = (Boolean) meth.invoke(classInstance, methodArguments);
					break;
				} catch (IllegalArgumentException | IllegalAccessException 
						| InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
				}
			}
		}
		
		if(keywordResult || argListSize == 0){
			myAssert.assertTrue(keywordResult);
			return true;
		}else{
			myAssert.assertTrue(false);
			return false;
		}
	}
	
/****************************************************************************************************
 * @Method: prerequisiteActions
 * @Cases: Initial Routines, Initialize Data, Set Global Variables
 * @author
 * @return boolean
 ***************************************************************************************************/
	public ArrayList<String> getInitialInformationFromXml(int i, NodeList tList){
		String argumentList = null;
		Utilities util = new Utilities(myAssert);
		ArrayList<String> returnList = null;
		
		AssertionAndTestStep.setValue("keyWord",
				tList.item(i).getNodeName());
		try {
			AssertionAndTestStep.setValue("testStepName", 
					tList.item(i).getAttributes().getNamedItem("TSNAME").getNodeValue());
		} catch (Exception e) {
			AssertionAndTestStep.setValue("testStepName", "");
		}
		
		argumentList = tList.item(i).getTextContent();
		if (!argumentList.isEmpty()) {
			returnList = util.getParametersValue(argumentList);
		} else {
			returnList = null;
		}
		
		try {
			AssertionAndTestStep.setValue("parameterValues",
							returnList.toString().replace("[", "").replace("]", ""));
		} catch (Exception e) {
			AssertionAndTestStep.setValue("parameterValues", "");
		}
		return returnList;
	}
	
/****************************************************************************************************
 * @Method: prerequisiteActions
 * @Cases: Initial Routines, Initialize Data, Set Global Variables
 * @author
 * @return boolean
 ***************************************************************************************************/
	public boolean prerequisiteActions(Node keyWords) {
		try {
			if (executionEnv.contains("Remote")) {
				sessionId = sd.getSessionId();
			}

			String strKeyword = "";
			boolean keywordResult = false;
			boolean testCaseResult = true;

			NodeList tList = keyWords.getChildNodes();

			for (int i = 0; i < tList.getLength(); i++) {
				if ((sessionId == null || sessionId.isEmpty()) && executionEnv.contains("Remote")) {
					testCaseResult = false;
					break;
				}
				if (tList.item(i).getNodeType() == Node.ELEMENT_NODE) {
					getInitialInformationFromXml(i,tList);
					strKeyword = tList.item(i).getNodeName().toLowerCase();
					switch (strKeyword) {

					case "initialroutines":
					case "initializedata":
						keywordResult = true;
						keywordResult = StaticMethods.setGlobalUnique() && keywordResult;
						MasterHashMap.setValue("gblStrRememberMe", "Off");
						myAssert.assertTrue(keywordResult);
						break;

					case "setglobalvariables":
						myAssert.assertTrue(true);
						break;

					default:
						myAssert.setGblPassFailMessage("fail", "Invalid keyword :" + strKeyword);
						myAssert.assertTrue(false);
						keywordResult = false;
					}
					testCaseResult = testCaseResult && keywordResult;
				}
				myAssert.setGblPassFailMessage("pass", "");
				myAssert.setGblPassFailMessage("fail", "");
			}
			return testCaseResult;
		} catch (Exception e) {
			myAssert.assertTrue(false);
			log.info("Caught an exception in Pre Requisites Actions " + e.toString());
			throw e;
		}
	}
/****************************************************************************************************
 * @Method: homePageActions
 * @Cases: Set Global Variables, Open Browser, Login, Verify Login, Logout,
 *         Verify Logout, Verify Current Page, Verify Current Modal, Click
 *         Grainger Logo, Navigate To, Click Top Nav Link, Click Customer
 *         Service Link, Search Item, Clean The Cart Environment, Pause
 *         Execution, Clean The Cart, Verify Catalog Options, Select Catalog
 *         Option, Verify CC Alert Message, Go To Item Detail Page,
 *         RequestTAService, VerifyTAResponse, VerifyDigitalData,
 *         AddJavaScript, ViewPendingOrder, 
 * @author
 * @return boolean
 ***************************************************************************************************/
	public boolean homePageActions (Node keyWords) {
		try{
		if (executionEnv.contains("Remote")) {
			sessionId = sd.getSessionId();
		}
		String tName = MasterHashMap.getValue("testCaseName");
		String strKeyword = "";
		boolean keywordResult = false;
		boolean testCaseResult = true;
		ArrayList<String> argList = null;

		NodeList tList = keyWords.getChildNodes();

		for (int i = 0; i < tList.getLength(); i++) {
			if ((sessionId == null || sessionId.isEmpty())
					&& executionEnv.contains("Remote")) {
				testCaseResult = false;
				break;
			}
			if (tList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				argList = getInitialInformationFromXml(i, tList);
				strKeyword = tList.item(i).getNodeName().toLowerCase();
				switch (strKeyword) {


				case "navigatetourl":
					runKeyword(argList, Utilities.class.getName(), strKeyword);
					break;
				
									  
		            case "setglobalvariables":
						myAssert.assertTrue(true);
					    break;
		            	  
					default:
						myAssert.setGblPassFailMessage("fail", "Invalid keyword :" + strKeyword);
						myAssert.assertTrue(false);
						keywordResult = false;
					}//End Switch
					testCaseResult = testCaseResult && keywordResult;
				}
				myAssert.setGblPassFailMessage("pass", "");
				myAssert.setGblPassFailMessage("fail", "");
			}
			return testCaseResult;
			}catch (Exception e){
				myAssert.assertTrue(false);
				log.info("Caught an exception in CSA Search Actions " + e.toString());
				throw e;
			}
		}
	/*****************************************************************************************
	 * UpTake Test for Home Page
	 * 
	 *******************************************************************************************/
	//TODO
	public boolean verifyHomePageDisplayed(){
		WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
		WebElement body;
		try{
			body = driver.findElement(By.tagName("body"));
			if(body.getAttribute("class").contains("home page")){
				myAssert.setGblPassFailMessage("pass", "UpTake home page is displayed");
				return true;
			}
		}
		catch(Exception e){
			myAssert.setGblPassFailMessage("fail", "UpTake home page is not displayed");
			return false;
		}
		    return true;
	}

	/**************************************************************
	 * click menu primary top navigation links in Home page
	 * @return boolean
	 **************************************************************/
	public boolean clickLinkInTopNavigation(String strLinkName){
	WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
	WebElement topNav;
	try{
	topNav = driver.findElement(By.id("menu-primary-nav"));
	topNav.findElement(By.linkText(strLinkName)).click();
	util.syncBrowser();
	}
	catch(Exception e){
		myAssert.setGblPassFailMessage("fail", "Cannot find element link with name: " + strLinkName);
		return false;
	}
	    myAssert.setGblPassFailMessage("pass", "Link name: " + strLinkName + " clicked successfully");
	    return true;
	}
	
	/****************************************************************
	 * Verify 5 keys and content are displayed in Approach
	 * @return boolean
	 ***********************************************************************/
	public boolean verfyApproachKeyContentDisplayed(String strKeyName){
		WebDriver driver = SeleniumDriver.getInstance().getWebDriver();
		WebElement oParent;
		List<WebElement> oColl;
		try{
		//oParent = driver.findElement(By.className("l-wrap"));
		oParent = driver.findElement(By.id("how"));
		oColl = oParent.findElements(By.className("panel__how__item"));
		for(WebElement elm: oColl){
			if(elm.findElement(By.tagName("h3")).getText().equalsIgnoreCase(strKeyName)){
				elm.findElement(By.tagName("h3")).click();
				util.syncBrowser();
			if(elm.findElement(By.tagName("p")).getAttribute("style").contains("block")){
				myAssert.setGblPassFailMessage("pass", "Key Name: " + strKeyName + " content is displayed");
				return true;
			  }
			}
		  }
		
		}
		catch(Exception e){
			myAssert.setGblPassFailMessage("fail", "Cannot click Key Name: " + strKeyName + " and/or content is not displayed");
			return false;
		}
		    return true;
	}
		
}

