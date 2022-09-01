/*
 * Copyright 2012 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.mapbased.rpc.func.client;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.NullPointerError;
import com.perforce.p4java.exception.TrustException;
import com.perforce.p4java.impl.mapbased.rpc.RpcServer;
import com.perforce.p4java.messages.PerforceMessages;
import com.perforce.p4java.server.Fingerprint;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Handle the client trust and fingerprint for Perforce SSL connections.
 *
 * This also include methods to assist in validating a certificate path.
 * We trust all certificates but save the certificates for
 * later checking with methods in this class.
 */
public class ClientTrust {

	public static final String DIGEST_TYPE = "SHA";

	public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static final String FINGERPRINT_USER_NAME = "**++**";

	public static final String FINGERPRINT_REPLACEMENT_USER_NAME = "++++++";

	public static final String CLIENT_TRUST_MESSAGES = "com.perforce.p4java.messages.ClientTrustMessages";

	public static final String CLIENT_TRUST_WARNING_NOT_ESTABLISHED = "client.trust.warning.notestablished";
	public static final String CLIENT_TRUST_WARNING_NEW_CONNECTION = "client.trust.warning.newconnection";
	public static final String CLIENT_TRUST_WARNING_NEW_KEY = "client.trust.warning.newkey";

	public static final String CLIENT_TRUST_EXCEPTION_NEW_CONNECTION = "client.trust.exception.newconnection";
	public static final String CLIENT_TRUST_EXCEPTION_NEW_KEY = "client.trust.exception.newkey";

	public static final String CLIENT_TRUST_ADD_EXCEPTION_NEW_CONNECTION = "client.trust.add.exception.newconnection";
	public static final String CLIENT_TRUST_ADD_EXCEPTION_NEW_KEY = "client.trust.add.exception.newkey";

	public static final String CLIENT_TRUST_ADDED = "client.trust.added";
	public static final String CLIENT_TRUST_REMOVED = "client.trust.removed";
	public static final String CLIENT_TRUST_ALREADY_ESTABLISHED = "client.trust.alreadyestablished";

	public static final String CLIENT_TRUST_INSTALL_EXCEPTION = "client.trust.install.exception";
	public static final String CLIENT_TRUST_UNINSTALL_EXCEPTION = "client.trust.uninstall.exception";

	public static final String SSL_CLIENT_TRUST_BADDATE= "client.trust.cert.bad.date.exception";
	public static final String SSL_CLIENT_TRUST_BADHOST = "client.trust.cert.bad.host.exception";
	private RpcServer rpcServer = null;

	private static PerforceMessages messages = new PerforceMessages(
			ClientTrust.CLIENT_TRUST_MESSAGES);

	/**
	 * Instantiates a new client trust.
	 * 
	 * @param rpcServer
	 *            the rpc server
	 */
	public ClientTrust(RpcServer rpcServer) {
		if (rpcServer == null) {
			throw new NullPointerError(
					"null rpcServer passed to ClientTrust constructor");
		}
		this.rpcServer = rpcServer;
	}

	/**
	 * Install the fingerprint for the specified server IP and port
	 * 
	 * @param serverIpPort
	 *            the serverIpPort
	 * @param fingerprintUser
	 *            the fingerprintUser
	 * @param fingerprint
	 *            the fingerprint
	 * @throws TrustException
	 *             the trust exception
	 */
	public void installFingerprint(String serverIpPort, String fingerprintUser, String fingerprint)
			throws TrustException {
		if (serverIpPort == null) {
			throw new NullPointerError(
					"null serverIpPort passed to the ClientTrust installFingerprint method");
		}
		if (fingerprintUser == null) {
			throw new NullPointerError(
					"null fingerprintUser passed to the ClientTrust installFingerprint method");
		}
		if (fingerprint == null) {
			throw new NullPointerError(
					"null fingerprint passed to the ClientTrust installFingerprint method");
		}
		try {
			rpcServer.saveFingerprint(serverIpPort, fingerprintUser, fingerprint);
		} catch (ConfigException e) {
			throw new TrustException(TrustException.Type.INSTALL,
					rpcServer.getServerHostPort(), serverIpPort, fingerprint,
					messages.getMessage(ClientTrust.CLIENT_TRUST_INSTALL_EXCEPTION,
    						new Object[] { fingerprint, rpcServer.getServerHostPort(), serverIpPort }), e);
		}
	}

	/**
	 * Removes the fingerprint for the specified server IP and port
	 * 
	 * @param serverIpPort
	 *            the serverIpPort
	 * @param fingerprintUser
	 *            the fingerprintUser
	 * @throws TrustException
	 *             the trust exception
	 */
	public void removeFingerprint(String serverIpPort, String fingerprintUser) throws TrustException {
		if (serverIpPort == null) {
			throw new NullPointerError(
					"null serverIpPort passed to the ClientTrust removeFingerprint method");
		}
		if (fingerprintUser == null) {
			throw new NullPointerError(
					"null fingerprintUser passed to the ClientTrust removeFingerprint method");
		}
		try {
			rpcServer.saveFingerprint(serverIpPort, fingerprintUser, null);
		} catch (ConfigException e) {
			throw new TrustException(TrustException.Type.UNINSTALL,
					rpcServer.getServerHostPort(), serverIpPort, null,
					messages.getMessage(ClientTrust.CLIENT_TRUST_UNINSTALL_EXCEPTION,
    						new Object[] { rpcServer.getServerHostPort(), serverIpPort }), e);
		}
	}

	/**
	 * Check if the fingerprint exists for the specified server IP and port
	 * 
	 * @param serverKey
	 *            the serverIpPort or serverHostName
	 * @param fingerprintUser
	 *            the fingerprintUser
	 * @return true, if successful
	 */
	public boolean fingerprintExists(String serverKey, String fingerprintUser) {
		if (serverKey == null) {
			throw new NullPointerError(
					"null serverIpPort passed to the ClientTrust fingerprintExists method");
		}
		if (fingerprintUser == null) {
			throw new NullPointerError(
					"null fingerprintUser passed to the ClientTrust fingerprintExists method");
		}
		return (rpcServer.loadFingerprint(serverKey, fingerprintUser) != null);
	}

	/**
	 * Check if the fingerprint for the specified server IP and port matches the
	 * one in trust file.
	 *
	 * @param serverKey
	 *            the serverIpPort or serverHostName
	 * @param fingerprintUser
	 *            the fingerprintUser
	 * @param fingerprint
	 *            the fingerprint
	 * @return true, if successful
	 */
	public boolean fingerprintMatches(String serverKey, String fingerprintUser, String fingerprint) {
		if (serverKey == null) {
			throw new NullPointerError(
					"null serverIpPort passed to the ClientTrust fingerprintMatches method");
		}
		if (fingerprintUser == null) {
			throw new NullPointerError(
					"null fingerprintUser passed to the ClientTrust fingerprintMatches method");
		}
		if (fingerprint == null) {
			throw new NullPointerError(
					"null fingerprint passed to the ClientTrust fingerprintMatches method");
		}
		if (fingerprintExists(serverKey, fingerprintUser)) {
			Fingerprint existingFingerprint = rpcServer
					.loadFingerprint(serverKey, fingerprintUser);
			if (existingFingerprint != null
					&& existingFingerprint.getFingerprintValue() != null) {
				if (fingerprint.equalsIgnoreCase(existingFingerprint
						.getFingerprintValue())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generate fingerprint from public key using MessageDigest.
	 * 
	 * @param publicKey
	 *            the public key
	 * @return the string
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 */
	public static String generateFingerprint(PublicKey publicKey)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(DIGEST_TYPE);
		md.update(publicKey.getEncoded());
		byte[] fp = md.digest();
		return convert2Hex(fp);
	}

	/**
	 * Generate fingerprint from a certificate using MessageDigest.
	 * 
	 * @param certificate
	 *            the certificate
	 * @return the string
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws CertificateEncodingException
	 *             the certificate encoding exception
	 */
	public static String generateFingerprint(X509Certificate certificate)
			throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance(DIGEST_TYPE);
		md.update(certificate.getEncoded());
		byte[] fp = md.digest();
		return convert2Hex(fp);
	}

	/**
	 * Convert a byte array to a hexadecimal string
	 * 
	 * @param data
	 *            the data
	 * @return the string
	 */
	public static String convert2Hex(byte[] data) {
		int n = data.length;
		StringBuffer sb = new StringBuffer(n * 3 - 1);
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				sb.append(':');
			}
			sb.append(HEX_CHARS[(data[i] >> 4) & 0x0F]);
			sb.append(HEX_CHARS[data[i] & 0x0F]);
		}
		return sb.toString();
	}

	/**
	 * Gets the messages.
	 * 
	 * @return the messages
	 */
	public PerforceMessages getMessages() {
		return messages;
	}

	/**
	 * We assume a JVM will only use one trust store.<br/>
	 * System.setProperty() is often prohibited by java security policy so you can't
	 * change the trustore property mid application.
	 */
	private static Set<TrustAnchor> trustedCAs;

	/**
	 * Gets the root CAs in the trust store, either the default truststore or as
	 * specified by javax.net.ssl.trustStore/javax.net.ssl.trustStorePassword.
	 * root CAs are cached.
	 */
	public static Set<TrustAnchor> getTrustedCAs()
			throws NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException {
		return getTrustedCAs(false);
	}
	/**
	 * Gets the root CAs from the trust store, either the default truststore or as
	 * specified by javax.net.ssl.trustStore/javax.net.ssl.trustStorePassword.
	 *
	 * @param refreshCache force retrieve from truststore
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static synchronized Set<TrustAnchor> getTrustedCAs(boolean refreshCache)
			throws NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException {
		if (! refreshCache && trustedCAs != null) {
			return trustedCAs;
		}
		X509TrustManager x509tm = getDefaultX509TrustManager();
		trustedCAs = new HashSet<TrustAnchor>();
		for (X509Certificate cert : x509tm.getAcceptedIssuers()) {
			trustedCAs.add(new TrustAnchor(cert, null));
		}
		return trustedCAs;
	}
	/**
	 * Get the system default trust manager {@link X509TrustManager}
	 */
	public static X509TrustManager getDefaultX509TrustManager() throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		tmf.init((KeyStore) null);
		for (TrustManager trustMgr : tmf.getTrustManagers()) {
			if (trustMgr instanceof X509TrustManager) {
				return (X509TrustManager) trustMgr;
			}
		}
		throw new IllegalStateException("X509TrustManager is not found");
	}

	/**
	 * Check the certificate chain.
	 *
	 * @param certs    the certificates from p4d handshake.
	 * @throws CertificateException if the validation fails
	 */
	public static void validateServerChain(X509Certificate[] certs, String refName)
			throws CertificateException {

		// workaround for bug P4-22041:
		//     remove duplicates at the end of the chain.
		//     do not disturb order of certs.
		List<X509Certificate> certList = new ArrayList<>();
		for (X509Certificate cert : certs) {
			if (certList.contains(cert)) {
				continue;
			}
			certList.add(cert);
		}

		try {
			verifyCertificateSubject(certs[0], refName);
			verifyCertificateDates(certs[0]);

			CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
			CertificateFactory certFactory = CertificateFactory.getInstance("X509");
			CertPath path = certFactory.generateCertPath(certList);

			// parameters used for validating certs.
			PKIXParameters pkixParameters = new PKIXParameters(getTrustedCAs());
			pkixParameters.setRevocationEnabled(false);  // TODO:  configurable?

			PKIXCertPathValidatorResult valDetails = (PKIXCertPathValidatorResult) certPathValidator.validate(path, pkixParameters);

			// TODO logging for validation:  ssl=3 System.out.println("result=" + valDetails.toString());

		} catch (GeneralSecurityException e) {
			// example:  java.security.cert.CertPathValidatorException:  path does not chain with any of the trust anchors
			throw new CertificateException(e);
		}
	}

	/**
	 * Check the certificate Not Before and Not After dates
	 * @param cert
	 * @throws CertificateException
	 */
	public static void verifyCertificateDates(X509Certificate cert) throws CertificateException {
		Date after= cert.getNotAfter();
		Date before = cert.getNotBefore() ;
		Date now = new Date();

		if (now.before(before)) {
			throw new CertificateException(messages.getMessage(SSL_CLIENT_TRUST_BADDATE,
					new Object[] { "before", before}));
		}
		if (now.after(after)) {
			throw new CertificateException(messages.getMessage(SSL_CLIENT_TRUST_BADDATE,
					new Object[] { "after", after}));
		}
		return;
	}

	/**
	 * Verify the request's hostname to that in the certificate.
	 *
	 * @param cert certificate
	 * @param hostName Host name
	 * @throws CertificateParsingException
	 * @throws CertificateException
	 */
	public static void verifyCertificateSubject(X509Certificate cert, String hostName) throws CertificateParsingException, CertificateException {

		// check SANs first, https://www.rfc-editor.org/rfc/rfc6125#section-6.4.3
		for (List<?> entry : cert.getSubjectAlternativeNames()) {
			final int type = ((Integer) entry.get(0)).intValue();
			// DNS or IP
			if (type == 2 || type == 7) {
				if ( matchSubject((String)entry.get(1), hostName)) {
					return;
				}
			}
		}

		// check the CN.  I think RFC 6125 says we shouldn't, but p4api compares it.
		String cn = cert.getSubjectDN().getName();
		if (cn.startsWith("CN=")) {
			cn = cn.substring(3);
		}
		if ( matchSubject(cn, hostName)) {
			return;
		}

		// not expected to be here, so be nice and tell
		// what CN values the cert had for the exception msg.
		StringBuilder sb = new StringBuilder();
		for (List<?> entry : cert.getSubjectAlternativeNames()) {
			final int type = ((Integer) entry.get(0)).intValue();
			if (type == 2 || type == 7) {
				sb.append((String)entry.get(1) + ",");
			}
		}
		sb.append(cn);
		throw new CertificateException(messages.getMessage(SSL_CLIENT_TRUST_BADHOST,new Object[]{hostName, sb}));
	}

	/**
	 * Check to see if a cert's subject matches with a reference name (e.g., hostname in P4PORT)
	 * Note that the subject may contain a leading wildcard "*.".<br/>
	 *
	 * @param subject - certificate's subject name (a SAN value or CN)
	 * @param refName reference name to compare.
	 * @return true if matches.
	 */
	private static boolean matchSubject(String subject, String refName) {
		if ( subject.startsWith("*.") ) {
			subject = subject.substring(1); // remove "*"
			int firstDot = refName.indexOf("."); // remove hostname?
			firstDot = (firstDot >= 0 ) ? firstDot  : 0;
			if (subject.equalsIgnoreCase(refName.substring(firstDot))) {
				return true;
			}
		}
		return subject.equalsIgnoreCase(refName);
	}

}