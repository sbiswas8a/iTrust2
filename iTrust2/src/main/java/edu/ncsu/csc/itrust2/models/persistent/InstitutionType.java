/**
 *
 */
package edu.ncsu.csc.itrust2.models.persistent;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.criterion.Criterion;

/**
 * InstitutionType that acts as a identifier for the type of institutions in the
 * iTrust2 system.
 *
 * @author Sumit Biswas
 *
 */
@Entity
@Table ( name = "InstitutionTypes" )
public class InstitutionType extends DomainObject<InstitutionType> {

    /** Id for the institution type */
    @Id
    @GeneratedValue ( strategy = GenerationType.AUTO )
    private Long   id;
    /** String that holds what type of institution this is */
    private String name;

    /**
     * Looks for an existing InstitutionType with the given name. If one exists,
     * returns that object, else creates a new one
     *
     * @param name
     *            is the name to look for
     * @return returns an instance of InsitutionType
     */
    public static InstitutionType getInstance ( final String name ) {
        InstitutionType toReturn = getByName( name );
        if ( toReturn == null ) {
            toReturn = new InstitutionType( name );
            toReturn.save();
        }
        return toReturn;
    }

    /**
     * Empty constructor for Hibernate
     */
    public InstitutionType () {

    }

    /**
     * Sets the name from the given parameter
     *
     * @param name
     *            is the name to set to
     */
    private InstitutionType ( final String name ) {
        this.name = name;
    }

    /**
     * Returns the name for the institution type
     *
     * @return returns the name
     */
    public String getName () {
        return name;
    }

    /**
     * Returns a list of all InsitutionTypes
     *
     *
     * @return Returns a list of all InsitutionTypes
     */
    @SuppressWarnings ( "unchecked" )
    public static List<InstitutionType> getInstitutionTypes () {
        return (List<InstitutionType>) getAll( InstitutionType.class );
    }

    /**
     * Looks for an InstitutionType by the given name
     *
     * @param name
     *            is the name by which to look for
     * @return returns the InstitutionType with the given name
     */
    public static InstitutionType getByName ( final String name ) {
        try {
            return getWhere( eqList( "name", name ) ).get( 0 );
        }
        catch ( final Exception e ) {
            return null;
        }
    }

    /**
     * Retrieve all matching InstitutionType from the database that match a
     * where clause provided.
     *
     * @param where
     *            List of Criterion to and together and search for records by
     * @return The matching InstitutionType
     */
    @SuppressWarnings ( "unchecked" )
    private static List<InstitutionType> getWhere ( final List<Criterion> where ) {
        return (List<InstitutionType>) getWhere( InstitutionType.class, where );
    }

    @Override
    public Serializable getId () {
        return id;
    }
}
