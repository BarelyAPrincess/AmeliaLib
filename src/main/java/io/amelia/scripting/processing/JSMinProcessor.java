/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.processing;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingProcessor;
import io.amelia.support.Strs;

public class JSMinProcessor implements ScriptingProcessor
{
	@Override
	public void postEvaluate( ScriptingContext scriptingContext )
	{
		if ( !scriptingContext.getContentType().equals( "application/javascript-x" ) || !scriptingContext.getFileName().endsWith( "js" ) )
			return;

		// A simple way to ignore JS files that might already be minimized
		if ( scriptingContext.getFileName() != null && scriptingContext.getFileName().toLowerCase().endsWith( ".min.js" ) )
			return;

		String code = scriptingContext.readString();
		List<SourceFile> externals = new ArrayList<>();
		List<SourceFile> inputs = Arrays.asList( SourceFile.fromCode( ( scriptingContext.getFileName() == null || scriptingContext.getFileName().isEmpty() ) ? "fakefile.js" : scriptingContext.getFileName(), code ) );

		Compiler compiler = new Compiler();

		CompilerOptions options = new CompilerOptions();

		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel( options );

		compiler.compile( externals, inputs, options );

		scriptingContext.resetAndWrite( Strs.trimAll( Strs.ifNullReturnEmpty( compiler.toSource() ) ) );
	}

	@Override
	public void preEvaluate( ScriptingContext scriptingContext )
	{

	}
}
