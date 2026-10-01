package Classes;


/**
 * Generated from IDL interface "GestionClasses".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public abstract class GestionClassesHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(GestionClassesHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_interface_tc("IDL:Classes/GestionClasses:1.0", "GestionClasses");
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final Classes.GestionClasses s)
	{
			any.insert_Object(s);
	}
	public static Classes.GestionClasses extract(final org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object()) ;
	}
	public static String id()
	{
		return "IDL:Classes/GestionClasses:1.0";
	}
	public static GestionClasses read(final org.omg.CORBA.portable.InputStream in)
	{
		return narrow(in.read_Object(Classes._GestionClassesStub.class));
	}
	public static void write(final org.omg.CORBA.portable.OutputStream _out, final Classes.GestionClasses s)
	{
		_out.write_Object(s);
	}
	public static Classes.GestionClasses narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof Classes.GestionClasses)
		{
			return (Classes.GestionClasses)obj;
		}
		else if (obj._is_a("IDL:Classes/GestionClasses:1.0"))
		{
			Classes._GestionClassesStub stub;
			stub = new Classes._GestionClassesStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
		else
		{
			throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
		}
	}
	public static Classes.GestionClasses unchecked_narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof Classes.GestionClasses)
		{
			return (Classes.GestionClasses)obj;
		}
		else
		{
			Classes._GestionClassesStub stub;
			stub = new Classes._GestionClassesStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
	}
}
