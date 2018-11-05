/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;

/**
 * The ScriptingEngine is commonly used to evaluate scripts and produce dynamic content.
 * They are most notably used to compile Groovy scripts at runtime.
 */
public interface ScriptingEngine
{
	/**
	 * Called to evaluate the provided context based on registered content types and extensions
	 * Returning false} will continue to next available ScriptingProcessor.
	 *
	 * @param context The EvalContext
	 *
	 * @return Context finished execution
	 *
	 * @throws Exception Provided simply for convenience, keep in mind that if any unique exceptions need special handling when thrown.
	 */
	boolean eval( ScriptingContext context ) throws Exception;

	/**
	 * Called on each instance to register what types this engine will handle
	 *
	 * @return types Array of content types and file extensions that provided {@link ScriptingEngine} can handle, e.g., "text/css", "css", "js", or "application/javascript-x".
	 * Leaving this field empty will catch everything, please don't abuse the power.
	 */
	List<String> getTypes();

	/**
	 * Called to provide the EvalFactory bindings
	 *
	 * @param binding The EvalFactory binding
	 */
	void setBinding( ScriptBinding binding );

	/**
	 * Called to provide output stream to ScriptingEngine
	 *
	 * @param buffer  The ByteBuf output stream
	 * @param charset The current EvalFactory character set
	 */
	void setOutput( ByteBuf buffer, Charset charset );
}
