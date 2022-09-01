package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.IExtension;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Extension;
import com.perforce.p4java.impl.generic.core.ExtensionSummary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface IExtensionDelegator {
	/**
	 * 	   p4 extension --sample extName
	 *     p4 extension --package dir [--sign=keyDir]
	 *     p4 extension --install extName.p4-extension [--yes]
	 *     p4 extension --install certificateFile --cert [ --comment ]
	 *     p4 extension --delete extFQN [ --name instName ] [ --revision=n ]
	 *                  [ --path filespec ] [--yes]
	 *     p4 extension --delete fingerprint --cert
	 *
	 *     p4 extension --configure extFQN [ --revision=n ] [ -o | -i ]
	 *     p4 extension --configure extFQN [ --revision=n ] [ -o | -i ]
	 *                  --name instName
	 *
	 *     p4 extension --run instName [ extArguments ]
	 *
	 *     p4 extension --list --type type
	 */


	/**
	 * Creates sample extension with the given name. As a result a folder <extnName> will be created with main.lua
	 * and manifest.json in it.
	 * command: extension [ --sample name ]
	 *
	 * @param extnName - name of the extension
	 * @throws P4JavaException
	 */
	String sampleExtension(String extnName) throws P4JavaException;

	/**
	 * Packages the extension. As a result creates a file with .p4-extension in the current folder.
	 * command: extension [ --package dir ]
	 *
	 * @param dirName - Directory name containing main.lua and manifest.json for the extension
	 * @throws P4JavaException
	 */
	String packageExtension(String dirName) throws P4JavaException;

	/**
	 * Installs extension on p4 server.
	 * command: extension [ --install file [--yes] [--cert] [--allow-unsigned] ]
	 *
	 * @param extnPackageName - Name of the extension package file. For ex. p4java-obliterate-extn.p4-extension
	 * @throws P4JavaException
	 */
	ExtensionSummary installExtension(String extnPackageName, boolean allowUnsigned) throws P4JavaException;


	/**
	 * Creates/updates an extension's global/instance config.
	 *
	 * @param extension
	 * @param namespace
	 * @param instanceConfig - If provided, creates/updates this instance config.
	 * @return
	 * @throws P4JavaException
	 */
	String createExtensionConfig(IExtension extension, String namespace, @Nullable String instanceConfig) throws P4JavaException;

	/**
	 * Updates an extension's global/instance config.
	 *
	 * @param extension
	 * @param namespace
	 * @param instanceConfig - If provided updates this instance config.
	 * @return
	 * @throws P4JavaException
	 */
	String updateExtensionConfig(IExtension extension, String namespace, @Nullable String instanceConfig) throws P4JavaException;

	/**
	 * Lists extensions or configs based on type.
	 *
	 * @param type - Can be "extensions" or "configs"
	 * @return
	 * @throws P4JavaException
	 */
	List<ExtensionSummary> listExtensions(String type) throws P4JavaException;

	/**
	 * Deletes an extension along with all of its configuration.
	 *
	 * @param namespace
	 * @param extnName
	 * @return
	 * @throws P4JavaException
	 */
	String deleteExtension(String namespace, String extnName) throws P4JavaException;

	/**
	 * Retrieves an extension/config.
	 *
	 * @param namespace
	 * @param name
	 * @param instanceName Optional, only required for instance config
	 * @return
	 * @throws P4JavaException
	 */
	Extension getExtensionConfig(String namespace, String name, @Nullable String instanceName) throws P4JavaException;
}
