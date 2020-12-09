package edu.ncsu.csc.itrust2.models.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.criterion.Criterion;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ncsu.csc.itrust2.forms.admin.PharmacyForm;
import edu.ncsu.csc.itrust2.models.enums.State;

/**
 * Class representing a Pharmacy object, as stored in the DB
 *
 * @author Sumit Biswas
 *
 */
@Entity
@Table ( name = "Pharmacies" )
public class Pharmacy extends DomainObject<Pharmacy> implements Serializable {
    /**
     * Used for serializing the object.
     */

    private static final long    serialVersionUID = 1L;

    /** InstitutionType identifier for this class */
    @OneToOne
    private InstitutionType      institutionType;

    /** Id for the pharmacy */
    @Id
    @GeneratedValue ( strategy = GenerationType.AUTO )
    private Long                 id;

    /**
     * Name of the Pharmacy
     */
    @NotEmpty
    @Length ( max = 100 )
    private String               name;

    /**
     * Address of the Pharmacy
     */
    @NotEmpty
    @Length ( max = 100 )
    private String               address;

    /**
     * State of the Pharmacy
     */
    @Enumerated ( EnumType.STRING )
    private State                state;

    /**
     * ZIP code of the Pharmacy
     */
    @NotEmpty
    @Length ( min = 5, max = 10 )
    private String               zip;

    /** List of pharmacists assigned to this pharmacy */
    @OneToMany ( cascade = CascadeType.PERSIST, fetch = FetchType.EAGER )
    private final Set<Personnel> pharmacists;

    /** List of prescriptions sent to this pharmacy */
    @OneToMany ( mappedBy = "sentTo", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER )
    private Set<Prescription>    prescriptions;

    /**
     * Construct a Pharmacy object from the PharmacyForm object provided
     *
     * @param pf
     *            A PharmacyForm to convert to a Pharmacy
     */
    public Pharmacy ( final PharmacyForm pf ) {
        pharmacists = new HashSet<>();
        prescriptions = new HashSet<>();
        setInstitutionType();
        setName( pf.getName() );
        setAddress( pf.getAddress() );
        setZip( pf.getZip() );
        setState( State.parse( pf.getState() ) );
    }

    /**
     * Construct a Pharmacy object from all of its individual fields.
     *
     * @param name
     *            Name of the Pharmacy
     * @param address
     *            Address of the Pharmacy
     * @param zip
     *            ZIP of the Pharmacy
     * @param state
     *            State of the Pharmacy
     */
    public Pharmacy ( final String name, final String address, final String zip, final String state ) {
        pharmacists = new HashSet<>();
        prescriptions = new HashSet<>();
        setInstitutionType();
        setName( name );
        setAddress( address );
        setZip( zip );
        setState( State.parse( state ) );
    }

    /**
     * Sets the InstitutionType
     *
     */
    private void setInstitutionType () {
        final String className = this.getClass().getSimpleName();
        final InstitutionType check = InstitutionType.getInstance( className );
        this.institutionType = check;
    }

    /**
     * Returns the institution type
     *
     * @return returns the institution type
     */
    public InstitutionType getInstitutionType () {
        return this.institutionType;
    }

    /**
     * Retrieve a Pharmacy from the database or in-memory cache by name.
     *
     * @param id
     *            Id of the Pharmacy to retrieve
     * @return The Pharmacy found, or null if none was found.
     */
    public static Pharmacy getById ( final Long id ) {
        try {
            return getWhere( eqList( "id", id ) ).get( 0 );
        }
        catch ( final Exception e ) {
            return null;
        }
    }

    /**
     * Retrieve a Pharmacy from the database or in-memory cache by location
     * (address + zip).
     *
     * @param address
     *            is the address to look for
     * @param zip
     *            is the zip to look for
     *
     * @return The Pharmacy found, or null if none was found.
     */
    public static Pharmacy getByLocation ( final String address, final String zip ) {
        final List<Criterion> criteria = new ArrayList<Criterion>();
        criteria.add( eq( "address", address ) );
        criteria.add( eq( "zip", zip ) );
        try {
            return getWhere( criteria ).get( 0 );
        }
        catch ( final Exception e ) {
            return null;
        }
    }

    /**
     * Returns a list of all pharmacies in a given state
     *
     * @param state
     *            is the state to fetch the pharmacies from
     * @return returns a list of pharmacies
     */
    public static List<Pharmacy> getAllinState ( final String state ) {
        final State given = State.parse( state );
        final List<Criterion> criteria = new ArrayList<Criterion>();
        criteria.add( eq( "state", given ) );
        try {
            return getWhere( criteria );
        }
        catch ( final Exception e ) {
            return null;
        }
    }

    /**
     * Retrieve all matching Pharmacies from the database that match a where
     * clause provided.
     *
     * @param where
     *            List of Criterion to and together and search for records by
     * @return The matching Pharmacies
     */
    @SuppressWarnings ( "unchecked" )
    private static List<Pharmacy> getWhere ( final List<Criterion> where ) {
        return (List<Pharmacy>) getWhere( Pharmacy.class, where );
    }

    /**
     * Retrieve all Pharmacies from the database
     *
     * @return Pharmacies found
     */
    @SuppressWarnings ( "unchecked" )
    public static List<Pharmacy> getPharmacies () {
        return (List<Pharmacy>) getAll( Pharmacy.class );
    }

    /**
     * Construct an empty Pharmacy record. Used for Hibernate.
     */
    public Pharmacy () {
        pharmacists = new HashSet<>();
        prescriptions = new HashSet<>();
    }

    /**
     * Retrieves the name of this Pharmacy
     *
     * @return The Name of the Pharmacy
     */
    public String getName () {
        return name;
    }

    /**
     * Sets the name of this Pharmacy
     *
     * @param name
     *            New Name for the Pharmacy
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * Gets the Address of this Pharmacy
     *
     * @return Address of the Pharmacy
     */
    public String getAddress () {
        return address;
    }

    /**
     * Sets the Address of this Pharmacy
     *
     * @param address
     *            New Address of the Pharmacy
     */
    public void setAddress ( final String address ) {
        this.address = address;
    }

    /**
     * Gets the State of this Pharmacy
     *
     * @return The State of the Pharmacy
     */
    public State getState () {
        return state;
    }

    /**
     * Sets the State of this Pharmacy
     *
     * @param state
     *            New State of the Pharmacy
     */
    public void setState ( final State state ) {
        this.state = state;
    }

    /**
     * Gets the ZIP code of this Pharmacy
     *
     * @return The ZIP of the Pharmacy
     */
    public String getZip () {
        return zip;
    }

    /**
     * Sets the ZIP of this Pharmacy
     *
     * @param zip
     *            New ZIP code for the Pharmacy
     */
    public void setZip ( final String zip ) {
        this.zip = zip;
    }

    /**
     * Adds a pharmacist to this Pharmacy
     *
     * @param pharmacist
     *            is the pharmacist to add
     * @return returns if the operation was successful
     */
    public boolean addPharmacist ( final Personnel pharmacist ) {
        if ( pharmacists.add( pharmacist ) ) {
            pharmacist.assign( institutionType, id.toString(), name );
            return true;
        }
        return false;
    }

    /**
     * Removes a pharmacist from this pharmacy
     *
     * @param username
     *            is the username of the pharmacist to remove
     * @return returns if the operation was successful
     */
    public boolean removePharmacist ( final String username ) {
        for ( final Personnel p : pharmacists ) {
            if ( p.getSelf().getUsername().equals( username ) ) {
                return pharmacists.remove( p );
            }
        }
        return false;
    }

    /**
     * Returns the prescriptions sent to this pharmacy
     *
     * @return Returns the prescriptions sent to this pharmacy
     */
    public Set<Prescription> getPrescriptions () {
        return this.prescriptions;
    }

    /**
     * Nulls the prescriptions set for Pharmacy Used to prevent infinite
     * recursion between Pharmacy and Prescriptions when serializing
     */
    public void nullPrescriptions () {
        this.prescriptions = null;
    }

    /**
     * Adds all prescriptions from a given list to the list of prescriptions in
     * this pharmacy
     *
     * @param list
     *            is the list of prescriptions to be added
     */
    public void addPrescriptions ( final Set<Prescription> list ) {
        for ( final Prescription p : list ) {
            p.markSent( this );
        }
        prescriptions.addAll( list );
    }

    /**
     * Remove all unfilled prescriptions from a given list from the list of
     * prescriptions in this pharmacy
     *
     * @param list
     *            is the list of prescriptions to be removed
     */
    public void removePrescriptions ( final Set<Prescription> list ) {
        for ( final Prescription p : list ) {
            if ( !p.isPickedUp() ) {
                p.markUnsent();
                prescriptions.remove( p );
            }
        }
    }

    /**
     * Checks if the given prescription is sent to this Pharmacy or not
     *
     * @param prescription
     *            is the prescription to check
     * @return returns true if prescription has been sent here, false otherwise
     */
    public boolean checkPrescription ( final Prescription prescription ) {
        for ( final Prescription p : prescriptions ) {
            if ( p.getId().equals( prescription.getId() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the ID of this Pharmacy
     */
    @Override
    public Long getId () {
        return id;
    }

    @Override
    public String toString () {
        final String s = this.name + ", " + this.state + " - " + this.zip;
        return s;
    }

    /**
     * Overridden save
     *
     */
    @Override
    public void save () {
        for ( final Personnel ph : pharmacists ) {
            ph.save();
        }
        for ( final Prescription p : prescriptions ) {
            p.save();
        }
        super.save();
    }

    /**
     * Unassigns all pharmacists, marks prescriptions not picked up as not sent,
     * and deletes itself
     *
     */
    @Override
    public void delete () {
        for ( final Personnel ph : pharmacists ) {
            ph.unassign();
            ph.save();
        }
        for ( final Prescription p : prescriptions ) {
            if ( !p.isPickedUp() ) {
                p.markUnsent();
                p.save();
            }
        }
        super.delete();
    }

}
