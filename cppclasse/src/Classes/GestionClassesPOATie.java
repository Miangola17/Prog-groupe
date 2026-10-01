package Classes;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "GestionClasses".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 1 oct. 2026, 05:34:51
 */

public class GestionClassesPOATie
	extends GestionClassesPOA
{
	private GestionClassesOperations _delegate;

	private POA _poa;
	public GestionClassesPOATie(GestionClassesOperations delegate)
	{
		_delegate = delegate;
	}
	public GestionClassesPOATie(GestionClassesOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
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
	public GestionClassesOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(GestionClassesOperations delegate)
	{
		_delegate = delegate;
	}
	public POA _default_POA()
	{
		if (_poa != null)
		{
			return _poa;
		}
		return super._default_POA();
	}
	public java.lang.String[] listerEtudiants(java.lang.String nomClasse) throws Classes.ErreurClasse
	{
		return _delegate.listerEtudiants(nomClasse);
	}

	public java.lang.String[] listerClasses() throws Classes.ErreurClasse
	{
		return _delegate.listerClasses();
	}

}
