package io.amelia.foundation.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.PluginException;
import io.amelia.plugins.PluginMeta;
import io.amelia.plugins.loader.PluginLoader;

public interface PluginImpl extends RegistrarBase
{
	boolean equals( Object obj );

	/**
	 * Gets a {@link ConfigData} for this plugin, read through "config.yml"
	 * <p>
	 * If there is a default config.yml embedded in this plugin, it will be provided as a default for this Configuration.
	 *
	 * @return Plugin configuration
	 */
	Parcel getConfig();

	Path getConfigPath();

	/**
	 * Returns the folder that the plugin data's files are located in. The folder may not yet exist.
	 *
	 * @return The folder
	 */
	Path getDataPath();

	/**
	 * Returns the plugin.yaml file containing the details for this plugin
	 *
	 * @return Contents of the plugin.yaml file
	 */
	PluginMeta getMeta();

	@Nonnull
	String getName();

	/**
	 * Gets the associated PluginLoader responsible for this plugin
	 *
	 * @return PluginLoader that controls this plugin
	 */
	PluginLoader getPluginLoader();

	/**
	 * Gets an embedded resource in this plugin
	 *
	 * @param filename Filename of the resource
	 *
	 * @return File if found, otherwise null
	 */
	InputStream getResource( String filename );

	@Override
	int hashCode();

	void init( PluginLoader loader, PluginMeta meta, Path dataPath, Path pluginPath, ClassLoader classLoader );

	/**
	 * Returns a value indicating whether or not this plugin is currently enabled
	 *
	 * @return true if this plugin is enabled, otherwise false
	 */
	boolean isEnabled();

	/**
	 * Simple boolean if we can still nag to the logs about things
	 *
	 * @return boolean whether we can nag
	 */
	boolean isNaggable();

	/**
	 * Called when this plugin is disabled
	 */
	void onDisable() throws PluginException.Error;

	/**
	 * Called when this plugin is enabled
	 */
	void onEnable() throws PluginException.Error;

	/**
	 * Called after a plugin is loaded but before it has been enabled. When multiple plugins are loaded, the onLoad() for all plugins is called before any onEnable() is called.
	 */
	void onLoad() throws PluginException.Error;

	/**
	 * Discards any data in {@link #getConfig()} and reloads from disk.
	 */
	void reloadConfig() throws IOException, ParcelableException.Error;

	/**
	 * Saves the {@link ConfigData} retrievable by {@link #getConfig()}.
	 */
	void saveConfig();

	/**
	 * Saves the raw contents of the default config.yml file to the location retrievable by {@link #getConfig()}. If there is no default config.yml embedded in the plugin, an empty config.yml file is saved. This should fail silently if the config.yml
	 * already exists.
	 */
	void saveDefaultConfig();

	/**
	 * Saves the raw contents of any resource embedded with a plugin's .jar file assuming it can be found using {@link #getResource(String)}. The resource is saved into the plugin's data folder using the same hierarchy as the .jar file (subdirectories are
	 * preserved).
	 *
	 * @param resourcePath the embedded resource path to look for within the plugin's .jar file. (No preceding slash).
	 * @param replace      if true, the embedded resource will overwrite the contents of an existing file.
	 *
	 * @throws IllegalArgumentException if the resource path is null, empty, or points to a nonexistent resource.
	 */
	void saveResource( String resourcePath, boolean replace );

	/**
	 * Set naggable state
	 *
	 * @param canNag is this plugin still naggable?
	 */
	void setNaggable( boolean canNag );
}
