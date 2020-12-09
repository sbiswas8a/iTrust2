package edu.ncsu.csc.itrust2.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ncsu.csc.itrust2.models.persistent.InstitutionType;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;

/**
 * Testing the InstitutionType class
 *
 * @author Sumit Biswas
 *
 */
public class InstitutionTypeTest {

    /** Test Method */
    @Test
    public void testInstitutionType () {
        // InstitutionType.deleteAll( InstitutionType.class );
        final InstitutionType pharmacy = InstitutionType.getInstance( "Pharmacy" );
        assertEquals( "Pharmacy", pharmacy.getName() );
        final Pharmacy check = new Pharmacy( "Hogarth Pharma", "1245 Birch Street", "10014", "New York" );
        assertEquals( "Pharmacy", check.getInstitutionType().getName() );

        final InstitutionType random = InstitutionType.getInstance( "SomeInstitution" );
        assertEquals( "SomeInstitution", random.getName() );

    }
}
