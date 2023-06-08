package org.signature.rsmpSupervisor.communication.diaser;

import org.signature.rsmpSupervisor.FabriqueRsmpSupervisor;

import com.google.inject.Inject;

/**
 * Un provider alimentant avec des ServiceCommunicationDiaser à la demande
 * @author SDARIZCUREN
 *
 */
public class ProviderServiceCommunicationDiaser {
	
	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ProviderServiceCommunicationDiaser() {
	}
	
	/**
	 * Fourni un nouveau service de communication Diaser à la demande
	 * @return une nouvelle instance d'un service de communication Diaser
	 */
	public ServiceCommunicationDiaser construitNouveauServiceCommunicationDiaser() {
		return FabriqueRsmpSupervisor.INSTANCE.getServiceCommunicationDiaser();
	}

}
