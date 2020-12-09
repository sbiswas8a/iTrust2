package edu.ncsu.csc.itrust2.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ncsu.csc.itrust2.models.enums.State;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;

/**
 * Tests for the HospitalForm class
 *
 * @author Sumit Biswas
 *
 */
public class PharmacyTest {

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
        pharmacy.save();

        final Long id = pharmacy.getId();
        System.out.println( "Id is: " + id );

        final Pharmacy pharma = Pharmacy.getById( id );
        assertEquals( pharma.getId(), pharmacy.getId() );
        assertEquals( pharma.getName(), pharmacy.getName() );
        assertEquals( pharma.getAddress(), pharmacy.getAddress() );
        assertEquals( pharma.getInstitutionType(), pharmacy.getInstitutionType() );
        pharmacy.delete();
    }
}
