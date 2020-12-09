package edu.ncsu.csc.itrust2.controllers.api;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;

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

import edu.ncsu.csc.itrust2.forms.hcp.PrescriptionForm;
import edu.ncsu.csc.itrust2.models.enums.TransactionType;
import edu.ncsu.csc.itrust2.models.persistent.Personnel;
import edu.ncsu.csc.itrust2.models.persistent.Pharmacy;
import edu.ncsu.csc.itrust2.models.persistent.Prescription;
import edu.ncsu.csc.itrust2.models.persistent.User;
import edu.ncsu.csc.itrust2.utils.EmailUtil;
import edu.ncsu.csc.itrust2.utils.LoggerUtil;

/**
 * Provides REST endpoints that deal with prescriptions. Exposes functionality
 * to add, edit, fetch, and delete prescriptions.
 *
 * @author Connor
 * @author Kai Presler-Marshall
 */
@RestController
@SuppressWarnings ( { "rawtypes", "unchecked" } )
public class APIPrescriptionController extends APIController {

    /**
     * Adds a new prescription to the system. Requires HCP permissions. Sends it
     * to a Pharmacy as well
     *
     * @param pharmacyId
     *            is the id where the prescription is to be sent
     *
     * @param form
     *            details of the new prescription
     *
     * @return the created prescription
     */
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_OD', 'ROLE_OPH', 'ROLE_VIROLOGIST')" )
    @PostMapping ( BASE_PATH + "/prescriptions/sendTo/{pharmacyId}" )
    public ResponseEntity addPrescription ( @PathVariable final Long pharmacyId,
            @RequestBody final PrescriptionForm form ) {
        try {
            final Prescription p = new Prescription( form );
            p.save();
            //// sending prescription
            final Pharmacy receiver = Pharmacy.getById( pharmacyId );
            if ( receiver == null ) {
                p.delete();
                return new ResponseEntity( errorResponse( "Receiver pharmacy not found!" ), HttpStatus.NOT_FOUND );
            }
            receiver.addPrescriptions( Collections.singleton( p ) );
            receiver.save();
            LoggerUtil.log( TransactionType.PRESCRIPTION_CREATE, LoggerUtil.currentUser(), p.getPatient().getUsername(),
                    "Created prescription with id " + p.getId() );
            LoggerUtil.log( TransactionType.PRESCRIPTION_SENT, LoggerUtil.currentUser(), p.getPatient().getUsername(),
                    "Sent prescription to " + receiver.toString() );
            //// sent prescription
            // breaking infinite recursion
            p.setPharmacy( null );
            // breaking infinite recursion
            return new ResponseEntity( p, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            LoggerUtil.log( TransactionType.PRESCRIPTION_CREATE, LoggerUtil.currentUser(),
                    "Failed to create prescription" );
            return new ResponseEntity( errorResponse( "Could not save the prescription: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Edits an existing prescription in the system. Matches prescriptions by
     * ids. Requires HCP permissions.
     *
     * @param pharmacyId
     *            is the id where the prescription is to be sent
     *
     * @param form
     *            the form containing the details of the new prescription
     * @return the edited prescription
     */
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_OD', 'ROLE_OPH', 'ROLE_VIROLOGIST')" )
    @PutMapping ( BASE_PATH + "/prescriptions/sendTo/{pharmacyId}" )
    public ResponseEntity editPrescription ( @PathVariable final Long pharmacyId,
            @RequestBody final PrescriptionForm form ) {
        try {
            final Prescription p = new Prescription( form );
            final Prescription saved = Prescription.getById( p.getId() );
            if ( saved == null ) {
                LoggerUtil.log( TransactionType.PRESCRIPTION_EDIT, LoggerUtil.currentUser(),
                        "No prescription found with id " + p.getId() );
                return new ResponseEntity( errorResponse( "No prescription found with id " + p.getId() ),
                        HttpStatus.NOT_FOUND );
            }
            final Pharmacy previous = saved.getReceiverPharmacy();
            p.save(); /* Overwrite existing */
            // Sending prescription to new pharmacy //
            final Pharmacy current = Pharmacy.getById( pharmacyId );
            if ( current == null ) {
                saved.save(); // Rolling back changes //
                return new ResponseEntity( errorResponse( "Receiver pharmacy not found!" ), HttpStatus.NOT_FOUND );
            }
            else if ( !current.getId().equals( previous.getId() ) ) {
                previous.removePrescriptions( Collections.singleton( saved ) );
                previous.save();
                current.addPrescriptions( Collections.singleton( p ) );
                current.save();
                LoggerUtil.log( TransactionType.PRESCRIPTION_SENT, LoggerUtil.currentUser(),
                        p.getPatient().getUsername(), "Sent prescription to " + current.toString() );
            }
            else {
                previous.removePrescriptions( Collections.singleton( saved ) );
                previous.addPrescriptions( Collections.singleton( p ) );
                previous.save();
            }
            // Sending prescription to new pharmacy //
            LoggerUtil.log( TransactionType.PRESCRIPTION_EDIT, LoggerUtil.currentUser(), p.getPatient().getUsername(),
                    "Edited prescription with id " + p.getId() );

            // breaking infinite recursion
            p.setPharmacy( null );
            // breaking infinite recursion
            return new ResponseEntity( p, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            LoggerUtil.log( TransactionType.PRESCRIPTION_EDIT, LoggerUtil.currentUser(),
                    "Failed to edit prescription" );
            return new ResponseEntity( errorResponse( "Failed to update prescription: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Deletes the prescription with the given id.
     *
     * @param id
     *            the id
     * @return the id of the deleted prescription
     */
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_OD', 'ROLE_OPH', 'ROLE_VIROLOGIST')" )
    @DeleteMapping ( BASE_PATH + "/prescriptions/{id}" )
    public ResponseEntity deletePrescription ( @PathVariable final Long id ) {
        final Prescription p = Prescription.getById( id );
        if ( p == null ) {
            return new ResponseEntity( errorResponse( "No prescription found with id " + id ), HttpStatus.NOT_FOUND );
        }
        try {
            final Pharmacy receiver = p.getReceiverPharmacy();
            receiver.removePrescriptions( Collections.singleton( p ) );
            receiver.save();
            p.delete();
            LoggerUtil.log( TransactionType.PRESCRIPTION_DELETE, LoggerUtil.currentUser(), p.getPatient().getUsername(),
                    "Deleted prescription with id " + p.getId() );
            return new ResponseEntity( p.getId(), HttpStatus.OK );
        }
        catch ( final Exception e ) {
            LoggerUtil.log( TransactionType.PRESCRIPTION_DELETE, LoggerUtil.currentUser(), p.getPatient().getUsername(),
                    "Failed to delete prescription" );
            return new ResponseEntity( errorResponse( "Failed to delete prescription: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * Returns a collection of all the prescriptions in the system.
     *
     * @return all saved prescriptions
     */
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_OD', 'ROLE_OPH', 'ROLE_VIROLOGIST', 'ROLE_PATIENT')" )
    @GetMapping ( BASE_PATH + "/prescriptions" )
    public List<Prescription> getPrescriptions () {
        final User self = User.getByName( LoggerUtil.currentUser() );
        List<Prescription> toReturn;
        if ( self.isDoctor() ) {
            // Return all prescriptions in system
            LoggerUtil.log( TransactionType.PRESCRIPTION_VIEW, LoggerUtil.currentUser(),
                    "HCP viewed a list of all prescriptions" );
            toReturn = Prescription.getPrescriptions();
            for ( final Prescription p : toReturn ) {
                if ( p.getReceiverPharmacy() != null ) {
                    p.getReceiverPharmacy().nullPrescriptions();
                }
            }
            return toReturn;
        }
        else {
            // Issue #106
            // Return only prescriptions assigned to the patient
            LoggerUtil.log( TransactionType.PATIENT_PRESCRIPTION_VIEW, LoggerUtil.currentUser(),
                    "Patient viewed a list of their prescriptions" );
            toReturn = Prescription.getForPatient( LoggerUtil.currentUser() );
            for ( final Prescription p : toReturn ) {
                if ( p.getReceiverPharmacy() != null ) {
                    p.getReceiverPharmacy().nullPrescriptions();
                }
            }
            return toReturn;
        }
    }

    /**
     * Returns a single prescription using the given id.
     *
     * @param id
     *            the id of the desired prescription
     * @return the requested prescription
     */
    @PreAuthorize ( "hasAnyRole('ROLE_HCP', 'ROLE_OD', 'ROLE_OPH', 'ROLE_VIROLOGIST')" )
    @GetMapping ( BASE_PATH + "/prescriptions/{id}" )
    public ResponseEntity getPrescription ( @PathVariable final Long id ) {
        final Prescription p = Prescription.getById( id );
        if ( p == null ) {
            LoggerUtil.log( TransactionType.PRESCRIPTION_VIEW, LoggerUtil.currentUser(),
                    "Failed to find prescription with id " + id );
            return new ResponseEntity( errorResponse( "No prescription found for " + id ), HttpStatus.NOT_FOUND );
        }
        else {
            LoggerUtil.log( TransactionType.PRESCRIPTION_VIEW, LoggerUtil.currentUser(), "Viewed prescription  " + id );
            if ( p.getReceiverPharmacy() != null ) {
                p.getReceiverPharmacy().nullPrescriptions();
            }
            return new ResponseEntity( p, HttpStatus.OK );
        }
    }

    /**
     * Allows a pharmacist to fill a prescription from their pharmacy If the
     * patient's email is in the system, they are notified that their
     * prescription is filled
     *
     * @param prescriptionId
     *            is the id of the prescription to be filled
     * @return returns the server response
     */
    @PreAuthorize ( "hasRole( 'ROLE_PHARMACIST')" )
    @PutMapping ( BASE_PATH + "/prescriptions/fill/{prescriptionId}" )
    public ResponseEntity fillPrescription ( @PathVariable final Long prescriptionId ) {
        final Prescription toFill = Prescription.getById( prescriptionId );
        if ( toFill == null ) {
            return new ResponseEntity( errorResponse( "Prescription specified does not exist!" ),
                    HttpStatus.NOT_FOUND );
        }
        final Personnel pharmacist = Personnel.getByName( LoggerUtil.currentUser() );
        if ( pharmacist == null ) {
            return new ResponseEntity( errorResponse( "Pharmacist not in the system!" ), HttpStatus.BAD_REQUEST );
        }
        if ( !pharmacist.isAssigned() ) {
            return new ResponseEntity(
                    errorResponse( "Pharmacist must be assigned to a Pharmacy to fill Prescriptions!" ),
                    HttpStatus.BAD_REQUEST );
        }
        final Pharmacy pharmacy = Pharmacy.getById( Long.parseLong( pharmacist.getInstitutionId() ) );
        if ( !pharmacy.checkPrescription( toFill ) ) {
            return new ResponseEntity( errorResponse( "Cannot fill prescription not received at the Pharmacy!" ),
                    HttpStatus.BAD_REQUEST );
        }
        if ( toFill.isFilled() ) {
            return new ResponseEntity( errorResponse( "Prescription is already filled!" ), HttpStatus.BAD_REQUEST );
        }
        toFill.fill( pharmacist );
        try {
            toFill.save();
            LoggerUtil.log( TransactionType.PRESCRIPTION_FILL, LoggerUtil.currentUser(),
                    toFill.getPatient().getUsername(), toFill.getDrug().getName() + " prescription filled" );
            final String email = EmailUtil.getEmailByUsername( toFill.getPatient().getUsername() );
            if ( email != null ) {
                final String body = "This is an automated message to notify you that your prescription is ready for pickup.\n"
                        + "Your prescription:" + toFill.getDrug().getName() + ": " + toFill.getDosage() + "mg"
                        + "\nPharmacy: " + pharmacy.getName() + ", " + pharmacy.getAddress() + "\nDate Filled: "
                        + toFill.dateFilled().format(
                                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM, FormatStyle.SHORT ) );
                EmailUtil.sendEmail( email, "Prescription Filled", body );
                LoggerUtil.log( TransactionType.PRESCRIPTION_FILL_EMAIL, LoggerUtil.currentUser(),
                        toFill.getPatient().getUsername(), "Patient notified about their prescription getting filled" );
            }
            return new ResponseEntity( successResponse( "Prescription filled!" ), HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( errorResponse( "Could not fill prescription: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }

    /**
     * In case a prescription cannot be filled at the moment, this endpoint
     * allows a Pharmacist to notify the patient about the issue
     *
     * @param prescriptionId
     *            is the id of the prescription that could not be filled
     * @param message
     *            is the message from the Pharmacist to be sent to the patient
     * @return returns the server response
     */
    @PreAuthorize ( "hasRole( 'ROLE_PHARMACIST')" )
    @PutMapping ( BASE_PATH + "/prescriptions/notify/{prescriptionId}" )
    public ResponseEntity cannotFillPrescription ( @PathVariable final Long prescriptionId,
            @RequestBody final String message ) {
        final Prescription toFill = Prescription.getById( prescriptionId );
        if ( toFill == null ) {
            return new ResponseEntity( errorResponse( "Prescription specified does not exist!" ),
                    HttpStatus.NOT_FOUND );
        }
        final Personnel pharmacist = Personnel.getByName( LoggerUtil.currentUser() );
        if ( pharmacist == null ) {
            return new ResponseEntity( errorResponse( "Pharmacist not in the system!" ), HttpStatus.BAD_REQUEST );
        }
        if ( !pharmacist.isAssigned() ) {
            return new ResponseEntity(
                    errorResponse( "Pharmacist must be assigned to a Pharmacy to fill Prescriptions!" ),
                    HttpStatus.BAD_REQUEST );
        }
        final Pharmacy pharmacy = Pharmacy.getById( Long.parseLong( pharmacist.getInstitutionId() ) );
        if ( !pharmacy.checkPrescription( toFill ) ) {
            return new ResponseEntity( errorResponse( "Cannot access prescription not received at the Pharmacy!" ),
                    HttpStatus.BAD_REQUEST );
        }
        if ( toFill.isFilled() ) {
            return new ResponseEntity( errorResponse( "Prescription is already filled!" ), HttpStatus.BAD_REQUEST );
        }
        final String email = EmailUtil.getEmailByUsername( toFill.getPatient().getUsername() );
        if ( email == null ) {
            return new ResponseEntity( errorResponse( "Patient has no email in the sytem!" ), HttpStatus.NOT_FOUND );
        }
        try {
            EmailUtil.sendEmail( email, "Prescription unable to be filled", message );
            LoggerUtil.log( TransactionType.PRESCRIPTION_NOT_FILL_EMAIL, LoggerUtil.currentUser(),
                    toFill.getPatient().getUsername(), "Patient notified about issue with their prescription" );
        }
        catch ( final Exception e ) {
            return new ResponseEntity( errorResponse( "Could not notify patient: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
        return new ResponseEntity( successResponse( "Patient notified about the issue!" ), HttpStatus.OK );

    }

    /**
     * Allows a pharmacist to mark a prescription as picked up
     *
     * @param prescriptionId
     *            is the id of the prescription that was picked up
     * @return return the server response
     */
    @PreAuthorize ( "hasRole( 'ROLE_PHARMACIST')" )
    @PutMapping ( BASE_PATH + "/prescriptions/pickup/{prescriptionId}" )
    public ResponseEntity markPickup ( @PathVariable final Long prescriptionId ) {
        final Prescription pickedUp = Prescription.getById( prescriptionId );
        if ( pickedUp == null ) {
            return new ResponseEntity( errorResponse( "Prescription specified does not exist!" ),
                    HttpStatus.NOT_FOUND );
        }
        final Personnel pharmacist = Personnel.getByName( LoggerUtil.currentUser() );
        if ( pharmacist == null ) {
            return new ResponseEntity( errorResponse( "Pharmacist not in the system!" ), HttpStatus.NOT_FOUND );
        }
        if ( !pickedUp.isFilled() ) {
            return new ResponseEntity( errorResponse( "Prescription is not filled yet!" ), HttpStatus.BAD_REQUEST );
        }
        pickedUp.markPickedUp();
        try {
            pickedUp.save();
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "Prescription could not be marked as picked up: " + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
        return new ResponseEntity( successResponse( "Prescription marked as picked up!" ), HttpStatus.OK );
    }
}
