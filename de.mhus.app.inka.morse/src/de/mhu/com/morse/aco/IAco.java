package de.mhu.com.morse.aco;

import java.util.Date;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

/**
 * <P>
 * <b>Attribute Control Object (ACO)</b> handles the values of Business Attributes (IAttribute).
 * For example boolean, int, string, acl, id etc. The basic types are created by static
 * functions. All other can be defined by m_function with the prefix "aco." in the sys
 * channel. A ACO always need this interface to interact with morse.
 * </p>
 * <p>
 * The control class is a content less class (it's not a container for a value. The RAW value is stored as string).
 * The only attribute is the Business Atrribute definition. In this definition the ACO gets 
 * informations from "type", "extra value" and "default".
 * </p>
 * 
 * @author mike
 *
 */
public interface IAco {

	/**
	 * Called at init time. If the ACO and the base type
	 * of the business attribute is not compatible it should
	 * throw a exception.
	 * 
	 * @param pAttr Attribute definition to handle.
	 */
	public void init( IAttribute pAttr ) throws MorseException;
	
	/**
	 * Validate the attribute. If the attribute is not valide (e.g. to long or NULL)
	 * it returns false.
	 * 
	 * @param value RAW Value
	 * @return true if the "value" is valide.
	 */
	public boolean validate( String value );
	
	public String getString( String value ) throws MorseException;
	public Object getObject( String value ) throws MorseException;
	public int getInteger(String value) throws MorseException;
	public long getLong(String value) throws MorseException;
	public double getDouble(String value) throws MorseException;
	public Date getDate(String value) throws MorseException;
	public boolean getBoolean(String value) throws MorseException;
	
	/**
	 * Returns the RAW representation to store or to transport the data. This representation
	 * should be a valide value, it should be short and equals the base type of the
	 * business attribute (if possible).
	 * 
	 * @param value
	 * @return
	 * @throws MorseException
	 */
	public String getRaw( String value ) throws MorseException;
	
}
