/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.processing;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.amelia.foundation.events.EventHandler;
import io.amelia.scripting.event.PostEvalEvent;

public class PostJSMinProcessor
{
	@EventHandler
	public void onEvent( PostEvalEvent event )
	{
		if ( !event.getScriptingContext().getContentType().equals( "application/javascript-x" ) || !event.getScriptingContext().getFileName().endsWith( "js" ) )
			return;

		// A simple way to ignore JS files that might already be minimized
		if ( event.getScriptingContext().getFileName() != null && event.getScriptingContext().getFileName().toLowerCase().endsWith( ".min.js" ) )
			return;

		String code = event.getScriptingContext().readString();
		List<SourceFile> externals = new ArrayList<>();
		List<SourceFile> inputs = Arrays.asList( SourceFile.fromCode( ( event.getScriptingContext().getFileName() == null || event.getScriptingContext().getFileName().isEmpty() ) ? "fakefile.js" : event.getScriptingContext().getFileName(), code ) );

		Compiler compiler = new Compiler();

		CompilerOptions options = new CompilerOptions();

		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel( options );

		compiler.compile( externals, inputs, options );

		event.getScriptingContext().resetAndWrite( StringUtils.trimToNull( compiler.toSource() ) );
	}

}
