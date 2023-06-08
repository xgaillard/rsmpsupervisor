package org.signature.rsmpSupervisor.communication.tcp;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.logging.log4j.Level;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;

import com.google.inject.Inject;

/**
 * Serveur TCP
 * 
 * @author SDARIZCUREN
 *
 */
public class ServeurTcp {
	private final LoggerRsmpSupervisor _logger;

	private ServerSocket _serverSocket;

	private static final String[] protocols = new String[] { "TLSv1.3" };
	private static final String[] cipher_suites = new String[] { "TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384" };

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ServeurTcp(LoggerRsmpSupervisor pLogger) {
		_logger = pLogger;
	}

	/**
	 * Démarrage du serveur
	 * 
	 * @param pListener            le listener à prévenir lorsqu'une nouvelle
	 *                             connexion apparaît
	 * @param pPortTcp             le port TCP à écouter
	 * @param pAvecTls             true pour activer la sécurisation TLS
	 * @param pNomMagasinCles      le nom du fichier magasin de cles
	 * @param pMotPasseMagasinCles le mot de passe du fichier magasin de cles
	 * 
	 */
	public void lancementService(IConnexionClientTcp pListener, int pPortTcp, boolean pAvecTls, String pNomMagasinCles,
			String pMotPasseMagasinCles) {
		// Soit connexion TLS, soit connexion classique
		if (pAvecTls) {
			pNomMagasinCles = pNomMagasinCles == null ? "" : pNomMagasinCles;
			pMotPasseMagasinCles = pMotPasseMagasinCles == null ? "" : pMotPasseMagasinCles;
			
			lancementServiceTls(pListener, pPortTcp, pNomMagasinCles,
					pMotPasseMagasinCles);
			
			return;
		}

		// Connexion classique
		new Thread(() -> {
			try {
				_serverSocket = new ServerSocket(pPortTcp);
			} catch (IOException e) {
				_logger.getLoggerEvenements(ServeurTcp.class).log(Level.ERROR, "Erreur démarrage du serveur", e);
				return;
			}

			while (true) {
				try {
					pListener.nouvelleConnexionTcp(_serverSocket.accept());
				} catch (IOException e) {
					_logger.getLoggerEvenements(ServeurTcp.class).log(Level.ERROR, "Erreur création connexion client",
							e);
				}
			}
		}).start();

	}

	/**
	 * Arrêt du serveur
	 */
	public void arretService() {
		if (_serverSocket != null) {
			try {
				_serverSocket.close();
			} catch (IOException e) {
				_logger.getLoggerEvenements(ServeurTcp.class).log(Level.ERROR, "Erreur arret du serveur", e);
			}
		}
	}
	
	// Démarrage d'une serveur TCP avec sécurisation TLS
	private void lancementServiceTls(IConnexionClientTcp pListener, int pPortTcp, String pNomMagasinCles,
			String pMotPasseMagasinCles) {
		new Thread(() -> {
			String keyStore = System.getProperty("user.dir") + File.separator + pNomMagasinCles;

			System.setProperty("javax.net.ssl.keyStore", keyStore);
			System.setProperty("javax.net.ssl.keyStorePassword", pMotPasseMagasinCles);
			
			ServerSocketFactory sslserversocketfactory = SSLServerSocketFactory.getDefault();
			
			try {
				_serverSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(pPortTcp);
			} catch (IOException e) {
				_logger.getLoggerEvenements(ServeurTcp.class).log(Level.ERROR, "Erreur démarrage du serveur TLS",
						e);
				return;
			}

			((SSLServerSocket) _serverSocket).setEnabledProtocols(protocols);
			((SSLServerSocket) _serverSocket).setEnabledCipherSuites(cipher_suites);

			while (true) {
				try {
					pListener.nouvelleConnexionTcp(_serverSocket.accept());
				} catch (IOException e) {
					_logger.getLoggerEvenements(ServeurTcp.class).log(Level.ERROR,
							"Erreur création connexion client", e);
				}
			}
		}).start();
	}
}
