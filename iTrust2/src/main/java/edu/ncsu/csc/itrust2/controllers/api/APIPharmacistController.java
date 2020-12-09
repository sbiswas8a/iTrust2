package edu.ncsu.csc.itrust2.controllers.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.itrust2.models.enums.Role;
import edu.ncsu.csc.itrust2.models.enums.TransactionType;
import edu.ncsu.csc.itrust2.models.persistent.Personnel;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.utils.LoggerUtil;

/**
 * API Controller for Pharmacists. Meant to be used by the administrator
 *
 * @author Sumit Biswas
 *
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIPharmacistController extends APIController {

    /**
     * Retrieves a list of all Pharmacists in the database
     *
     * @return list of Pharmacies
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @GetMapping ( BASE_PATH + "/pharmacists" )
    public List<Personnel> getPharmacists () {
        return Personnel.getByRole( Role.valueOf( "ROLE_PHARMACIST" ) );
    }

    /**
     * Assigns a specified pharmacist to a specified pharmacy
     *
     * @param pharmacyId
     *            is the id of the pharmacy to assign to
     * @param pharmacistId
     *            is the id of the pharmacist to assign
     * @return returns a response to indicate success or failure
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @PutMapping ( BASE_PATH + "/pharmacies/{pharmacyId}/{pharmacistId}" )
    public ResponseEntity assignPharmacist ( @PathVariable ( "pharmacyId" ) final Long pharmacyId,
            @PathVariable ( "pharmacistId" ) final String pharmacistId ) {
        final Pharmacy pharmacy = Pharmacy.getById( pharmacyId );
        final Personnel pharmacist = Personnel.getByName( pharmacistId );
        if ( pharmacy == null ) {
            return new ResponseEntity( errorResponse( "No pharmacy found for id " + pharmacyId ),
                    HttpStatus.NOT_FOUND );
        }
        if ( pharmacist == null ) {
            return new ResponseEntity( errorResponse( "No pharmacist found for id " + pharmacistId ),
                    HttpStatus.NOT_FOUND );
        }
        if ( pharmacist.isAssigned() ) {
            return new ResponseEntity( errorResponse( "Pharmacist is already assigned to a pharmacy!" ),
                    HttpStatus.BAD_REQUEST );
        }
        pharmacy.addPharmacist( pharmacist );
        pharmacy.save();
        pharmacist.save();
        LoggerUtil.log( TransactionType.PHARMACIST_ASSIGN, LoggerUtil.currentUser(), pharmacistId,
                "Admin assigned " + pharmacistId + " to " + pharmacy.getName() );
        return new ResponseEntity( successResponse( "Pharmacist successfully assigned" ), HttpStatus.OK );
    }

    /**
     * Unassigns a pharmacist from their place of work
     *
     * @param id
     *            is the id (username) of the pharmacist to unassign
     * @return returns the server response
     */
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    @PutMapping ( BASE_PATH + "/pharmacists/{id}/unassign" )
    public ResponseEntity unassignPharmacist ( @PathVariable ( "id" ) final String id ) {
        final Personnel pharmacist = Personnel.getByName( id );
        if ( pharmacist == null ) {
            return new ResponseEntity( errorResponse( "No pharmacist found for id " + id ), HttpStatus.NOT_FOUND );
        }
        if ( !pharmacist.isAssigned() ) {
            return new ResponseEntity( errorResponse( "Pharmacist is not assigned to any pharmacy!" ),
                    HttpStatus.NOT_FOUND );
        }
        final Long pharmacyId = Long.parseLong( pharmacist.getInstitutionId() );
        final Pharmacy pharmacy = Pharmacy.getById( pharmacyId );
        if ( pharmacy == null ) {
            return new ResponseEntity(
                    errorResponse( "Pharmacist assigned to non-existent pharmacy with id: " + pharmacyId ),
                    HttpStatus.NOT_FOUND );
        }
        pharmacy.removePharmacist( pharmacist.getSelf().getUsername() );
        pharmacist.unassign();
        pharmacy.save();
        pharmacist.save();
        LoggerUtil.log( TransactionType.PHARMACIST_UNASSIGN, LoggerUtil.currentUser(), id,
                "Admin unassigned " + id + " from " + pharmacy.getName() );
        return new ResponseEntity( successResponse( "Pharmacist unassigned" ), HttpStatus.OK );
    }

}
