package org.signature.rsmpSupervisor.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Level;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;

import com.google.inject.Inject;

/**
 * Implémentation d'un client serveur UDP
 * 
 * @author SDARIZCUREN
 *
 */
public class ClientServeurUdp {
	private final LoggerRsmpSupervisor _logger;

	private DatagramSocket _datagramSocket;

	private InetAddress _adresseIpSystemeExterne;
	private int _portUdpSystemeExterne = 0;
	private int _portUdpLocal = 0;

	private byte[] _bufferReception = new byte[TAILLE_MAX];

	private final static int TAILLE_MAX = 256;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ClientServeurUdp(LoggerRsmpSupervisor pLogger) {
		_logger = pLogger;
	}

	/**
	 * Démarrage du serveur
	 * 
	 * @param pListener le listener à prévenir avec les données reçues
	 * @param pPortUdp  le port UDP à écouter
	 * 
	 */
	public void lancementService(IConnexionClientUdp pListener, int pPortUdp) {
		_portUdpLocal = pPortUdp;

		try {
			_datagramSocket = new DatagramSocket(_portUdpLocal);
		} catch (SocketException e) {
			_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR, "Erreur création serveur UDP", e);

			return;
		}

		// Lecture des données dans une boucle infinie
		lectureDonnees(pListener);
	}

	/**
	 * Arrêt du serveur
	 */
	public void arretService() {
		if (_datagramSocket != null) {
			_datagramSocket.close();
		}
	}

	/**
	 * Emission d'une trame à destination d'un serveur UDP
	 * 
	 * @param pTrame la trame à émettre
	 */
	public void emissionTrame(String pTrame) {
		// Rien à faire si le serveur distant n'est pas défini
		if (_adresseIpSystemeExterne == null || _portUdpSystemeExterne <= 0 || _datagramSocket == null
				|| _datagramSocket.isClosed()) {
			return;
		}

//		DatagramSocket socket;
//		try {
//			socket = new DatagramSocket(_portUdpLocal);
//		} catch (SocketException e) {
//			_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR, "Erreur creation socket client UDP",
//					e);
//			return;
//		}

		byte[] buf = pTrame.getBytes(StandardCharsets.ISO_8859_1);

		DatagramPacket packet = new DatagramPacket(buf, buf.length, _adresseIpSystemeExterne, _portUdpSystemeExterne);
		try {
			_datagramSocket.send(packet);
		} catch (IOException e) {
			_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR,
					"Erreur emission données vers le serveur UDP", e);
		}

		// Je met un temps d'attente pour laisser le destinataire récupérer la trame et
		// la traiter
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		try {
//			socket.disconnect();
//		} catch (Exception e) {
//			_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR,
//					"Erreur disconnect de la connexion client UDP", e);
//		}
//
//		try {
//			socket.close();
//		} catch (Exception e) {
//			_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR,
//					"Erreur close de la connexion client UDP", e);
//		}
	}

	// Retournes les données reçues sur le port UDP
	private void lectureDonnees(IConnexionClientUdp pListener) {
		new Thread(() -> {
			while (_datagramSocket != null && !_datagramSocket.isClosed()) {
				DatagramPacket packet = new DatagramPacket(_bufferReception, _bufferReception.length);

				try {
					// Méthode bloquante
					_datagramSocket.receive(packet);

					// Sauvegarde de l'adresse IP et port UDP du système externe pour pouvoir lui
					// retourner la réponse
					_adresseIpSystemeExterne = packet.getAddress();
					_portUdpSystemeExterne = packet.getPort();

					byte[] recus = packet.getData();

					// Je reconstruit un buffer avec la taille reçue
					byte[] retour = new byte[packet.getLength()];
					for (int i = 0; i < retour.length; i++) {
						retour[i] = recus[i];
					}

					pListener.receptionDatas(new String(retour));
				} catch (IOException e) {
					_logger.getLoggerEvenements(ClientServeurUdp.class).log(Level.ERROR, "Erreur réception données UDP",
							e);
				}
			}
		}).start();
	}
}
