package io.amelia.sessions;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.http.session.Session;
import io.amelia.http.session.SessionRegistry;
import io.amelia.http.webroot.Webroot;
import io.amelia.http.webroot.WebrootRegistry;
import io.amelia.lang.SessionException;
import io.amelia.permission.PermissibleEntity;
import io.amelia.scripting.BindingProvider;
import io.amelia.scripting.ScriptBinding;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.Lists;
import io.amelia.support.Strs;
import io.amelia.users.UserAttachment;
import io.amelia.users.UserContext;
import io.amelia.users.UserEntity;
import io.amelia.users.UserPermissible;

/**
 * Represents a user session over ANY connection type, e.g., HTTP, HTTPS, SSH, Telnet, Console, Etc.
 */
public abstract class SessionProvider implements BindingProvider, UserAttachment
{
	private ScriptBinding binding = new ScriptBinding();
	private ScriptingFactory factory;
	private Session session;

	/**
	 * Used to nullify this SessionProvider and prepare it for collection by the GC.
	 * XXX This only cleans up this SessionProvider, finishing a {@link Session} is a different methodology.
	 */
	public void finish()
	{
		if ( session != null )
		{
			Map<String, Object> bindings = session.getGlobals();
			Map<String, Object> variables = binding.getVariables();
			List<String> disallow = Lists.newArrayList( "out", "request", "response", "context" );

			/*
			 * We transfer any global variables back into our parent session like so.
			 * We also check to make sure keys like [out, _request, _response, _FILES, _REQUEST, etc...] are excluded.
			 */
			if ( bindings != null && variables != null )
				for ( Map.Entry<String, Object> e : variables.entrySet() )
					if ( !disallow.contains( e.getKey() ) && !( e.getKey().startsWith( "_" ) && Strs.isUppercase( e.getKey() ) ) )
						bindings.put( e.getKey(), e.getValue() );

			// SessionProvider is weak referenced but by explicitly removing it we potentially speed up the GC to happen sooner.
			session.removeWrapper( this );
		}

		// Clearing references to these classes, again for easier GC cleanup.
		session = null;
		factory = null;
		binding = null;

		// Active connections should be closed here.
		finishFinal();
	}

	protected abstract void finishFinal();

	@Override
	public ScriptBinding getBinding()
	{
		return binding;
	}

	public abstract HttpCookie getCookie( String key );

	public abstract Set<HttpCookie> getCookies();

	@Override
	public String getDisplayName()
	{
		return getSession().getDisplayName();
	}

	@Override
	public PermissibleEntity getPermissibleEntity()
	{
		return getSession().getPermissibleEntity();
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return factory;
	}

	public Object getGlobal( String key )
	{
		return binding.getVariable( key );
	}

	@Override
	public UUID uuid()
	{
		return getSession().uuid();
	}

	@Override
	public abstract Webroot getWebroot();

	@Override
	public final UserPermissible getPermissible()
	{
		return session;
	}

	public final HttpCookie getServerCookie( String key, String altDefault )
	{
		HttpCookie cookie = getServerCookie( key );
		return cookie == null ? getServerCookie( altDefault ) : cookie;
	}

	protected abstract HttpCookie getServerCookie( String key );

	/**
	 * Gets the Session
	 *
	 * @return The session
	 */
	public final Session getSession()
	{
		if ( session == null )
			throw new IllegalStateException( "Detected an attempt to get session before startSession() was called" );
		return session;
	}

	@Override
	public String getVariable( String key )
	{
		return getSession().getVariable( key );
	}

	@Override
	public String getVariable( String key, String def )
	{
		return getSession().getVariable( key, def );
	}

	public final boolean hasSession()
	{
		return session != null;
	}

	@Override
	public UserEntity getEntity()
	{
		return session.getEntity();
	}

	@Override
	public boolean isInitialized()
	{
		return session.isInitialized();
	}

	@Override
	public UserContext getContext()
	{
		return session.getContext();
	}

	protected abstract void sessionStarted();

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	@Override
	public void setVariable( String key, String value )
	{
		getSession().setVariable( key, value );
	}

	/**
	 * Starts the session
	 *
	 * @throws SessionException.Error
	 */
	public Session startSession() throws SessionException.Error
	{
		session = Sessions.startSession( this );

		// Create our Binding
		binding = new ScriptBinding( new HashMap<String, Object>( session.getGlobals() ) );

		// Create our EvalFactory
		factory = ScriptingFactory.create( this );

		// Reference Session Variables
		binding.setVariable( "_SESSION", session.data.data );

		Webroot webroot = getWebroot();

		if ( webroot == null )
			webroot = WebrootRegistry.getDefaultWebroot();

		session.setWebroot( webroot );

		for ( HttpCookie cookie : getCookies() )
			session.putSessionCookie( cookie.getKey(), cookie );

		// Reference Context
		binding.setVariable( "context", this );

		// Reset __FILE__ Variable
		binding.setVariable( "__FILE__", webroot.getPublicDirectory() );

		if ( ConfigRegistry.config.getBoolean( SessionRegistry.ConfigKeys.SESSIONS_REARM_TIMEOUT ) )
			session.rearmTimeout();

		sessionStarted();

		return session;
	}

	// TODO: Future add of setDomain, setCookieName, setSecure (http verses https)
}
