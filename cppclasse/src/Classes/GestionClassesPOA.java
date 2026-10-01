package Classes;


/**
 * Generated from IDL interface "GestionClasses".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public abstract class GestionClassesPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, Classes.GestionClassesOperations
{
	static private final java.util.HashMap<String,Integer> m_opsHash = new java.util.HashMap<String,Integer>();
	static
	{
		m_opsHash.put ( "listerEtudiants", Integer.valueOf(0));
		m_opsHash.put ( "listerClasses", Integer.valueOf(1));
	}
	private String[] ids = {"IDL:Classes/GestionClasses:1.0"};
	public Classes.GestionClasses _this()
	{
		org.omg.CORBA.Object __o = _this_object() ;
		Classes.GestionClasses __r = Classes.GestionClassesHelper.narrow(__o);
		return __r;
	}
	public Classes.GestionClasses _this(org.omg.CORBA.ORB orb)
	{
		org.omg.CORBA.Object __o = _this_object(orb) ;
		Classes.GestionClasses __r = Classes.GestionClassesHelper.narrow(__o);
		return __r;
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // listerEtudiants
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				_out = handler.createReply();
				Classes.ListeNomsHelper.write(_out,listerEtudiants(_arg0));
			}
			catch(Classes.ErreurClasse _ex0)
			{
				_out = handler.createExceptionReply();
				Classes.ErreurClasseHelper.write(_out, _ex0);
			}
				break;
			}
			case 1: // listerClasses
			{
			try
			{
				_out = handler.createReply();
				Classes.ListeNomsHelper.write(_out,listerClasses());
			}
			catch(Classes.ErreurClasse _ex0)
			{
				_out = handler.createExceptionReply();
				Classes.ErreurClasseHelper.write(_out, _ex0);
			}
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
