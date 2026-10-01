package Classes;

/**
 * Generated from IDL alias "ListeNoms".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public final class ListeNomsHolder
	implements org.omg.CORBA.portable.Streamable
{
	public java.lang.String[] value;

	public ListeNomsHolder ()
	{
	}
	public ListeNomsHolder (final java.lang.String[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ListeNomsHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ListeNomsHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		ListeNomsHelper.write (out,value);
	}
}
