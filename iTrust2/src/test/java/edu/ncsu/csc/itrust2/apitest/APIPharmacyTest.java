package edu.ncsu.csc.itrust2.apitest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.ncsu.csc.itrust2.config.RootConfiguration;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.mvc.config.WebMvcConfiguration;

/**
 * Test for API functionality for interacting with Hospitals
 *
 * @author Caleb Hughes
 * @author Sumit Biswas
 *
 */
@RunWith ( SpringJUnit4ClassRunner.class )
@ContextConfiguration ( classes = { RootConfiguration.class, WebMvcConfiguration.class } )
@WebAppConfiguration

public class APIPharmacyTest {

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
     * Tests getting a non existent Pharmacy and ensures that the correct status
     * is returned.
     *
     * @throws Exception
     *             on error
     */
    @Test
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testNonExistentPharmacy () throws Exception {
        mvc.perform( get( "/api/v1/pharmacies/-1" ) ).andExpect( status().isNotFound() );
        mvc.perform( delete( "/api/v1/pharmacies/0" ) ).andExpect( status().isNotFound() );
    }

    /**
     * Test retrieval of prescriptions from a given Pharmacy
     *
     * Separate test method since mock user needs to be a pharmacist
     *
     * @throws Exception
     *             on error
     */
    @Test
    @WithMockUser ( username = "pharmacist", roles = { "PHARMACIST" } )
    public void testGetPrescriptions () throws Exception {
        final Pharmacy p = new Pharmacy( "Test Pharmacy", "Test Street", "27606", "NC" );
        p.save();
        final String id = p.getId().toString();
        mvc.perform( get( "/api/v1/pharmacies/" + id + "/prescriptions" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8_VALUE ) );
        p.delete();
    }

    /**
     * Tests PharmacyAPI
     *
     * @throws Exception
     *             on error
     */
    @Test
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testPharmacyAPI () throws Exception {
        final Pharmacy pharmacy = new Pharmacy( "iTrust Test Pharmacy", "1 iTrust Test Street", "27606", "NC" );
        final Pharmacy pharmacy2 = new Pharmacy( "iTrust Test Pharmacy", "2 iTrust Test Street", "27607", "NC" );
        final Pharmacy pharmacy3 = new Pharmacy( "Very Different Pharmacy", "1 iTrust Test Street", "27606", "NC" );

        mvc.perform( post( "/api/v1/pharmacies" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacy ) ) );
        // Can post a pharmacy with the same name but different address
        mvc.perform( post( "/api/v1/pharmacies" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacy2 ) ) );
        // Expecting a conflict when the same pharmacy is posted again
        mvc.perform( post( "/api/v1/pharmacies" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacy ) ) ).andExpect( status().isConflict() );
        // Expecting a conflict when a pharmacy with a different name is posted
        // that has the same
        // address as an existing one
        mvc.perform( post( "/api/v1/pharmacies" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacy3 ) ) ).andExpect( status().isConflict() );

        pharmacy.save(); // saving since auto generated id cannot be retrieved
                         // without persistent object
        final String id = pharmacy.getId().toString();
        mvc.perform( get( "/api/v1/pharmacies/" + id ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8_VALUE ) );

        // Make sure that the put didn't break anything
        pharmacy2.save(); // saving since auto generated id cannot be retrieved
                          // without persistent object
        final String id2 = pharmacy2.getId().toString();
        mvc.perform( get( "/api/v1/pharmacies/" + id2 ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8_VALUE ) );

        // Editing a non-existent pharmacy should not work
        mvc.perform( put( "/api/v1/pharmacies/-1" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacy ) ) ).andExpect( status().isNotFound() );

        // Testing delete mapping
        mvc.perform( delete( "/api/v1/pharmacies/" + id ) ).andExpect( status().isOk() );

        // Testing retrieval of pharmacies via state
        mvc.perform( get( "/api/v1/pharmacies/state/NC" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8_VALUE ) );

        // Testing retrieval of all pharmacies
        mvc.perform( get( "/api/v1/pharmacies" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8_VALUE ) );

        // DELETING ALL PHARMACIES PERSISTED FOR THE TEST
        pharmacy2.delete();

    }
}
