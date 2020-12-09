/**
 *
 */
package edu.ncsu.csc.itrust2.forms.patient;

/**
 * Form used to pass Patient preference for default pharmacy and prescription
 * type
 *
 * @author Sumit Biswas
 *
 */
public class PatientPreferenceForm {

    /**
     * Field that stores brand preference True is branded / False is generic
     */
    private boolean branded;

    /** Stores default pharmacy preference for patient */
    private Long    defaultPharmacyId;

    /**
     * Empty constructor for Hibernate
     *
     */
    public PatientPreferenceForm () {
        // empty
    }

    /**
     * Constructs a PatientPreferenceForm with fields for the preferences
     *
     * @param branded
     *            is the brand preference. True is branded, False is generic
     * @param pharmacyId
     *            is the id of the pharmacy to be set as default
     */
    public PatientPreferenceForm ( final boolean branded, final Long pharmacyId ) {
        this.branded = branded;
        this.defaultPharmacyId = pharmacyId;
    }

    /**
     * Returns the brand preference
     *
     * @return Returns the brand preference
     */
    public boolean getBrandPreference () {
        return this.branded;
    }

    /**
     * Returns the pharmacy preference
     *
     * @return returns the id for the preferred pharmacy
     */
    public Long getDefaultPharmacyId () {
        return this.defaultPharmacyId;
    }
}
