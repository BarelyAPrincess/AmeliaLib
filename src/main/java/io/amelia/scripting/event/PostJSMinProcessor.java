/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.event;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.amelia.events.EventHandler;

public class PostJSMinProcessor implements Listener
{
	@EventHandler
	public void onEvent( PostEvalEvent event )
	{
		if ( !event.context().contentType().equals( "application/javascript-x" ) || !event.context().filename().endsWith( "js" ) )
			return;

		// A simple way to ignore JS files that might already be minimized
		if ( event.context().filename() != null && event.context().filename().toLowerCase().endsWith( ".min.js" ) )
			return;

		String code = event.context().readString();
		List<SourceFile> externals = new ArrayList<>();
		List<SourceFile> inputs = Arrays.asList( SourceFile.fromCode( ( event.context().filename() == null || event.context().filename().isEmpty() ) ? "fakefile.js" : event.context().filename(), code ) );

		Compiler compiler = new Compiler();

		CompilerOptions options = new CompilerOptions();

		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel( options );

		compiler.compile( externals, inputs, options );

		event.context().resetAndWrite( StringUtils.trimToNull( compiler.toSource() ) );
	}

}
