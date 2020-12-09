package edu.ncsu.csc.itrust2.cucumber;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.ncsu.csc.itrust2.models.persistent.User;

/**
 * Step definitions for adding a pharmacist feature
 */
public class AddPharmacistStepDefs extends CucumberTest {
    private final String baseUrl      = "http://localhost:8080/iTrust2";
    // private final String jenkinsUname = "jenkins" + ( new Random()
    // ).nextInt();
    private final String jenkinsUname = "jenkins";                                                     // no
                                                                                                       // randomizer
                                                                                                       // in
                                                                                                       // name
                                                                                                       // since
                                                                                                       // different
                                                                                                       // tests
                                                                                                       // rerun
                                                                                                       // the
                                                                                                       // class
                                                                                                       // and
                                                                                                       // regenerate
                                                                                                       // a
                                                                                                       // new
                                                                                                       // user
                                                                                                       // name
    private final String username     = "pharmacist";
    private final String password     = "123456";
    private final String encoded      = "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.";
    private final String pharmacy     = "pharmacy";
    private final String address      = "112 somewhere road";

    /**
     * Check for no user
     */
    @Given ( "The desired pharmacist does not exist" )
    public void noUser () {
        attemptLogout();

        final List<User> users = User.getUsers();
        for ( final User user : users ) {
            if ( user.getUsername().equals( jenkinsUname ) ) {
                try {
                    user.delete();
                }
                catch ( final Exception e ) {
                    Assert.fail();
                }
            }
        }
    }

    /**
     * Pharmacy does exist
     */
    @Given ( "The desired Pharmacy does exist" )
    public void existPharmacy () {
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

    /**
     * method that ensures the pharmacist does exist
     */
    @Given ( "The desired pharmacist does exist" )
    public void existPharmacist () {
        assertTrue( User.getByName( jenkinsUname ) != null );
        driver.get( baseUrl );
        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( "admin" );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( "123456" );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('users').click();" );
        final WebElement username1 = driver.findElement( By.id( "username" ) );
        username1.clear();
        username1.sendKeys( this.username );

        final WebElement password1 = driver.findElement( By.id( "password" ) );
        password1.clear();
        password1.sendKeys( this.password );

        final WebElement password2 = driver.findElement( By.id( "password2" ) );
        password2.clear();
        password2.sendKeys( this.password );

        final Select role = new Select( driver.findElement( By.id( "role" ) ) );
        role.selectByVisibleText( "Pharmacist HCP" );

        final WebElement enabled = driver.findElement( By.name( "enabled" ) );
        enabled.click();

        driver.findElement( By.id( "submit" ) ).click();
    }

    /**
     * Admin log in
     */
    @When ( "I login as an admin" )
    public void loginAdmin () {
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
     * pharmacist log in
     */
    @When ( "I login as an pharmacist" )
    public void loginPharmacist () {
        driver.get( baseUrl );
        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( this.username );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( this.password );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();
    }

    /**
     * Navigate to add user page
     */
    @When ( "I navigate to the add user page" )
    public void addUserPage () {
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('users').click();" );
    }

    /**
     * Navigate to the pharmacy page
     */
    @When ( "I navigate to the Pharmacy page" )
    public void addPharmacyPage () {
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('deletepharmacy').click();" );
    }

    @When ( "I navigate to the Pharmacist page" )
    public void pharmacistPage () {
        ( (JavascriptExecutor) driver ).executeScript( "document.getElementById('deletepharmacist').click();" );
    }

    @When ( "I choose the pharmacy and pharmacist to assign" )
    public void assignPharmacist () {
        final Select pha = new Select( driver.findElement( By.id( "selectedPharmacy" ) ) );
        pha.selectByVisibleText( pharmacy + ", CA - 00112" );
        final Select pharmacist = new Select( driver.findElement( By.id( "selectedPharmacist" ) ) );
        pharmacist.selectByVisibleText( username );
        driver.findElement( By.id( "submit" ) ).click();
    }

    /**
     * Fill in add user values
     */
    @When ( "I fill in the values in the add user form with role Pharmacist HCP" )
    public void fillFields () {
        final WebElement username = driver.findElement( By.id( "username" ) );
        username.clear();
        username.sendKeys( jenkinsUname );

        final WebElement password = driver.findElement( By.id( "password" ) );
        password.clear();
        password.sendKeys( "123456" );

        final WebElement password2 = driver.findElement( By.id( "password2" ) );
        password2.clear();
        password2.sendKeys( "123456" );

        final Select role = new Select( driver.findElement( By.id( "role" ) ) );
        role.selectByVisibleText( "Pharmacist HCP" );

        final WebElement enabled = driver.findElement( By.name( "enabled" ) );
        enabled.click();

        driver.findElement( By.id( "submit" ) ).click();

    }

    /**
     * Create user
     */
    @Then ( "The pharmacist is created successfully" )
    public void createdSuccessfully () {
        assertTrue( driver.getPageSource().contains( "User added successfully" ) );
    }

    /**
     * Check that the pharmacist is successfully assigned
     */
    @Then ( "The pharmacist is assigned to that pharmacy successfully" )
    public void assignedSuccessfully () {
        assertTrue( driver.getPageSource().contains( "Successfully assigned pharmacist to pharmacy" ) );
    }

    /**
     * User login
     */
    @Then ( "The new pharmacist can login" )
    public void tryLogin () {
        driver.findElement( By.id( "logout" ) ).click();

        final WebElement username = driver.findElement( By.name( "username" ) );
        username.clear();
        username.sendKeys( jenkinsUname );
        final WebElement password = driver.findElement( By.name( "password" ) );
        password.clear();
        password.sendKeys( "123456" );
        final WebElement submit = driver.findElement( By.className( "btn" ) );
        submit.click();
        /**
         * Not an assert statement in the typical sense, but we know that we can
         * log in if we can find the "iTrust" button in the top-left after
         * attempting to do so.
         */
        try {
            waitForAngular();
            driver.findElement( By.linkText( "iTrust2" ) );
        }
        catch ( final Exception e ) {
            fail();
        }
    }
}
