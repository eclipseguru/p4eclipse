package com.perforce.team.ui.operations;

import java.util.ArrayList;
import java.util.List;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4ClientOperation;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * This is a temporary place holding all retryable methods.
 * 
 * <p>
 * In future, these methods could be re-factored into subclasses using command pattern.
 * 
 * @author ali
 *
 */
public class RetryableOperation {

	private static void runOperation(IP4Connection connection,
            IP4ClientOperation operation) {
        if (connection != null && operation != null) {
            boolean retry = true;
            IClient client = connection.getClient();
            while (retry && client != null) {
                retry = false;
                try {
                    operation.run(client);
                } catch (P4JavaException e) {
                    if (e instanceof AccessException) {
                        retry = P4ConnectionManager.retryAfterLogin(connection, (AccessException) e);
                    }
                }
            }
        }
    }

    public static List<IFileSpec> whereWithRetry(IP4Connection connection,
            final List<IFileSpec> specList) throws Throwable {
        final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        IP4ClientOperation operation = new P4ClientOperation() {
            public void run(IClient client) throws P4JavaException, P4JavaError {
                specs.addAll(client.where(specList));
            }
        };
        runOperation(connection, operation);
        return specs;
    }

    public static List<IFileSpec> haveWithRetry(IP4Connection connection,
            final List<IFileSpec> specList) throws Throwable {
        final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        IP4ClientOperation operation = new P4ClientOperation() {
            public void run(IClient client) throws P4JavaException, P4JavaError {
                specs.addAll(client.haveList(specList));
            }
        };
        runOperation(connection, operation);
        return specs;
    }

	public static List<IFileSpec> fstatWithRetry(IP4Connection connection,
			final List<IFileSpec> specList) {
        final List<IFileSpec> specs = new ArrayList<IFileSpec>();
        IP4ClientOperation operation = new P4ClientOperation() {
            public void run(IClient client) throws P4JavaException, P4JavaError {
                specs.addAll(client.getServer().getExtendedFiles(
                        specList, 0, -1, -1, null,null));
            }
        };
        runOperation(connection, operation);
        return specs;
	}

}
