package org.signature.rsmpSupervisor.communication.rsmp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.signature.rsmpSupervisor.communication.rsmp.MessageAvecOrigine.Origine;
import org.signature.rsmpSupervisor.communication.rsmp.json.IdentifiantMessage;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageAcknowledgement;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageNotAcknowledgement;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageRsmp;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageTunnelRequest;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageTunnelResponse;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageVersion;
import org.signature.rsmpSupervisor.communication.rsmp.json.MessageWatchdog;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;
import org.signature.rsmpSupervisor.resources.ConfigurationRsmpSupervisor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Traitement du protocole RSMP
 * 
 * @author SDARIZCUREN
 *
 */
public class TraitementProtocoleRsmp {
	private final ConfigurationRsmpSupervisor _configuration;
	private final LoggerRsmpSupervisor _logger;

	private PublishSubject<MessageAvecOrigine> _subject;

	private boolean _attenteAcquittementMsgVersion = true;
	private List<HorodateMessageRsmp> _messagesEnvoyes;

	private boolean _attenteMsgVersionClient = true;
	private boolean _arretCommunication = false;

	private String _identifiantClientRsmp = "";

	private final int FREQUENCE_EMISSION_WATCHDOG;
	private final int ANCIENNETE_MAX_MESSAGES;

	private final static int FREQUENCE_CONTROLE_RECEPTION_ACK = 1000;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private TraitementProtocoleRsmp(ConfigurationRsmpSupervisor pConfiguration, LoggerRsmpSupervisor pLogger) {
		_configuration = pConfiguration;
		_logger = pLogger;

		_messagesEnvoyes = new CopyOnWriteArrayList<>();

		FREQUENCE_EMISSION_WATCHDOG = Integer.valueOf(_configuration.getString("frequenceMessageWatchdog"));
		ANCIENNETE_MAX_MESSAGES = Integer.valueOf(_configuration.getString("attenteMaxMessageAcquittement"));
	}

	/**
	 * Donne l'identifiant du client connecté
	 * 
	 * @return
	 */
	protected String getIdRsmpClient() {
		return _identifiantClientRsmp;
	}

	/**
	 * Initialise l'observer en attente des messages à emettre ou des problèmes dans
	 * la messagerie RSMP
	 * 
	 * @param pObserver l'observateur à l'écoute des évenements RSMP
	 */
	protected void setObserver(Observer<MessageAvecOrigine> pObserver) {
		// Enregistrement de l'observateur
		_subject = PublishSubject.create();
		_subject.subscribe(pObserver);

		// Lancement d'un service de surveillance des messages en attente
		// d'acquittement pour arrêter les services si un acquittement n'arrive pas dans
		// les temps
		demarreSurveillanceMessagesEnAttente();
	}

	/**
	 * Prévient de la réception d'un nouveau message sur le canal de communication
	 * 
	 * @param pMsg le message RSMP à traiter
	 */
	protected synchronized void receptionNouveauMessage(String pMsg) {
		if (pMsg == null || pMsg.length() == 0 || _subject == null) {
			return;
		}

		// Log de la trame reçue
		_logger.getLoggerJsonRecus().log(Level.DEBUG, pMsg);

		// Test reception message Version
		Gson gson = new Gson();
		MessageRsmp typeMessageRsmp;

		try {
			typeMessageRsmp = gson.fromJson(pMsg, MessageRsmp.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message Type RSMP", e);
			return;
		}

		if (typeMessageRsmp == null || typeMessageRsmp.type == null) {
			return;
		}

		// Cas reception message Version du client
		if (_attenteMsgVersionClient) {
			if (MessageVersion.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
				traitementReceptionMessageVersionClient(pMsg);
			}

			return;
		}

		// Cas attente acquittement du message version
		if (_attenteAcquittementMsgVersion) {
			if (MessageAcknowledgement.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
				traitementAcquittementMessageVersion(pMsg);
			} else if (MessageNotAcknowledgement.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
				traitementNonAcquittementMessageVersion(pMsg);
			}

			return;
		}

		// Cas reception d'un message watchdog
		if (MessageWatchdog.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
			traitementReceptionMessageWatchdog(pMsg);

			return;
		}

		// Cas reception d'un acquittement ou non acquittement
		if (MessageAcknowledgement.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
			traitementAcquittementMessage(pMsg);

			return;
		} else if (MessageNotAcknowledgement.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
			traitementNonAcquittementMessage(pMsg);

			return;
		}

		// AUTRES TYPES DE MESSAGES A FAIRE

		// Cas reception d'un message transporté par un tunnel RSMP
		if (MessageTunnelResponse.TYPE_MESSAGE.equalsIgnoreCase(typeMessageRsmp.type)) {
			traitementReceptionMessageTunnelResponse(pMsg);

			return;
		}
	}

	// Traitement reception de l'acquittement d'un message version
	private void traitementAcquittementMessageVersion(String pMsg) {
		// Si mon message version est acquitté
		if (traitementAcquittementMessage(pMsg)) {
			_attenteAcquittementMsgVersion = false;

			// Je peux commencer à émettre les messages de watchdog
			demarreEmissionWatchdog();
		}
	}

	// Traitement reception non acquittement d'un message version
	private void traitementNonAcquittementMessageVersion(String pMsg) {
		traitementNonAcquittementMessage(pMsg);
	}

	// Traitement réception du message version du client
	private void traitementReceptionMessageVersionClient(String pMsg) {
		Gson gson = new Gson();
		MessageVersion msgVer;

		try {
			msgVer = gson.fromJson(pMsg, MessageVersion.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message version client", e);
			return;
		}

		if (msgVer == null) {
			return;
		}

		// TODO traitement des informations de version à faire. Emission ack ou nack

		if (msgVer.siteId != null && msgVer.siteId.size() > 0) {
			_identifiantClientRsmp = msgVer.siteId.get(0).sId;
		}

		// Envoi du message d'acquittement à au client
		emissionMessageAcquittement(msgVer.mId);

		_attenteMsgVersionClient = false;

		// C'est à mon tour d'émettre le message Version
		gson = new Gson();

		String idServeur = _configuration.getString("idServeurRsmp");
		List<String> versionsRsmp = Arrays.stream(_configuration.getString("listeVersionsRsmp").split(";"))
				.collect(Collectors.toList());

		String versionSxl = _configuration.getString("versionSxlSupporte");

		MessageVersion msg = new MessageVersion(UUID.randomUUID().toString(), idServeur, versionsRsmp, versionSxl);
		// Ajout dans la liste des messages en attente d'acquittement
		_messagesEnvoyes.add(new HorodateMessageRsmp(msg, LocalDateTime.now()));

		String json = gson.toJson(msg);
		if (_subject.hasObservers()) {
			_subject.onNext(new MessageAvecOrigine(json, Origine.MESSAGERIE_RSMP));
		} else {
			// Plus personne à l'écoute, je sort
			forceArretCommunicationRsmp();
			return;
		}

		// Log de la trame émise
		_logger.getLoggerJsonEmis().log(Level.DEBUG, json);

	}

	// Traitement réception du message watchog du client
	private void traitementReceptionMessageWatchdog(String pMsg) {
		Gson gson = new Gson();
		MessageWatchdog msgWdog;

		try {
			msgWdog = gson.fromJson(pMsg, MessageWatchdog.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message watchdog client", e);
			return;
		}

		if (msgWdog == null) {
			return;
		}

		// Envoi du message d'acquittement à au client
		emissionMessageAcquittement(msgWdog.mId);
	}

	// Traitement reception de l'acquittement d'un message
	// Supprime le message en attente de cet acquittement
	// Retourne true si OK, sinon false
	private boolean traitementAcquittementMessage(String pMsg) {
		Gson gson = new Gson();
		MessageAcknowledgement msgAck;

		try {
			msgAck = gson.fromJson(pMsg, MessageAcknowledgement.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message d'acquittment", e);
			return false;
		}

		if (msgAck == null) {
			return false;
		}

		// Récupération du message en attente
		IdentifiantMessage msg = getMessageAvecUUID(msgAck.oMId);

		// Cas message reçu n'est pas l'acquittement du message en attente
		if (msg == null) {
			return false;
		}

		// Mon message est acquitté
		supprimeMessageEnAttente(msg);

		return true;
	}

	// Traitement reception non acquittement d'un message
	private void traitementNonAcquittementMessage(String pMsg) {
		Gson gson = new Gson();
		MessageNotAcknowledgement msgNotAck;

		try {
			msgNotAck = gson.fromJson(pMsg, MessageNotAcknowledgement.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message non acquittment", e);
			return;
		}

		if (msgNotAck == null) {
			return;
		}

		// Récupération du message en attente
		IdentifiantMessage msg = getMessageAvecUUID(msgNotAck.oMId);

		// Cas message reçu n'est pas l'acquittement d'un message en attente
		if (msg == null) {
			return;
		}

		// Un de mes messages est refusé, je coupe la communication RSMP
		// TODO A reflechir à un autre traitement ...
		forceArretCommunicationRsmp();
	}

	// Reception d'un message tunnelisé dans un message tunnel RSMP
	private void traitementReceptionMessageTunnelResponse(String pMsg) {
		Gson gson = new Gson();
		MessageTunnelResponse msgTxt;

		try {
			msgTxt = gson.fromJson(pMsg, MessageTunnelResponse.class);
		} catch (JsonSyntaxException e) {
			_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.DEBUG,
					"Erreur decodage JSON du message MessageTunnelResponse", e);
			return;
		}

		if (msgTxt == null) {
			return;
		}
		
		// Si pas de réponse reçue
		if(msgTxt.rvs == null || msgTxt.rvs.size() == 0 || msgTxt.rvs.get(0).v == null) {
			return;
		}

		// Passe la réponse à la couche de gestion des communications externes
		if (_subject.hasObservers()) {
			_subject.onNext(new MessageAvecOrigine(msgTxt.rvs.get(0).v, Origine.CLIENT_EXTERNE));
		} else {
			// Plus personne à l'écoute, je sort
			forceArretCommunicationRsmp();
			return;
		}

		// Envoi du message d'acquittement au client
		emissionMessageAcquittement(msgTxt.mId);
	}

	// Acquittement du message reçu
	private void emissionMessageAcquittement(String pIdMsgAAcquitter) {
		// Rien à acquitter si chaîne ID vide
		if (pIdMsgAAcquitter == null || pIdMsgAAcquitter.trim().length() == 0) {
			return;
		}

		Gson gson = new Gson();

		MessageAcknowledgement msgAck = new MessageAcknowledgement(pIdMsgAAcquitter);
		String json = gson.toJson(msgAck);

		if (_subject.hasObservers()) {
			_subject.onNext(new MessageAvecOrigine(json, Origine.MESSAGERIE_RSMP));
		} else {
			// Plus personne à l'écoute, je sort
			forceArretCommunicationRsmp();
			return;
		}

		// Log de la trame émise
		_logger.getLoggerJsonEmis().log(Level.DEBUG, json);
	}

	// Retourne le message avec cet UUID ou null si non trouvé
	private IdentifiantMessage getMessageAvecUUID(String pUuid) {
		if (pUuid == null || pUuid.length() == 0) {
			return null;
		}

		HorodateMessageRsmp horoMsg = _messagesEnvoyes.stream()
				.filter(m -> pUuid.equals(m._message.getIdentifiantMessage())).findFirst().orElse(null);

		return horoMsg != null ? horoMsg._message : null;
	}

	// Supprime ce message dans la liste
	private void supprimeMessageEnAttente(IdentifiantMessage pMsg) {
		HorodateMessageRsmp horoMsg = _messagesEnvoyes.stream().filter(m -> m._message.equals(pMsg)).findFirst()
				.orElse(null);

		if (horoMsg != null) {
			_messagesEnvoyes.remove(horoMsg);
		}
	}

	/**
	 * Réception d'une commande extérieure à traduire en RSMP et à émettre au client
	 * 
	 * @param pCommande la commande à émettre
	 * @param pIdClient l'id du client à utiliser
	 */
	protected void commandePourClient(String pCommande, String pIdClient) {
		Gson gson = new Gson();

		MessageTunnelRequest msgTunnelRequest = new MessageTunnelRequest(UUID.randomUUID().toString(), pCommande,
				pIdClient);

		String json = gson.toJson(msgTunnelRequest);

		// Ajout dans la liste des messages en attente d'acquittement
		_messagesEnvoyes.add(new HorodateMessageRsmp(msgTunnelRequest, LocalDateTime.now()));

		if (_subject.hasObservers()) {
			_subject.onNext(new MessageAvecOrigine(json, Origine.MESSAGERIE_RSMP));
		} else {
			// Plus personne à l'écoute, je sort
			forceArretCommunicationRsmp();
			return;
		}

		// Log de la trame émise
		_logger.getLoggerJsonEmis().log(Level.DEBUG, json);
	}

	/**
	 * Demande à arrêter la communication RSMP
	 */
	protected void forceArretCommunicationRsmp() {
		_arretCommunication = true;

		if (_subject.hasObservers()) {
			_subject.onComplete();
		}

		_logger.getLoggerEvenements(TraitementProtocoleRsmp.class).log(Level.ERROR,
				"Problème messagerie RSMP ou demande arrêt extérieur -> Arrêt communication");
	}

	// Emission watchdog à fréquence régulière
	private void demarreEmissionWatchdog() {
		new Thread(() -> {
			while (!_arretCommunication) {
				Gson gson = new Gson();

				MessageWatchdog msgWDog = new MessageWatchdog(UUID.randomUUID().toString(),
						LocalDateTime.now().toString());
				String json = gson.toJson(msgWDog);

				// Ajout dans la liste des messages en attente d'acquittement
				_messagesEnvoyes.add(new HorodateMessageRsmp(msgWDog, LocalDateTime.now()));

				if (_subject.hasObservers()) {
					_subject.onNext(new MessageAvecOrigine(json, Origine.MESSAGERIE_RSMP));
				} else {
					// Plus personne à l'écoute, je sort
					forceArretCommunicationRsmp();
					return;
				}

				// Log de la trame émise
				_logger.getLoggerJsonEmis().log(Level.DEBUG, json);

				try {
					Thread.sleep(FREQUENCE_EMISSION_WATCHDOG);
				} catch (InterruptedException e) {
				}
			}
		}).start();

	}

	// Contrôle des message en attente d'acuqittement, à fréquence régulière
	private void demarreSurveillanceMessagesEnAttente() {
		new Thread(() -> {
			while (!_arretCommunication) {
				LocalDateTime now = LocalDateTime.now();

				// Contrôle de l'horodate sur chaque message en attente
				for (HorodateMessageRsmp msgEnAttente : _messagesEnvoyes) {
					if (now.minus(ANCIENNETE_MAX_MESSAGES, ChronoUnit.MILLIS).isAfter(msgEnAttente._horodate)) {
						// Acquittement non reçu, je sort
						forceArretCommunicationRsmp();

						break;
					}
				}

				try {
					Thread.sleep(FREQUENCE_CONTROLE_RECEPTION_ACK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

}
