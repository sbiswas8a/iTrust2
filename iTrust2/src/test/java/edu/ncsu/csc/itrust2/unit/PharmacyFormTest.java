package edu.ncsu.csc.itrust2.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ncsu.csc.itrust2.forms.admin.PharmacyForm;
import edu.ncsu.csc.itrust2.models.enums.State;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;

/**
 * Tests for the PharmacyForm class
 *
 * @author Sumit Biswas
 *
 */
public class PharmacyFormTest {

    /**
     * Test the PharmacyForm class.
     */
    @Test
    public void testPharmacyForm () {
        final Pharmacy pharmacy = new Pharmacy();
        pharmacy.setAddress( "somewhere" );
        pharmacy.setName( "hospital" );
        pharmacy.setState( State.NC );
        pharmacy.setZip( "27040" );
        final PharmacyForm form = new PharmacyForm( pharmacy );
        assertEquals( pharmacy.getAddress(), form.getAddress() );
        assertEquals( pharmacy.getName(), form.getName() );
        assertEquals( pharmacy.getState().getName(), form.getState() );
        assertEquals( pharmacy.getZip(), form.getZip() );
    }
}
