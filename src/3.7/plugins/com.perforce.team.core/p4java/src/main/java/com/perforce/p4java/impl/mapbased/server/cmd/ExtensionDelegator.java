package com.perforce.p4java.impl.mapbased.server.cmd;

import com.perforce.p4java.core.IExtension;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.core.Extension;
import com.perforce.p4java.impl.generic.core.ExtensionSummary;
import com.perforce.p4java.impl.generic.core.InputMapper;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.delegator.IExtensionDelegator;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.parseCommandResultMapAsString;
import static com.perforce.p4java.server.CmdSpec.EXTENSION;

public class ExtensionDelegator extends BaseDelegator implements IExtensionDelegator {
	/**
	 * Basic constructor, taking a server object.
	 *
	 * @param server - an instance of the currently effective server implementation
	 */
	public ExtensionDelegator(IOptionsServer server) {
		super(server);
	}

	@Override
	public String sampleExtension(String extnName) throws P4JavaException {
		// p4 extension --sample extName
		String[] args = new String[]{"--sample", extnName};
		List<Map<String, Object>> result = server.execMapCmdList(EXTENSION, args, null);
		return parseCommandResultMapAsString(result);
	}

	@Override
	public String packageExtension(String dirName) throws P4JavaException {
		// p4 extension --package dir [--sign=keyDir]
		// TODO Implement --sign
		String[] args = new String[]{"--package", dirName};
		List<Map<String, Object>> resultsMap = server.execMapCmdList(EXTENSION, args, null);

		return parseCommandResultMapAsString(resultsMap);
	}

	@Override
	public ExtensionSummary installExtension(String extnPackageName, boolean allowUnsigned) throws P4JavaException {
		// p4 extension --install extName.p4-extension [--yes]
		String[] args;
		if (allowUnsigned) {
			args = new String[]{"-y", "--allow-unsigned", "--install", extnPackageName};
		} else {
			args = new String[]{"-y", "--install", extnPackageName};
		}
		List<Map<String, Object>> result = server.execMapCmdList(EXTENSION, args, null);
		return processExtensionMaps(result).get(0);
	}

	@Override
	public String createExtensionConfig(IExtension extension, String namespace, @Nullable String instanceConfig) throws P4JavaException {
		return updateExtensionConfig(extension, namespace, instanceConfig);
	}

	@Override
	public String updateExtensionConfig(IExtension extension, String namespace, @Nullable String instanceConfig) throws P4JavaException {
		/*
		p4 extension --configure extFQN [ --revision=n ] [ -o | -i ]
		p4 extension --configure extFQN [ --revision=n ] [ -o | -i ]
	 				 --name instName
		 */
		// TODO Support --revision and -o
		String[] args;
		if (StringUtils.isNotEmpty(instanceConfig)) {
			args = new String[]{"--configure", namespace + "::" + extension.getExtName(), "--name", instanceConfig, "-i"};
		} else {
			args = new String[]{"--configure", namespace + "::" + extension.getExtName(), "-i"};
		}
		List<Map<String, Object>> result = server.execMapCmdList(EXTENSION, args,
				InputMapper.map(extension));
		return ResultMapParser.parseCommandResultMapIfIsInfoMessageAsString(result);

	}

	@Override
	public List<ExtensionSummary> listExtensions(String type) throws P4JavaException {
		// p4 extension --list --type type
		String[] args = new String[]{"--list", "--type", type};
		List<Map<String, Object>> result = server.execMapCmdList(EXTENSION, args, null);
		return processExtensionMaps(result);
	}

	@Override
	public String deleteExtension(String namespace, String extnName) throws P4JavaException {
		// [ --delete extension [--run name [arguments]] [--name name] [--revision rev] [--path path] [--cert] [--yes] ]
		/*
		p4 extension --delete extFQN [ --name instName ] [ --revision=n ]
									 [ --path filespec ] [--yes]
		p4 extension --delete fingerprint --cert
		 */
		// TODO Implement --run, path, fingerprint
		String[] args = new String[]{"-y", "--delete", namespace + "::" + extnName};
		List<Map<String, Object>> result = server.execMapCmdList(EXTENSION, args, null);
		return parseCommandResultMapAsString(result);
	}

	@Override
	public Extension getExtensionConfig(String namespace, String name, String instanceName) throws P4JavaException {
		String[] args;
		if (StringUtils.isBlank(instanceName)) {
			args = new String[]{"--configure", namespace + "::" + name, "-o"};

		} else {
			args = new String[]{"--configure", namespace + "::" + name, "--name", instanceName, "-o"};
		}

		List<Map<String, Object>> result = server.execMapCmdList("extension", args, null);
		// Raise error e.g. when no extension depot is defined
		ResultMapParser.handleErrorStr(result.get(0));

		return new Extension(result.get(0));
	}

	private List<ExtensionSummary> processExtensionMaps(List<Map<String, Object>> resultMaps) throws AccessException, RequestException {
		if (resultMaps == null || resultMaps.isEmpty()) {
			return null;
		}
		// Raise error e.g. when no extension depot is defined
		ResultMapParser.handleErrorStr(resultMaps.get(0));

		List<ExtensionSummary> extensionList = new ArrayList<>();
		for (Map<String, Object> map : resultMaps) {
			ExtensionSummary extSummary = new ExtensionSummary(map);
			extensionList.add(extSummary);
		}
		return extensionList;
	}
}
