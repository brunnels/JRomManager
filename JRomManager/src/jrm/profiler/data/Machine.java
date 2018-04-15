package jrm.profiler.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Machine extends Anyware implements Serializable
{
	public String romof = null;
	public String sampleof = null;
	public boolean isbios = false;
	public boolean ismechanical = false;
	public boolean isdevice = false;

	public Machine()
	{
	}

	@Override
	public boolean isClone()
	{
		return (parent != null && !getParent().isbios);
	}

	@Override
	public Machine getParent()
	{
		return getParent(Machine.class);
	}

	@Override
	public boolean isBios()
	{
		return isbios;
	}

	@Override
	public boolean isRomOf()
	{
		return romof != null;
	}
}