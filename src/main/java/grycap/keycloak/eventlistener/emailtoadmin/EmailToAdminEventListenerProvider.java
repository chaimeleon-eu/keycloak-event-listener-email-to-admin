package grycap.keycloak.eventlistener.emailtoadmin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.InMemoryUserAdapter;

public class EmailToAdminEventListenerProvider implements EventListenerProvider {
    private static final Logger LOG = Logger.getLogger(EmailToAdminEventListenerProvider.class);

    private final KeycloakSession session;
    private final String[] emailReceivers;
    private final String emailSubject;

    public EmailToAdminEventListenerProvider(KeycloakSession session, String[] emailReceivers, String emailSubject){
        this.session = session;
        this.emailReceivers = emailReceivers;
        this.emailSubject = emailSubject;
    }

    private void sendEmail(UserModel user, String client, UserModel emailReceiverUser){
        Map<String,List<String>> attributes = user.getAttributes();
        String emailContent = "User account created or updated\n\n"
            + "Client:                " + client  + "\n"
            + "Provider:              " + user.getFederationLink() + "\n"
            + "Username:              " + user.getUsername() + "\n"
            + "Email:                 " + user.getEmail() + "\n"
            //+ "Email verified:       " + String.valueOf(user.isEmailVerified()) + "\n"
            + "First name:            " + user.getFirstName() + "\n"
            + "Last name:             " + user.getLastName() + "\n";
            List<String> otherAttributes = Arrays.asList("companyOrOrganization", "projects", "requiredRoles", 
                                                         "eucaimNegotiationID", "additionalComments");
            for (String attributeName : otherAttributes) {
                String value = attributes.containsKey(attributeName) ? String.join(", ", attributes.get(attributeName)) : "";
                emailContent += String.format("%-22s %s\n", attributeName+":", value);
            }

        String emailHtmlContent = null; 
            // "<h3>New user registration</h3>"
            // + "<ul>"
            // + "<li>Username: " + user.getUsername() + "</li>"
            // + "<li>Email: " + user.getEmail() + "</li>"
            // + "</ul>";

        //DefaultEmailSenderProvider emailSender = new DefaultEmailSenderProvider(session);
        EmailSenderProvider emailSender = this.session.getProvider(EmailSenderProvider.class);
        RealmModel realm = this.session.getContext().getRealm();
        try {
            emailSender.send(realm.getSmtpConfig(), emailReceiverUser, this.emailSubject, emailContent, emailHtmlContent);
        } catch (EmailException e) {
            LOG.error("Error trying to send email.", e);
        }
    }

    @Override
    public void close() {
        // Nothing to do here
    }
    
    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER
            || event.getType() == EventType.UPDATE_EMAIL
            || event.getType() == EventType.UPDATE_PROFILE) 
        {
            LOG.infof("Event received [%s], notifying by email to the admin...", event.getType());
            RealmModel realm = this.session.realms().getRealm(event.getRealmId());
            UserModel newRegisteredUser = this.session.users().getUserById(realm, event.getUserId());
            for (String emailReceiver : this.emailReceivers) {
                // Let's create a temporal user in memory just for the destination email to send
                UserModel emailReceiverUser = new InMemoryUserAdapter(this.session, realm, UUID.randomUUID().toString());
                emailReceiverUser.setEmail(emailReceiver);
                this.sendEmail(newRegisteredUser, event.getClientId(), emailReceiverUser);
            }
            LOG.infof("Email successfully sent.");
        }
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Nothing to do for admin events
    }
}
