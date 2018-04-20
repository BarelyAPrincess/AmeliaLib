package io.amelia.scripting;

public interface ScriptingProcessor
{
	void postEval( ScriptingContext scriptingContext );

	void preEval( ScriptingContext scriptingContext );
}
