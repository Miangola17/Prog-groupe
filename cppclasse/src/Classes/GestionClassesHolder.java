package Classes;

/**
 * Generated from IDL interface "GestionClasses".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public final class GestionClassesHolder	implements org.omg.CORBA.portable.Streamable{
	 public GestionClasses value;
	public GestionClassesHolder()
	{
	}
	public GestionClassesHolder (final GestionClasses initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return GestionClassesHelper.type();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = GestionClassesHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		GestionClassesHelper.write (_out,value);
	}
}
