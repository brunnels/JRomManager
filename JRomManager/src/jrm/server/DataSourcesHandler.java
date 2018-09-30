package jrm.server;

import java.io.BufferedInputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class DataSourcesHandler extends DefaultHandler
{

	@Override
	public String getText()
	{
		return "not implemented";
	}

	@Override
	public String getMimeType()
	{
		return "text/html";
	}

	@Override
	public IStatus getStatus()
	{
		return Status.OK;
	}

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			if (bodylenstr != null)
			{
				int bodylen = Integer.parseInt(bodylenstr);
				if (headers.get("content-type").equals("text/xml"))
				{
					switch (urlParams.get("action"))
					{
						case "profilesTree":
							return new ProfilesTreeXMLResponse(new XMLRequest(new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "profilesList":
							return new ProfilesListXMLResponse(new XMLRequest(new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
					}
				}
				else
					session.getInputStream().skip(bodylen);
			}
		}
		catch (Exception e)
		{
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}

}