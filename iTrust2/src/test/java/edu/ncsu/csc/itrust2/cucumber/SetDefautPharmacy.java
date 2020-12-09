package edu.ncsu.csc.itrust2.cucumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SetDefautPharmacy extends CucumberTest {

    private final String baseUrl  = "http://localhost:8080/iTrust2";

    // private final String encoded =
    // "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.";
    private final String pharmacy = "pharmacy";
    private final String address  = "112 somewhere road";

    /**
     * Pharmacy does exist
     */
    @Given ( "The desired pharmacy does exist" )
    public void existPharmacy () {
        driver.get( baseUrl );
        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( "admin" );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( "123456" );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('deletepharmacy').click();" );
        final WebElement name = driver.findElement( By.id( "name" ) );
        name.clear();
        name.sendKeys( this.pharmacy );

        final WebElement address = driver.findElement( By.id( "address" ) );
        address.clear();
        address.sendKeys( this.address );

        final WebElement state = driver.findElement( By.id( "state" ) );
        final Select dropdown = new Select( state );
        dropdown.selectByVisibleText( "California" );

        final WebElement zip = driver.findElement( By.id( "zip" ) );
        zip.clear();
        zip.sendKeys( "00112" );

        driver.findElement( By.id( "submit" ) ).click();
    }

    @When ( "I login as patient" )
    public void loginAsPatient () {
        attemptLogout();

        driver.get( baseUrl );
        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( "patient" );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( "123456" );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();

        assertEquals( "iTrust2: Patient Home", driver.getTitle() );
    }

    @When ( "I navigate to the Set default pharmacy page" )
    public void editDemographics () {
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('editdefaultpref-patient').click();" );
    }

    @When ( "I select the desired pharmacy" )
    public void selectDefault () {
        final Select dropdown = new Select( driver.findElement( By.id( "state" ) ) );
        dropdown.selectByVisibleText( "California" );
        final Select pha = new Select( driver.findElement( By.id( "selectedPharmacy" ) ) );
        pha.selectByVisibleText( pharmacy );
        driver.findElement( By.id( "brand" ) ).click();
        driver.findElement( By.id( "submit" ) ).click();
    }

    @Then ( "The pharmacy is set as default successfully" )
    public void assignedSuccessfully () {
        assertTrue( driver.getPageSource().contains( "Preferences successfully set!" ) );
    }
}
