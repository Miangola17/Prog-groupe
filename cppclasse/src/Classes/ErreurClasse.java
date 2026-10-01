package Classes;

/**
 * Generated from IDL exception "ErreurClasse".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public final class ErreurClasse
	extends org.omg.CORBA.UserException
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	public ErreurClasse()
	{
		super(Classes.ErreurClasseHelper.id());
	}

	public java.lang.String message = "";
	public ErreurClasse(java.lang.String _reason,java.lang.String message)
	{
		super(_reason);
		this.message = message;
	}
	public ErreurClasse(java.lang.String message)
	{
		super(Classes.ErreurClasseHelper.id());
		this.message = message;
	}
}
