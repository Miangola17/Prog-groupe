package Classes;


/**
 * Generated from IDL interface "GestionClasses".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public interface GestionClassesOperations
{
	/* constants */
	/* operations  */
	java.lang.String[] listerClasses() throws Classes.ErreurClasse;
	java.lang.String[] listerEtudiants(java.lang.String nomClasse) throws Classes.ErreurClasse;
}
