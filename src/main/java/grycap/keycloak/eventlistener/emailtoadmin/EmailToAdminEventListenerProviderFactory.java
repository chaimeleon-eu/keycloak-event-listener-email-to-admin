package grycap.keycloak.eventlistener.emailtoadmin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

public class EmailToAdminEventListenerProviderFactory implements EventListenerProviderFactory, ServerInfoAwareProviderFactory {
    private static final Logger LOG = Logger.getLogger(EmailToAdminEventListenerProviderFactory.class);
    
    private String[] emailReceivers;
    private String emailSubject;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new EmailToAdminEventListenerProvider(session, this.emailReceivers, this.emailSubject);
    }

    @Override
    public void init(Config.Scope config) {
        LOG.info( String.format( "### ------------  %s.init() ------------ ###", this.getId() ) );
        
        String[] emailReceiversArray = config.get("emailReceivers","").split(",");
        Set<String> emailReceiversSet = new HashSet<>();
        emailReceiversSet.addAll(Arrays.asList(emailReceiversArray));
        this.emailReceivers = emailReceiversSet.toArray(new String[0]);
        this.emailSubject = config.get("emailSubject", "Keycloak - User account created or updated");
        
        LOG.info ("Email-to-admin event listener configuration variables: ");
        LOG.info ("\tEmail receivers: " + Arrays.toString(this.emailReceivers));
        LOG.info ("\tEmail subject: " + this.emailSubject);
        LOG.info( String.format("-----------------------------------------------------------") );
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to do here
    }

    @Override
    public void close() {
        // Nothing to do here
    }

    @Override
    public String getId() {
        /**
         * The getId() method is used to provide a unique identifier for the custom event listener provider factory.
         * This identifier is used within Keycloak to reference and lookup the correct factory when instances of the
         * EventListenerProvider interface are needed.
         * When an event occurs within Keycloak that needs to be broadcasted to listeners, Keycloak looks up the 
         * appropriate listener based on the ID returned by getId().
         */
        return "email-to-admin";
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        /**
         * This method is used to show info from this provider to the Keycloak admin user in the Server Info page
         * in th Keycloak Admin Console.
         */
        Map<String, String> ret = new LinkedHashMap<>();
        ret.put("emailReceivers", Arrays.toString(this.emailReceivers));
        ret.put("emailSubject", this.emailSubject);
        return ret;
    }
}
