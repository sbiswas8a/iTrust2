package edu.ncsu.csc.itrust2.apitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.ncsu.csc.itrust2.config.RootConfiguration;
import edu.ncsu.csc.itrust2.forms.admin.DrugForm;
import edu.ncsu.csc.itrust2.forms.admin.PharmacyForm;
import edu.ncsu.csc.itrust2.forms.admin.UserForm;
import edu.ncsu.csc.itrust2.forms.hcp.PrescriptionForm;
import edu.ncsu.csc.itrust2.forms.hcp_patient.PatientForm;
import edu.ncsu.csc.itrust2.models.enums.Role;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.models.persistent.Prescription;
import edu.ncsu.csc.itrust2.mvc.config.WebMvcConfiguration;

/**
 * Class for testing prescription API.
 *
 * @author Connor
 */
@RunWith ( SpringJUnit4ClassRunner.class )
@ContextConfiguration ( classes = { RootConfiguration.class, WebMvcConfiguration.class } )
@WebAppConfiguration
public class APIPrescriptionTest {
    private MockMvc               mvc;

    private Gson                  gson;
    DrugForm                      drugForm;

    @Autowired
    private WebApplicationContext context;

    /**
     * Performs setup operations for the tests.
     *
     * @throws Exception
     */
    @Before
    @WithMockUser ( username = "admin", roles = { "USER", "ADMIN" } )
    public void setup () throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup( context ).build();
        gson = new GsonBuilder().create();
        final UserForm patientForm = new UserForm( "api_test_patient", "123456", Role.ROLE_PATIENT, 1 );
        mvc.perform( post( "/api/v1/users" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( patientForm ) ) );

        // Create drug for testing
        drugForm = new DrugForm();
        drugForm.setCode( "0000-0000-20" );
        drugForm.setName( "TEST" );
        drugForm.setDescription( "DESC" );
        mvc.perform( post( "/api/v1/drugs" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( drugForm ) ) );
    }

    /**
     * Tests basic prescription APIs.
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "hcp", roles = { "USER", "HCP", "ADMIN", "PHARMACIST" } )
    public void testPrescriptionAPI () throws Exception {

        final Pharmacy pharmacy = new Pharmacy( "iTrust Test Pharmacy", "1 iTrust Test Street", "27606", "NC" );
        pharmacy.save();
        final String id = pharmacy.getId().toString();

        // Create two prescription forms for testing
        final PrescriptionForm form1 = new PrescriptionForm();
        form1.setDrug( drugForm.getCode() );
        form1.setDosage( 100 );
        form1.setRenewals( 12 );
        form1.setPatient( "api_test_patient" );
        form1.setStartDate( "2009-10-10" ); // 10/10/2009
        form1.setEndDate( "2010-10-10" ); // 10/10/2010

        final PrescriptionForm form2 = new PrescriptionForm();
        form2.setDrug( drugForm.getCode() );
        form2.setDosage( 200 );
        form2.setRenewals( 3 );
        form2.setPatient( "api_test_patient" );
        form2.setStartDate( "2020-10-10" ); // 10/10/2020
        form2.setEndDate( "2020-11-10" ); // 11/10/2020

        // Add first prescription to system
        final String content1 = mvc
                .perform( post( "/api/v1/prescriptions/sendTo/" + id ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( form1 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();

        // Parse response as Prescription
        final Prescription p1 = gson.fromJson( content1, Prescription.class );
        final PrescriptionForm p1Form = new PrescriptionForm( p1 );
        assertEquals( form1.getDrug(), p1Form.getDrug() );
        assertEquals( form1.getDosage(), p1Form.getDosage() );
        assertEquals( form1.getRenewals(), p1Form.getRenewals() );
        assertEquals( form1.getPatient(), p1Form.getPatient() );
        assertEquals( form1.getStartDate(), p1Form.getStartDate() );
        assertEquals( form1.getEndDate(), p1Form.getEndDate() );

        // Add second prescription to system
        final String content2 = mvc
                .perform( post( "/api/v1/prescriptions/sendTo/" + id ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( form2 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();
        final Prescription p2 = gson.fromJson( content2, Prescription.class );
        final PrescriptionForm p2Form = new PrescriptionForm( p1 );
        assertEquals( form1.getDrug(), p2Form.getDrug() );
        assertEquals( form1.getDosage(), p2Form.getDosage() );
        assertEquals( form1.getRenewals(), p2Form.getRenewals() );
        assertEquals( form1.getPatient(), p2Form.getPatient() );
        assertEquals( form1.getStartDate(), p2Form.getStartDate() );
        assertEquals( form1.getEndDate(), p2Form.getEndDate() );

        // Verify prescriptions have been added
        final String allPrescriptionContent = mvc.perform( get( "/api/v1/prescriptions" ) ).andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();
        final List<Prescription> allPrescriptions = gson.fromJson( allPrescriptionContent,
                new TypeToken<List<Prescription>>() {
                }.getType() );
        assertTrue( allPrescriptions.size() >= 2 );

        // Edit first prescription
        p1.setDosage( 500 );
        final String editContent = mvc
                .perform( put( "/api/v1/prescriptions/sendTo/" + id ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( new PrescriptionForm( p1 ) ) ) )
                .andReturn().getResponse().getContentAsString();
        final Prescription edited = gson.fromJson( editContent, Prescription.class );
        assertEquals( p1.getId(), edited.getId() );
        assertEquals( p1.getDosage(), edited.getDosage() );

        // Get single prescription
        mvc.perform( put( "/api/v1/prescriptions/sendTo/" + id ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( new PrescriptionForm( p1 ) ) ) ).andReturn().getResponse()
                .getContentAsString();

        mvc.perform( get( "/api/v1/prescriptions/" + p1.getId() ) ).andExpect( status().isOk() );

        ////////////////////////
        mvc.perform( delete( "/api/v1/prescriptions/" + p1.getId() ) ).andExpect( status().isOk() )
                .andExpect( content().string( p1.getId().toString() ) );
        mvc.perform( delete( "/api/v1/prescriptions/" + p2.getId() ) ).andExpect( status().isOk() )
                .andExpect( content().string( p2.getId().toString() ) );

        // deleting test stuff
        pharmacy.delete();
        // deleting test stuff
    }

    /**
     * Tests filling / marking pickups for prescriptions
     *
     * @throws Exception
     *             on error
     */
    @Test
    @WithMockUser ( username = "pharmaMan", roles = { "USER", "HCP", "ADMIN", "PHARMACIST" } )
    public void testPharmacistActions () throws Exception {
        // Setup //
        final PharmacyForm form = new PharmacyForm();
        form.setAddress( "Some Corner" );
        form.setName( "Madicine" );
        form.setState( "NC" );
        form.setZip( "26707" );

        final String content2 = mvc
                .perform( post( "/api/v1/pharmacies/" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( form ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();
        // Parse response as Pharmacy
        final Pharmacy pharmacy = gson.fromJson( content2, Pharmacy.class );

        final UserForm pharmacist = new UserForm( "pharmaMan", "123456", Role.ROLE_PHARMACIST, 1 );
        mvc.perform( post( "/api/v1/users" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pharmacist ) ) );

        final UserForm patient = new UserForm( "patMan", "123456", Role.ROLE_PATIENT, 1 );
        mvc.perform( post( "/api/v1/users" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( patient ) ) );

        // creating personnelform
        final PatientForm pf = new PatientForm();
        pf.setSelf( "patMan" );
        pf.setAddress1( "Some Street" );
        pf.setAddress2( "Around the corner" );
        pf.setCity( "Raleigh" );
        pf.setEmail( "csc326f20.201.1@gmail.com" );
        pf.setFirstName( "Ward" );
        pf.setLastName( "Meachum" );
        pf.setPhone( "919-919-9119" );
        pf.setState( "CO" );
        pf.setZip( "42069" );
        pf.setDateOfBirth( "1995-02-22" );
        // updating patient demographics
        mvc.perform( put( "/api/v1/patients/patMan" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( pf ) ) ).andExpect( status().isOk() );

        // unassigning first so test reruns can work without errors
        mvc.perform( put( "/api/v1/pharmacists/pharmaMan/unassign" ) );
        mvc.perform( put( "/api/v1/pharmacies/" + pharmacy.getId() + "/pharmaMan" ) ).andExpect( status().isOk() );

        // Create two prescription forms for testing
        final PrescriptionForm form1 = new PrescriptionForm();
        form1.setDrug( drugForm.getCode() );
        form1.setDosage( 100 );
        form1.setRenewals( 12 );
        form1.setPatient( "patMan" );
        form1.setStartDate( "2009-10-10" ); // 10/10/2009
        form1.setEndDate( "2010-10-10" ); // 10/10/2010

        // Add first prescription to system
        final String content1 = mvc
                .perform( post( "/api/v1/prescriptions/sendTo/" + pharmacy.getId() )
                        .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( form1 ) ) )
                .andExpect( status().isOk() ).andReturn().getResponse().getContentAsString();
        // Parse response as Prescription
        final Prescription p1 = gson.fromJson( content1, Prescription.class );

        mvc.perform( put( "/api/v1/prescriptions/notify/" + p1.getId() ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( "Could not fill prescription!" ) ) ).andExpect( status().isOk() );

        mvc.perform( put( "/api/v1/prescriptions/fill/" + p1.getId() ) ).andExpect( status().isOk() );

        mvc.perform( put( "/api/v1/prescriptions/pickup/" + p1.getId() ) ).andExpect( status().isOk() );

        // deleting stuff to allow reruns
        mvc.perform( delete( "/api/v1/prescriptions/" + p1.getId() ) ).andExpect( status().isOk() );
        mvc.perform( delete( "/api/v1/pharmacies/" + pharmacy.getId() ) ).andExpect( status().isOk() );

    }

}
