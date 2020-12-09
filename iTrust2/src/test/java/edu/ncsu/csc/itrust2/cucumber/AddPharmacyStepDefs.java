package edu.ncsu.csc.itrust2.cucumber;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;

public class AddPharmacyStepDefs extends CucumberTest {

    private final String baseUrl      = "http://localhost:8080/iTrust2";

    private final String pharmacyName = "Pharmacy" + ( new Random() ).nextInt();

    /**
     * Pharmacy doesn't exist
     */
    @Given ( "The desired pharmacy doesn't exist" )
    public void noPharmacy () {
        attemptLogout();

        final List<Pharmacy> pharmacies = Pharmacy.getPharmacies();
        for ( final Pharmacy pharmacy : pharmacies ) {
            try {
                pharmacy.delete();
            }
            catch ( final Exception e ) {
                fail();
            }
        }
    }

    // /**
    // * Pharmacy does exist
    // */
    // @Given ( "The desired pharmacy does exist" )
    // public void existPharmacy () {
    // Pharmacy.getPharmacies().add( new Pharmacy( "pharmacy", "112 somewhere
    // road", "00112", "California" ) );
    // }

    /**
     * Admin login
     */
    @When ( "I login as admin" )
    public void loginAdminP () {
        driver.get( baseUrl );
        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( "admin" );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( "123456" );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();
    }

    /**
     * Navigate to add user page
     */
    @When ( "I navigate to the Add Pharmacy page" )
    public void addPharmacyPage () {
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('deletepharmacy').click();" );
    }

    /**
     * Fill in pharmacy form
     */
    @When ( "I fill in the values in the Add Pharmacy form" )
    public void fillPharmacyFields () {
        final WebElement name = driver.findElement( By.id( "name" ) );
        name.clear();
        name.sendKeys( pharmacyName );

        final WebElement address = driver.findElement( By.id( "address" ) );
        address.clear();
        address.sendKeys( "111 Somewhere Road" );

        final WebElement state = driver.findElement( By.id( "state" ) );
        final Select dropdown = new Select( state );
        dropdown.selectByVisibleText( "California" );

        final WebElement zip = driver.findElement( By.id( "zip" ) );
        zip.clear();
        zip.sendKeys( "00111" );

        driver.findElement( By.id( "submit" ) ).click();
    }

    /**
     * Create hospital successfully
     */
    @Then ( "The pharmacy is created successfully" )
    public void createdSuccessfully () {
        waitForAngular();
        assertTrue( driver.getPageSource().contains( "Pharmacy added successfully" ) );
    }

    // /**
    // * remove pharmacy
    // *
    // * @param name
    // * name of the pharmacy to remove
    // */
    // @Then ( "I delete the pharmacy with name (.+)$" )
    // public void deletePharmacy ( String name ) {
    // waitForAngular();
    // final WebElement e = driver.findElement( By.name( "DeleteButton" ) );
    // e.click();
    // waitForAngular();
    // }

}
