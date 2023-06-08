package org.signature.rsmpSupervisor;

import org.signature.rsmpSupervisor.communication.GestionnaireCommunication;
import org.signature.rsmpSupervisor.communication.diaser.ServiceCommunicationDiaser;
import org.signature.rsmpSupervisor.communication.lcr.ServiceCommunicationLcr;
import org.signature.rsmpSupervisor.communication.rsmp.ServiceCommunicationSocketRsmp;
import org.signature.rsmpSupervisor.communication.rsmp.TraitementProtocoleRsmp;
import org.signature.rsmpSupervisor.log.LoggerRsmpSupervisor;
import org.signature.rsmpSupervisor.resources.ConfigurationRsmpSupervisor;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/**
 * Une fabrique d'objets pour injecter
 * 
 * @author SDARIZCUREN
 *
 */
public enum FabriqueRsmpSupervisor {

	INSTANCE;

	private Injector _injector = Guice.createInjector(new AbstractModule() {
		@Override
		protected void configure() {
			// Mode Singleton. @Singleton sur les méthodes provides ne marche pas (?)
			bind(LoggerRsmpSupervisor.class).in(Scopes.SINGLETON);
			bind(ConfigurationRsmpSupervisor.class).in(Scopes.SINGLETON);
			bind(GestionnaireCommunication.class).in(Scopes.SINGLETON);

		}
	});

	/**
	 * Construction d'un objet LoggerRsmpSupervisor
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public LoggerRsmpSupervisor getLoggerRsmpSupervisor() {
		return _injector.getInstance(LoggerRsmpSupervisor.class);
	}

	/**
	 * Construction d'un objet ConfigurationRsmpSupervisor
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public ConfigurationRsmpSupervisor getConfigurationRsmpSupervisor() {
		return _injector.getInstance(ConfigurationRsmpSupervisor.class);
	}

	/**
	 * Construction d'un objet GestionnaireCommunication
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public GestionnaireCommunication getGestionnaireCommunication() {
		return _injector.getInstance(GestionnaireCommunication.class);
	}
	
	/**
	 * Construction d'un objet TraitementProtocoleRsmp
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public TraitementProtocoleRsmp getTraitementProtocoleRsmp() {
		return _injector.getInstance(TraitementProtocoleRsmp.class);
	}
	
	/**
	 * Construction d'un objet ServiceCommunicationSocketRsmp
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public ServiceCommunicationSocketRsmp getServiceCommunicationSocketRsmp() {
		return _injector.getInstance(ServiceCommunicationSocketRsmp.class);
	}
	
	/**
	 * Construction d'un objet ServiceCommunicationLcr
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public ServiceCommunicationLcr getServiceCommunicationLcr() {
		return _injector.getInstance(ServiceCommunicationLcr.class);
	}
	
	/**
	 * Construction d'un objet ServiceCommunicationDiaser
	 * 
	 * @return l'objet construit avec l'injection de dépendances
	 */
	@Provides
	public ServiceCommunicationDiaser getServiceCommunicationDiaser() {
		return _injector.getInstance(ServiceCommunicationDiaser.class);
	}
}
