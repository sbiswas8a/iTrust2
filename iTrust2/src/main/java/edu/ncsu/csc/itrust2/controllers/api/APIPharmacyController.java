package edu.ncsu.csc.itrust2.controllers.api;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.itrust2.forms.admin.PharmacyForm;
import edu.ncsu.csc.itrust2.models.enums.TransactionType;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.models.persistent.Prescription;
import edu.ncsu.csc.itrust2.utils.LoggerUtil;

/**
 * Class that provides REST API endpoints for the Pharmacy model. In all
 * requests made to this controller, the {id} provided is a String that is the
 * name of the Pharmacy desired.
 *
 * @author Sumit Biswas
 *
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIPharmacyController extends APIController {

    /**
     * Helper method to break the bidirectional link between pharmacy and
     * prescriptions before serializing
     *
     * @param pharmacy
     *            is the pharmacy where the links are being broken
     */
    private static void breakRecursion ( final Pharmacy pharmacy ) {
        final Set<Prescription> prescriptions = pharmacy.getPrescriptions();
        for ( final Prescription p : prescriptions ) {
            p.setPharmacy( null );
        }
    }

    /**
     * Retrieves a list of all Pharmacies in the database
     *
     * @return list of Pharmacies
     */
    @GetMapping ( BASE_PATH + "/pharmacies" )
    public List<Pharmacy> getPharmacies () {
        final List<Pharmacy> all = Pharmacy.getPharmacies();
        for ( final Pharmacy pharmacy : all ) {
            pharmacy.nullPrescriptions();
        }
        return all;
    }

    /**
     * Retrieves a list of all Pharmacies in the given State
     *
     * @param state
     *            is the state to fetch pharmacies from
     *
     * @return list of Pharmacies
     */
    @GetMapping ( BASE_PATH + "/pharmacies/state/{state}" )
    public List<Pharmacy> getPharmacies ( @PathVariable ( "state" ) final String state ) {
        final List<Pharmacy> all = Pharmacy.getAllinState( state );
        for ( final Pharmacy pharmacy : all ) {
            pharmacy.nullPrescriptions();
        }
        return all;
    }

    /**
     * Retrieves the Pharmacy specified by the name provided
     *
     * @param id
     *            The name of the Pharmacy
     * @return response
     */
    @GetMapping ( BASE_PATH + "/pharmacies/{id}" )
    public ResponseEntity getPharmacy ( @PathVariable ( "id" ) final Long id ) {
        final Pharmacy pharmacy = Pharmacy.getById( id );
        if ( null != pharmacy ) {
            LoggerUtil.log( TransactionType.VIEW_PHARMACY, LoggerUtil.currentUser() );
            pharmacy.nullPrescriptions();
            return new ResponseEntity( pharmacy, HttpStatus.OK );
        }
        return new ResponseEntity( errorResponse( "No pharmacy found for name " + id ), HttpStatus.NOT_FOUND );
    }

    /**
     * Creates a new Pharmacy from the RequestBody provided.
     *
     * @param pharmacyF
     *            The Pharmacy to be validated and saved to the database.
     * @return response
     */
    @PostMapping ( BASE_PATH + "/pharmacies" )
    @PreAuthorize ( "hasRole('ROLE_ADMIN') " )
    public ResponseEntity createPharmacy ( @RequestBody final PharmacyForm pharmacyF ) {
        final Pharmacy pharmacy = new Pharmacy( pharmacyF );
        if ( null != Pharmacy.getByLocation( pharmacy.getAddress(), pharmacy.getZip() ) ) {
            return new ResponseEntity( errorResponse( "A Pharmacy already exists at the given address" ),
                    HttpStatus.CONFLICT );
        }
        try {
            pharmacy.save();
            LoggerUtil.log( TransactionType.CREATE_PHARMACY, LoggerUtil.currentUser() );
            return new ResponseEntity( pharmacy, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( errorResponse( "Error occured while validating or saving " + pharmacy.toString()
                    + " because of " + e.getMessage() ), HttpStatus.BAD_REQUEST );
        }

    }

    /**
     * Updates the Pharmacy with the name provided by overwriting it with the
     * new Pharmacy provided.
     *
     * @param id
     *            Id of the Pharmacy to update
     * @param pharmacyF
     *            The new Pharmacy to save to this name
     * @return response
     */
    @PutMapping ( BASE_PATH + "/pharmacies/{id}" )
    @PreAuthorize ( "hasRole('ROLE_ADMIN') " )
    public ResponseEntity updatePharmacy ( @PathVariable ( "id" ) final Long id,
            @RequestBody final PharmacyForm pharmacyF ) {
        final Pharmacy pharmacy = new Pharmacy( pharmacyF );
        final Pharmacy dbPharmacy = Pharmacy.getById( id );
        final Pharmacy check = Pharmacy.getByLocation( pharmacy.getAddress(), pharmacy.getZip() );
        if ( null == dbPharmacy ) {
            return new ResponseEntity( errorResponse( "No pharmacy found for id: " + id ), HttpStatus.NOT_FOUND );
        }
        if ( !check.getId().equals( dbPharmacy.getId() ) ) {
            return new ResponseEntity( errorResponse( "A pharmacy already exists at the given address! " ),
                    HttpStatus.BAD_REQUEST );
        }
        try {
            pharmacy.save(); /* Will overwrite existing request */
            dbPharmacy.delete(); // deleting old pharmacy since id is
                                 // auto-generated and will be different
            LoggerUtil.log( TransactionType.EDIT_PHARMACY, LoggerUtil.currentUser() );
            return new ResponseEntity( pharmacy, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( errorResponse( "Could not update " + id + " because of " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Deletes the Pharmacy with the id matching the given id. Requires admin
     * permissions.
     *
     * @param id
     *            the id of the Pharmacy to delete
     * @return the id of the deleted Pharmacy
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @DeleteMapping ( BASE_PATH + "/pharmacies/{id}" )
    public ResponseEntity deletePharmacy ( @PathVariable ( "id" ) final Long id ) {
        try {
            final Pharmacy pharmacy = Pharmacy.getById( id );
            if ( pharmacy == null ) {
                LoggerUtil.log( TransactionType.DELETE_PHARMACY, LoggerUtil.currentUser(),
                        "Could not find pharmacy with id: " + id );
                return new ResponseEntity( errorResponse( "No pharmacy for id: " + id ), HttpStatus.NOT_FOUND );
            }
            pharmacy.delete();
            LoggerUtil.log( TransactionType.DELETE_PHARMACY, LoggerUtil.currentUser(),
                    "Deleted pharmacy with id: " + pharmacy.getName() );
            return new ResponseEntity( id, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            LoggerUtil.log( TransactionType.DELETE_PHARMACY, LoggerUtil.currentUser(), "Failed to delete pharmacy" );
            return new ResponseEntity( errorResponse( "Could not delete pharmacy: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Returns all the prescriptions sent to a given pharmacy
     *
     * @param id
     *            is the id of the pharmacy to retrieve the prescriptions for
     * @return returns a collection of Prescriptions
     */
    @PreAuthorize ( "hasRole('ROLE_PHARMACIST')" )
    @GetMapping ( BASE_PATH + "/pharmacies/{id}/prescriptions" )
    public ResponseEntity getPrescriptions ( @PathVariable final Long id ) {
        final Pharmacy pharmacy = Pharmacy.getById( id );
        if ( pharmacy == null ) {
            LoggerUtil.log( TransactionType.PHARMACIST_PRESCRIPTION_VIEW, LoggerUtil.currentUser(),
                    "Could not find pharmacy with id: " + id );
            return new ResponseEntity( errorResponse( "No pharmacy for id: " + id ), HttpStatus.NOT_FOUND );
        }
        breakRecursion( pharmacy );
        LoggerUtil.log( TransactionType.PHARMACIST_PRESCRIPTION_VIEW, LoggerUtil.currentUser(),
                "Pharmacist viewed prescriptions for pharmacy " + pharmacy.getName() );
        return new ResponseEntity( pharmacy.getPrescriptions(), HttpStatus.OK );
    }

}
