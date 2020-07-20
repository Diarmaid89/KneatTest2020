package kneatProject;

/**
 * @author Diarmaid Haugh
 * @version v1.1 17/07/20
 */

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;


import org.junit.Before;
import org.junit.Ignore;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class KneatTests {
	
	private WebDriver driver;
	
	//This will run before each test -to set the type and location of the driver.
	@Before
	public void setup()
	{
		//This following line will need to be edited - depending on where the driver has been installed.
		System.setProperty("webdriver.chrome.driver", "C:\\Chromedriver\\chromedriver.exe");
		driver = new ChromeDriver();
		navigateToPage();
	}
	
	//This will run after each test just to close the driver window once the test is complete.
	@After
	public void tearDown() throws InterruptedException
	{
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.close();
        driver.quit();
	}

	public void navigateToPage()
	{
		driver.get("http://booking.com");
		driver.manage().window().maximize();
	}
	
	//This method will enter all the required details for booking a room in a hotel before coming to the filters that need to be tested.
	public void enterReservationDetails() throws InterruptedException
	{
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		/*
		 * Writing the following WebElement search in the form of a list allows flexibility in terms of whether the cookie popup
		 * appears on the page or not. It pops up at random and if it does, it can be accepted, if it doesn't, the test won't
		 *fail due to a 'WebElementNotFound' exception.
		 */
		List<WebElement> cookie1 = driver.findElements(By.xpath("//*[text()='Accept']"));
		if(cookie1.size() > 0)
		{
			cookie1.get(0).click();
		}

		//Thread.sleep(5000);
		
		WebElement location = driver.findElement(By.id("ss"));
		location.sendKeys("Limerick");
		Thread.sleep(5000);
		
		Actions builder = new Actions(driver);
		Action selectLocation = builder.sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ENTER).build();
		selectLocation.perform();
		
		//This bit of code gets today's date from the booking.com calendar - instead of using the Calendar class from java
		Thread.sleep(5000);
		WebElement today = driver.findElement(By.cssSelector("td[class='bui-calendar__date bui-calendar__date--today']"));
		String date = today.getText();
		//Here I'm pulling the full date from the 'data-date' attribute
		String fullDate = today.getAttribute("data-date");
		//Separating the date into substrings to get the month and year
		String month = fullDate.substring(5, 7);
		String year = fullDate.substring(0, 4);
		Integer monthInteger = Integer.parseInt(month);
		Integer yearInteger = Integer.parseInt(year);
		
		//Changing the date & year to 3 months from now (year only affected during Oct, Nov and Dec)
		if(monthInteger < 7)
		{
			monthInteger += 3;
			month = "0" + monthInteger.toString();
		}
		else if(monthInteger == 10)
		{
			monthInteger = 1;
			yearInteger += 1;
			month = "0" + monthInteger.toString();
			year = yearInteger.toString();
		}
		else if(monthInteger == 11)
		{
			monthInteger = 2;
			yearInteger += 1;
			month = "0" + monthInteger.toString();
			year = yearInteger.toString();
		}
		else if(monthInteger == 12)
		{
			monthInteger = 3;
			yearInteger += 1;
			month = "0" + monthInteger.toString();
			year = yearInteger.toString();
		}
		else
		{
			monthInteger += 3;
			month = monthInteger.toString();
		}
		
		WebElement monthSelector = driver.findElement(By.cssSelector("div[class='bui-calendar__control bui-calendar__control--next']"));
		monthSelector.click();
		monthSelector.click();
		
		//This bit of code selects the date 3 months from now
		String dateSelection = "td[data-date='" + year + "-" + month + "-" + date + "']";
		//System.out.println("dateSelection: " + dateSelection);
				
		WebElement threeMonthsLater = driver.findElement(By.cssSelector(dateSelection));
		
		builder.moveToElement(threeMonthsLater).click().perform();
		
		//Select the checkout date - for a one night stay
		Integer checkout = Integer.parseInt(date);
		checkout += 1;
		String checkoutDate = "td[data-date='" + year + "-" + month + "-" + checkout.toString() + "']";
		WebElement threeMonthsLaterCheckout = driver.findElement(By.cssSelector(checkoutDate));
		builder.moveToElement(threeMonthsLaterCheckout).click().perform();
		
		Thread.sleep(5000);
		
		//Hit the 'Search' button
		//Note - the buttons class has an extra blank space at the end, it was poorly named and was causing my program to crash
		//until I realized what was happening and added an extra blank space to the below class name
		WebElement search = driver.findElement(By.cssSelector("button[class='sb-searchbox__button ']"));
		search.click();
		
		Thread.sleep(5000);
	}
	
	//This test will filter the hotels based on whether they have a 5 Star rating or not.
	//@Ignore
	@Test
	public void fiveStarFilter() throws InterruptedException
	{
		enterReservationDetails();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		try
		{
			WebElement fiveStarFilter = driver.findElement(By.partialLinkText("5 stars"));
			fiveStarFilter.click();
			
			Thread.sleep(5000);
			
			//Here I will add all the hotel names displayed after selecting the 5 star filter to a list
			List<WebElement> hotelNames = driver.findElements(By.cssSelector("a[class='hotel_name_link url']"));
			Boolean savoyResult = false;
			Boolean georgeResult = false;
			//Here I will run through the list and check for the Savoy and the George hotel
			for(WebElement e : hotelNames)
			{
				if(e.getText().contains("The Savoy Hotel"))
				{
					savoyResult = true;
					System.out.println("The Savoy is on the list." + "\n" +
							"The test to use the 5 Star filter has succeeded.");
				}
				
				if(e.getText().contains("George Limerick Hotel"))
				{
					georgeResult = true;
					System.out.println("The George is on the list." + "\n" +
							"The test has failed.");
				}
			}
			
			Assert.assertTrue("The Savoy is not in this list.", savoyResult);
			Assert.assertFalse("The George is on the list, it should not be.", georgeResult);
			
			Thread.sleep(5000);
		}
		catch(NoSuchElementException e)
		{
			System.out.println("An exception has occured, the web element can't be found.");
			System.out.println("The error is: " + e.getMessage());
		}
		catch(WebDriverException w)
		{
			System.out.println("An error has occured." + "\n" + "This test will now close");
			System.out.println("The error is: " + w.getMessage());
		}
	}

	//This test will filter the hotels based on whether they have a sauna or not.
	//@Ignore
	@Test
	public void saunaFilter() throws InterruptedException, NoSuchElementException
	{
		enterReservationDetails();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		Actions builder = new Actions(driver);
		
		try
		{
			//Select the 'Show More' button to bring up the options the will include 'Sauna'
			WebElement showMore = driver.findElement(By.xpath("//*[@id=\"filter_facilities\"]/div[2]/button"));
			String showMoreText = showMore.getText();
			if(showMoreText.contains("Show all 13"))
			{
				System.out.println("The 'Show all 13' filter appeared, this test will now stop." + "\n" +
						"Please read my bug report for further details.");			
			}
			else
			{
				showMore.click();
				Thread.sleep(5000);
				
				//Action Driver
				Action saunaSearch = builder.sendKeys(Keys.TAB).sendKeys(Keys.TAB).sendKeys("Sauna").build();
				saunaSearch.perform();
				Action selectFilter = builder.sendKeys(Keys.TAB).sendKeys(Keys.SPACE).build();
				selectFilter.perform();
				
				Thread.sleep(5000);
				
				WebElement applyFilter = driver.findElement(By.cssSelector("span[class='bui-button__text']"));
				builder.moveToElement(applyFilter).click().perform();
				
				List<WebElement> hotelNames = driver.findElements(By.cssSelector("a[class='hotel_name_link url']"));
				Boolean strandResult = false;
				Boolean georgeResult = false;
				//Here I will run through the list and check for the Savoy and the George hotel
				for(WebElement e : hotelNames)
				{
					if(e.getText().contains("Limerick Strand Hotel"))
					{
						strandResult = true;
						System.out.println("The Limerick Strand Hotel is on the list." + "\n" +
								"The test to filter hotels possessing a sauna has succeeded.");
					}
					
					if(e.getText().contains("George Limerick Hotel"))
					{
						georgeResult = true;
						System.out.println("The George is on the list, it shouldn't be.");
					}
				}
				
				Assert.assertTrue("The Savoy is not in this list.", strandResult);
				Assert.assertFalse("The George is on the list, it shouldn't be.", georgeResult);
						
				Thread.sleep(5000);
			}
		}
		catch(NoSuchElementException e)
		{
			System.out.println("An exception has occured, the web element can't be found."  + "\n" + "This test will now close");
			System.out.println("The error is: " + e.getMessage());
		}
		catch(WebDriverException w)
		{
			System.out.println("An error has occured." + "\n" + "This test will now close");
			System.out.println("The error is: " + w.getMessage());
		}
	}
	
	//This test will filter hotels to only show those in the city center
	//@Ignore
	@Test
	public void cityCentre() throws InterruptedException
	{
		enterReservationDetails();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		try
		{
			WebElement cityCentre = driver.findElement(By.cssSelector("a[data-id='di-8523']"));
			cityCentre.click();
			Thread.sleep(5000);
			
			List<WebElement> hotelNames = driver.findElements(By.cssSelector("a[class='hotel_name_link url']"));
			//I want the test to check whether the Absolute Hotel and the Castletroy hotel are in the city center (The Castletroy hotel shouldn't be)
			Boolean absoluteResult = false;
			Boolean castletroyResult = false;
			
			for(WebElement e : hotelNames)
			{
				if(e.getText().contains("Absolute Hotel Limerick"))
				{
					absoluteResult = true;
					System.out.println("The Absolute Hotel is on the list."  + "\n" +
							"The test to filter hotels based in the city center has succeeded.");
				}
				if(e.getText().contains("Castletroy Park Hotel"))
				{
					castletroyResult = true;
					System.out.println("The Castletroy Park Hotel is on the list, it shouldn't be.");
				}
			}
			Thread.sleep(5000);
			Assert.assertTrue("The Absolute Hotel is not on the list", absoluteResult);
			Assert.assertFalse("The Castletroy Park Hotel is on the list, it shouldn't be.", castletroyResult);
		}
		catch(ElementNotVisibleException nv)
		{
			System.out.println("The Filter has not appeared. The cityCentre test will now close.");
			System.out.println("The error is: " + nv.getMessage());
		}
	}
	
	//A test that will use the Map function on the search results page
	//@Ignore
	@Test
	public void searchWithMap() throws InterruptedException
	{
		enterReservationDetails();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		try
		{
			WebElement swimmingPool = driver.findElement(By.cssSelector("a[data-id='hotelfacility-301']"));
			swimmingPool.click();
			
			Thread.sleep(5000);
			
			WebElement map = driver.findElement(By.cssSelector("span[class='switch-map-view']"));
			map.click();
			
			Thread.sleep(5000);

			WebElement distance1KM = driver.findElement(By.cssSelector("a[data-id='distance-1000']"));
			distance1KM.click();

			Thread.sleep(5000);
			
			List<WebElement> hotels = driver.findElements(By.cssSelector("span[class='map-card__title-link']"));
			Boolean clayton = false;
			Boolean greenhills = false;
			for(WebElement e : hotels)
			{
				if(e.getText().contains("Clayton"))
				{
					clayton = true;
					System.out.println("The Clayton Hotel is on the list." + "\n" +
					"The test to open the map feature and filter by a distance of 1km has succeeded.");
				}
				if(e.getText().contains("Greenhills"))
				{
					greenhills = true;
					System.out.println("The Greenhills Hotel is on the list, it shouldn't be.");
				}
			}
			Assert.assertTrue("The Clayton Hotel is not on the list", clayton);
			Assert.assertFalse("The Greenhills Hotel is on the list, it shouldn't be.", greenhills);
		}
		catch(NoSuchElementException e)
		{
			System.out.println("An exception has occured, the web element can't be found.");
			System.out.println("The error is: " + e.getMessage());
		}
		catch(WebDriverException w)
		{
			System.out.println("An error has occured." + "\n" + "This test will now close");
			System.out.println("The error is: " + w.getMessage());
		}
	}
	
	//This test will enter information to book a flight, check the price and reach the checkout area
	//@Ignore
	@Test
	public void bookFlight() throws InterruptedException
	{
		Actions builder = new Actions(driver);
		
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		WebElement flightsLink = driver.findElement(By.linkText("Flights"));
		flightsLink.click();
		
		Thread.sleep(5000);
		
		/*
		 * Writing the following WebElement search in the form of a list allows flexibility in terms of whether the cookie popup
		 * appears on the page or not. It pops up at random and if it does, it can be accepted, if it doesn't, the test won't
		 * fail due to a 'WebElementNotFound' exception.
		 */
		List<WebElement> cookie1 = driver.findElements(By.xpath("//*[text()='Accept']"));
		if(cookie1.size() > 0)
		{
			cookie1.get(0).click();
		}
		
		try
		{
			WebElement direct = driver.findElement(By.cssSelector("div[class='bui-form__group css-1gtanqs']"));
			direct.click();
			
			WebElement oneWay = driver.findElement(By.className("css-1kmv57l-radio-input"));
			oneWay.click();
			
			WebElement destination = driver.findElement(By.cssSelector("input[placeholder='Where to?']"));
			destination.click();
			
			WebElement enterDestination = driver.findElement(By.cssSelector("input[data-testid='searchbox_destination_input']"));
			enterDestination.sendKeys("Munich");
			
			Thread.sleep(5000);
			
			builder.sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.RETURN).build().perform();
			
			Thread.sleep(5000);
			
			WebElement departureDate = driver.findElement(By.cssSelector("div[aria-label='Wed Oct 21 2020']"));
			departureDate.click();
			
			WebElement searchButton = driver.findElement(By.cssSelector("button[data-testid='searchbox_submit']"));
			searchButton.click();
			
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			
			WebElement getPrice = driver.findElement(By.cssSelector("div[class='bui-f-font-display_one']"));
			String price = getPrice.getText();
			
			Double priceComparison = Double.parseDouble(price.substring(1));
			
			if(priceComparison < 100)
			{
				System.out.println("The plane ticket costs " + price + " - not a bad price.");
			}
			else
			{
				System.out.println("The plane ticket costs " + price + " - bit steep.");
			}
			
			Thread.sleep(5000);
			
			WebElement selectFlight = driver.findElement(By.cssSelector("button[data-testid='searchresults_select_flight']"));
			selectFlight.click();
			
			Thread.sleep(5000);
			
			WebElement selectFlightAgain = driver.findElement(By.className("css-qpzlno"));
			selectFlightAgain.click();
			
			Thread.sleep(5000);
			
			//Check to see if I've reached the 'checkout' page by getting the page's URL
			Boolean atCheckout = false;
			String currentURL = driver.getCurrentUrl();
			
			if(currentURL.contains("checkout"))
			{
				atCheckout = true;
				System.out.println("You have reached the checkout." + "\n" + "The test to book a flight has succeeded.");
			}
			
			Assert.assertTrue("Something went wrong, you haven't reached the checkout.", atCheckout);
			
			Thread.sleep(5000);
		}
		catch(NoSuchElementException e)
		{
			System.out.println("An exception has occured, the web element can't be found."  + "\n" + "There may be no flights to select.");
			System.out.println("The error is: " + e.getMessage());
		}
		catch(WebDriverException w)
		{
			System.out.println("An error has occured." + "\n" + "This test will now close");
			System.out.println("The error is: " + w.getMessage());
		}
	}
}
