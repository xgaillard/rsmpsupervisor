package org.signature.rsmpSupervisor.communication.lcr;

import org.signature.rsmpSupervisor.FabriqueRsmpSupervisor;

import com.google.inject.Inject;

/**
 * Un provider alimentant avec des ServiceCommunicationLcr à la demande
 * @author SDARIZCUREN
 *
 */
public class ProviderServiceCommunicationLcr {
	
	/**
	 * Construction de l'objet depuis l'injection de dépendances
	 */
	@Inject
	private ProviderServiceCommunicationLcr() {
	}
	
	/**
	 * Fourni un nouveau service de communication LCR à la demande
	 * @return une nouvelle instance d'un service de communication LCR
	 */
	public ServiceCommunicationLcr construitNouveauServeurTcp() {
		return FabriqueRsmpSupervisor.INSTANCE.getServiceCommunicationLcr();
	}

}
