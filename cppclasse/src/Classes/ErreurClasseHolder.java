package Classes;

/**
 * Generated from IDL exception "ErreurClasse".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public final class ErreurClasseHolder
	implements org.omg.CORBA.portable.Streamable
{
	public Classes.ErreurClasse value;

	public ErreurClasseHolder ()
	{
	}
	public ErreurClasseHolder(final Classes.ErreurClasse initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return Classes.ErreurClasseHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = Classes.ErreurClasseHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		Classes.ErreurClasseHelper.write(_out, value);
	}
}
