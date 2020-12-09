package edu.ncsu.csc.itrust2.apitest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.ncsu.csc.itrust2.config.RootConfiguration;
import edu.ncsu.csc.itrust2.models.enums.Role;
import edu.ncsu.csc.itrust2.models.persistent.Personnel;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.models.persistent.User;
import edu.ncsu.csc.itrust2.mvc.config.WebMvcConfiguration;

/**
 * Test for API functionality for interacting with Pharmacists
 *
 * @author Caleb Hughes
 * @author Sumit Biswas
 *
 */
@RunWith ( SpringJUnit4ClassRunner.class )
@ContextConfiguration ( classes = { RootConfiguration.class, WebMvcConfiguration.class } )
@WebAppConfiguration
public class APIPharmacistTest {

    private MockMvc               mvc;

    @Autowired
    private WebApplicationContext context;

    /**
     * Test set up
     */
    @Before
    public void setup () {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
    }

    /**
     * Tests PharmacistAPI
     *
     * @throws Exception
     *             on error
     */
    @Test
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testPharmacistAPI () throws Exception {
        // Creating the pharmacy
        final Pharmacy pharmacy = new Pharmacy( "iTrust Test Pharmacy", "1 iTrust Test Street", "27606", "NC" );
        pharmacy.save();
        final String id = pharmacy.getId().toString();

        // Creating the user
        final User user = new User( "name", "pw", Role.ROLE_PHARMACIST, Integer.valueOf( 1 ) );
        user.save();
        final Personnel pharmacist = Personnel.getEmptyProfile( user );
        pharmacist.save();

        // Testing get
        mvc.perform( get( "/api/v1/pharmacists" ) ).andExpect( status().isOk() );

        // Testing invalid assign (nonexistent pharmacist)
        mvc.perform( put( "/api/v1/pharmacies/" + id + "/noname" ) ).andExpect( status().isNotFound() );
        // Testing invalid assign (nonexistent pharmacy)
        mvc.perform( put( "/api/v1/pharmacies/-2/name" ) ).andExpect( status().isNotFound() );

        // Testing valid assign
        mvc.perform( put( "/api/v1/pharmacies/" + id + "/name" ) ).andExpect( status().isOk() );
        // Testing valid unassign
        mvc.perform( put( "/api/v1/pharmacists/name/unassign" ) ).andExpect( status().isOk() );
        // Testing invalid unassign
        mvc.perform( put( "/api/v1/pharmacists/noname/unassign" ) ).andExpect( status().isNotFound() );

        // Delete stuff persisted for tests
        pharmacist.delete();
        user.delete();
        pharmacy.delete();
    }
}
