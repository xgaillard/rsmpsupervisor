package org.signature.rsmpSupervisor.communication;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.signature.rsmpSupervisor.FabriqueRsmpSupervisor;
import org.signature.rsmpSupervisor.communication.diaser.ProviderServiceCommunicationDiaser;
import org.signature.rsmpSupervisor.communication.diaser.ServiceCommunicationDiaser;
import org.signature.rsmpSupervisor.communication.lcr.ProviderServiceCommunicationLcr;
import org.signature.rsmpSupervisor.communication.lcr.ServiceCommunicationLcr;
import org.signature.rsmpSupervisor.communication.rsmp.ServiceCommunicationSocketRsmp;
import org.signature.rsmpSupervisor.communication.rsmp.TraitementProtocoleRsmp;
import org.signature.rsmpSupervisor.communication.tcp.IConnexionClientTcp;
import org.signature.rsmpSupervisor.communication.tcp.ServeurTcp;
import org.signature.rsmpSupervisor.resources.ConfigurationRsmpSupervisor;

import com.google.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Gestion des communications avec la Supervision et le client RSMP
 * 
 * @author SDARIZCUREN
 *
 */
public class GestionnaireCommunication implements Observer<MessageClient>, IConnexionClientTcp {
	private final ServeurTcp _serveurRSMPCommunicationTcp;
	private final ProviderServiceCommunicationLcr _providerServiceCommunicationLcr;
	private final ProviderServiceCommunicationDiaser _providerServiceCommunicationDiaser;
	private final ConfigurationRsmpSupervisor _configuration;

	private List<ServiceCommunicationSocketRsmp> _listCommunicationsSocketRsmp;

	private List<IServicePostReponseRsmp> _listServiceCommunicationLcrDiaser;

	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private GestionnaireCommunication(ServeurTcp pServeurTcp,
			ProviderServiceCommunicationLcr pProviderServiceCommunicationLcr,
			ProviderServiceCommunicationDiaser pProviderServiceCommunicationDiaser,
			ConfigurationRsmpSupervisor pConfiguration) {
		_serveurRSMPCommunicationTcp = pServeurTcp;
		_providerServiceCommunicationLcr = pProviderServiceCommunicationLcr;
		_providerServiceCommunicationDiaser = pProviderServiceCommunicationDiaser;
		_configuration = pConfiguration;

		_listCommunicationsSocketRsmp = new CopyOnWriteArrayList<>();

		_listServiceCommunicationLcrDiaser = new ArrayList<>();
	}

	/**
	 * Démarrage des services de communication
	 */
	public void lancementServices() {
		// Communication RSMP
		int tcpRsmp = Integer.valueOf(_configuration.getString("tcpServeurRsmp"));

		// Avec sécurisation TLS
		boolean avecTls = _configuration.getString("avecSecurisationTLS").equals("OUI");
		String nomFichier = _configuration.getString("nomMagasinCles");
		String motPasseFichier = _configuration.getString("motPasseMagasinCles");

		_serveurRSMPCommunicationTcp.lancementService(this, tcpRsmp, avecTls, nomFichier, motPasseFichier);

		lancementServicesLcr();
		lancementServicesDiaser();
	}

	// Services de communication LCR
	public void lancementServicesLcr() {
		// Démarre un service de communication LCR sur chaque port TCP à écouter
		List<Integer> listeTcpPortLcr = Arrays.asList(_configuration.getString("listeTcpServeurLcr").split(";"))
				.stream().map(p -> Integer.valueOf(p)).collect(Collectors.toList());

		// Pour chaque port TCP, un id de client est associé
		List<String> listeIdsClientsRsmp = Arrays
				.asList(_configuration.getString("listeIdsClientsAutorisesLCR").split(";")).stream()
				.collect(Collectors.toList());

		for (int i = 0; i < listeTcpPortLcr.size(); i++) {
			String idClient = listeIdsClientsRsmp.size() > i ? listeIdsClientsRsmp.get(i) : "";
			int tcpPort = listeTcpPortLcr.get(i);

			// Je sauvegarde les services LCR créés car il faudra leurs remonter les trames
			// à retourner au Superviseur LCR
			ServiceCommunicationLcr service = _providerServiceCommunicationLcr.construitNouveauServeurTcp();
			_listServiceCommunicationLcrDiaser.add(service);

			service.lancementServices(this, tcpPort, idClient);
		}
	}

	// Services de communication Diaser
	public void lancementServicesDiaser() {
		// Démarre un service de communication Diaser sur chaque port UDP à écouter
		List<Integer> listeUdpPortDiaser = Arrays.asList(_configuration.getString("listeUdpServeurDiaser").split(";"))
				.stream().map(p -> Integer.valueOf(p)).collect(Collectors.toList());

		// Pour chaque port UDP, un id de client est associé
		List<String> listeIdsClientsRsmp = Arrays
				.asList(_configuration.getString("listeIdsClientsAutorisesDiaser").split(";")).stream()
				.collect(Collectors.toList());

		for (int i = 0; i < listeUdpPortDiaser.size(); i++) {
			String idClient = listeIdsClientsRsmp.size() > i ? listeIdsClientsRsmp.get(i) : "";
			int udport = listeUdpPortDiaser.get(i);

			// Je sauvegarde les services UDP créés car il faudra leurs remonter les trames
			// à retourner au Superviseur LCR ou Diaser
			ServiceCommunicationDiaser service = _providerServiceCommunicationDiaser
					.construitNouveauServiceCommunicationDiaser();
			_listServiceCommunicationLcrDiaser.add(service);

			service.lancementServices(this, udport, idClient);
		}
	}

	/**
	 * Arrêt des services de communication
	 */
	public void arretServices() {
		_serveurRSMPCommunicationTcp.arretService();
	}

	/**
	 * Informe d'une nouvelle connexion TCP. c'est une connexion engagée par un
	 * client RSMP
	 * 
	 * @param pSocket la socket connectée
	 */
	public synchronized void nouvelleConnexionTcp(Socket pSocket) {
		// Création du service de communication pour la socket TCP, sous protocole RSMP
		ServiceCommunicationSocketRsmp serviceSocketRsmp = FabriqueRsmpSupervisor.INSTANCE
				.getServiceCommunicationSocketRsmp();

		TraitementProtocoleRsmp protocoleRsmp = FabriqueRsmpSupervisor.INSTANCE.getTraitementProtocoleRsmp();

		serviceSocketRsmp.lancementCommunicationRsmp(pSocket, protocoleRsmp, this);

		// Ajout d'un suivi sur cette connexion
		_listCommunicationsSocketRsmp.add(serviceSocketRsmp);
	}

	/**
	 * Traitement d'une commande envoyé par un logiciel externe. La commande est à
	 * envoyée au client distant correspondant à l'id donné
	 * 
	 * @param pCommande la commande à envoyer tel quel
	 * @param pIdClient l'id du client destinataire
	 */
	public void traitementCommandeExterne(String pCommande, String pIdClient) {
		if (pCommande == null || pIdClient == null) {
			return;
		}

		// Recherche si un client avec cet id est connecté
		// Je recherche un client avec un id commençant par celui demandé, car les
		// clients se connectent avec un id de site et pas un id complet d'équipement
		ServiceCommunicationSocketRsmp service = _listCommunicationsSocketRsmp.stream()
				.filter(o -> !o.estDeconnecte() && pIdClient.startsWith(o.getIdRsmpClient())).findFirst().orElse(null);

		if (service != null) {
			service.commandePourClient(pCommande, pIdClient);
		}
	}

	/**
	 * Connection à un observable
	 * 
	 * @param d un objet permettant de mettre fin à la connexion
	 */
	@Override
	public void onSubscribe(@NonNull Disposable d) {
	}

	/**
	 * Reception d'un message posté par un observable
	 */
	@Override
	public void onNext(@NonNull MessageClient msg) {
		// Cas message d'erreur
		if (msg.erreurCommunication) {
			supprimeCommunicationEnErreur();
			return;
		}

		// A retourner au système externe à l'origine de la demande
		IServicePostReponseRsmp service = getServiceCommunicationLcrAssocie(msg.clientId);
		if (service != null) {
			service.postReponseRsmp(msg.msg);
		}
	}

	/**
	 * Erreur de communication
	 */
	@Override
	public void onError(@NonNull Throwable e) {
		supprimeCommunicationEnErreur();
	}

	/**
	 * Un observable à mis fin a ses communications
	 */
	@Override
	public void onComplete() {
		supprimeCommunicationEnErreur();
	}

	// Supprime les communications en erreur
	private void supprimeCommunicationEnErreur() {
		List<ServiceCommunicationSocketRsmp> elts = _listCommunicationsSocketRsmp.stream()
				.filter(o -> o.estDeconnecte()).collect(Collectors.toList());

		// Suppression des elements déconnectés
		elts.forEach(e -> _listCommunicationsSocketRsmp.remove(e));
	}

	// Retourne le service associé à cet id de client ou null si non trouvé
	private IServicePostReponseRsmp getServiceCommunicationLcrAssocie(String pIdClient) {
		// Je recherche un client avec un id commençant par celui demandé, car les
		// clients se connectent avec un id de site et pas un id complet d'équipement
		return _listServiceCommunicationLcrDiaser.stream().filter(s -> pIdClient.startsWith(s.getIdClientRsmp()))
				.findFirst().orElse(null);
	}

}
