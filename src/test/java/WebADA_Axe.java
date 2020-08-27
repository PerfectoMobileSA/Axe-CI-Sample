import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;

public class WebADA_Axe {
	protected RemoteWebDriver driver;
	protected ReportiumClient reportiumClient;

	@Parameters({"platform"})
	@BeforeClass
	public void beforeClass(String platform) throws Exception {
		//Replace <<cloud name>> with your perfecto cloud name (e.g. demo) or pass it as gradle properties: -PcloudName=<<cloud name>>  
		String cloudName = "<<cloud name>>";
		//Replace <<security token>> with your perfecto security token or pass it as gradle properties: -PsecurityToken=<<SECURITY TOKEN>>  More info: https://developers.perfectomobile.com/display/PD/Generate+security+tokens
		String securityToken = "<<security token>>";


		System.out.println("Run started");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		if(platform.equalsIgnoreCase("Windows")) {
			capabilities.setCapability("platformName", "Windows");
			capabilities.setCapability("platformVersion", "10");
			capabilities.setCapability("browserName", "Chrome");
			capabilities.setCapability("browserVersion", "latest");
		}else if(platform.equalsIgnoreCase("Android")) {
			capabilities.setCapability("model", "Galaxy.*");
		}else if(platform.equalsIgnoreCase("Mac")) {
			capabilities.setCapability("platformName", "Mac");
			capabilities.setCapability("platformVersion", "macOS High Sierra");
			capabilities.setCapability("browserName", "Chrome");
			capabilities.setCapability("browserVersion", "beta");
			capabilities.setCapability("location", "NA-US-BOS");
			capabilities.setCapability("resolution", "800x600");
		}else if(platform.equalsIgnoreCase("iOS")) { 
			capabilities.setCapability("model", "iPhone.*");
		}else {
			capabilities.setCapability("model", "Galaxy.*");
		}
		capabilities.setCapability("securityToken", Utils.fetchSecurityToken(securityToken));

		try{
			driver = new RemoteWebDriver(new URL("https://" + Utils.fetchCloudName(cloudName)  + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities); 
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		}catch(SessionNotCreatedException e){
			throw new RuntimeException("Driver not created with capabilities: " + capabilities.toString());
		}
	}

	@AfterClass
	public void afterClass() {
		try {
			driver.close();
			driver.getCapabilities().getCapability("reportPdfUrl");
		} catch (Exception e) {
			e.printStackTrace();
		}

		driver.quit();
		System.out.println("Run ended");
	}

	@Parameters({"url"})
	@Test
	public void FM_Homepage(String url) throws Exception {
		reportiumClient = Utils.setReportiumClient(driver, reportiumClient); //Creates reportiumClient

		String reportUrl = reportiumClient.getReportUrl();
		int errorCount = 0;

		// Reporting client. For more details, see http://developers.perfectomobile.com/display/PD/Reporting
		try {


			reportiumClient.testStart("Demo - ADA validation with Axe (" +  url +  ")", new TestContext("ADA", "axe"));

			reportiumClient.stepStart("step 1: Load Page");
			driver.get(url);
			reportiumClient.stepStart("step 2: Capture Screenshot ");
			driver.getScreenshotAs(OutputType.BASE64);

			reportiumClient.stepStart("step 3: Running AXE Framework");
			AxeHelper axe = new AxeHelper(driver);
			axe.runAxe();

			reportiumClient.stepStart("Step 4: A11y violations");
			axe.startHighlighter("violations");

			final StringBuilder errors = new StringBuilder();

			while (true) {
				final Map<String, ?> violation = axe.nextHighlight();
				if (violation == null) {
					break;
				}

				errorCount++;
				final String ruleId = (String) violation.get("issue");
				final Map<String, String> node = (Map<String, String>) violation.get("node");

				final String impact = node.get("impact");
				final String summary = node.get("failureSummary");
				final String html = node.get("html");
				final String selector = (String) violation.get("target");

				final String message = String.format("%s - %s%n %s%n Selector:\t%s%n HTML:\t\t%s%n%n",
						impact, ruleId, summary, selector, html);

				driver.getScreenshotAs(OutputType.BASE64);
				reportiumClient.reportiumAssert(message,false);
				errors.append(message);

			}

			if (errorCount > 0) {
				final Capabilities capabilities = driver.getCapabilities();
				final String platform = String.valueOf(capabilities.getCapability("platformName"));
				final String version = String.valueOf(capabilities.getCapability("platformVersion"));
				final String browserName = String.valueOf(capabilities.getCapability("browserName"));
				final String browserVersion = String.valueOf(capabilities.getCapability("browserVersion"));

				String browserVersionFormatted;
				if ("null".equals(browserName)) {
					browserVersionFormatted = "default browser";
				} else {
					browserVersionFormatted = browserName + "-" + browserVersion;
				}

				String message = String.format("%n%s-%s %s : %d violations on %s%nReport Link: %s%n",
						platform, version, browserVersionFormatted, errorCount, url, reportUrl);


				message = String.format("%s%n%s%n", message, errors);

				throw new AccessibilityException(message);
			}

			reportiumClient.stepStart("Test Completed");
			reportiumClient.testStop(TestResultFactory.createSuccess());
		} catch (Exception e) {
			reportiumClient.testStop(TestResultFactory.createFailure(e.getMessage(), e));
			e.printStackTrace();

		}
	}
}
