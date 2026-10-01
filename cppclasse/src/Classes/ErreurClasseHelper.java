package Classes;


/**
 * Generated from IDL exception "ErreurClasse".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public abstract class ErreurClasseHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(ErreurClasseHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_exception_tc(Classes.ErreurClasseHelper.id(),"ErreurClasse",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("message", org.omg.CORBA.ORB.init().create_string_tc(0), null)});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final Classes.ErreurClasse s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static Classes.ErreurClasse extract (final org.omg.CORBA.Any any)
	{
		org.omg.CORBA.portable.InputStream in = any.create_input_stream();
		try
		{
			return read (in);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (java.io.IOException e)
			{
			throw new RuntimeException("Unexpected exception " + e.toString() );
			}
		}
	}

	public static String id()
	{
		return "IDL:Classes/ErreurClasse:1.0";
	}
	public static Classes.ErreurClasse read (final org.omg.CORBA.portable.InputStream in)
	{
		String id = in.read_string();
		if (!id.equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id: " + id);
		java.lang.String x0;
		x0=in.read_string();
		final Classes.ErreurClasse result = new Classes.ErreurClasse(id, x0);
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final Classes.ErreurClasse s)
	{
		out.write_string(id());
		java.lang.String tmpResult1 = s.message;
out.write_string( tmpResult1 );
	}
}
