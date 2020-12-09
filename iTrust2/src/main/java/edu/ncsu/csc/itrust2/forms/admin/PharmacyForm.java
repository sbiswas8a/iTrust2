package edu.ncsu.csc.itrust2.forms.admin;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;

/**
 * Form used for creating a new Pharmacy. Will be parsed into an actual Pharmacy
 * object to be saved.
 *
 * @author Sumit Biswas
 *
 */
public class PharmacyForm {

    /**
     * Name of the Pharmacy
     */
    @NotEmpty
    @Length ( max = 255 )
    private String name;

    /**
     * Address of the Pharmacy
     */
    @NotEmpty
    @Length ( max = 255 )
    private String address;

    /**
     * ZIP Code of the Pharmacy
     */
    @NotEmpty
    @Length ( min = 5, max = 10 )
    private String zip;

    /**
     * State of the Pharmacy
     */
    @NotEmpty
    @Length ( max = 255 )
    private String state;

    /**
     * Creates an empty PharmacyForm object. Used by the controllers for filling
     * out a new Pharmacy.
     */
    public PharmacyForm () {
    }

    /**
     * Creates a PharmacyForm from the Pharmacy provided. Used to convert a
     * Pharmacy to a form that can be edited.
     *
     * @param p
     *            Pharmacy to convert to its Form.
     */
    public PharmacyForm ( final Pharmacy p ) {
        setName( p.getName() );
        setAddress( p.getAddress() );
        setZip( p.getZip() );
        setState( p.getState().getName() );
    }

    /**
     * Gets the name of the Pharmacy from this PharmacyForm
     *
     * @return Name of the Pharmacy
     */
    public String getName () {
        return name;
    }

    /**
     * Sets the name of the Pharmacy in this PharmacyForm
     *
     * @param name
     *            New Name of the Pharmacy
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * Gets the Address of the Pharmacy in this PharmacyForm.
     *
     * @return The address of the Pharmacy
     */
    public String getAddress () {
        return address;
    }

    /**
     * Sets the Address of this Pharmacy.
     *
     * @param address
     *            New Address to set.
     */
    public void setAddress ( final String address ) {
        this.address = address;
    }

    /**
     * Gets the ZIP code of the Pharmacy in this form
     *
     * @return ZIP code of the Pharmacy
     */
    public String getZip () {
        return zip;
    }

    /**
     * Sets the ZIP code of this Pharmacy
     *
     * @param zip
     *            ZIP code to set for the Pharmacy
     */
    public void setZip ( final String zip ) {
        this.zip = zip;
    }

    /**
     * Retrieves the State of this Pharmacy
     *
     * @return State of the Pharmacy
     */
    public String getState () {
        return state;
    }

    /**
     * Sets the State of the Pharmacy
     *
     * @param state
     *            New State to set
     */
    public void setState ( final String state ) {
        this.state = state;
    }

}
