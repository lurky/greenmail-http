package uk.co.bigsoft.greenmail;

import java.util.Properties;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.standalone.HttpGreenMailStandaloneRunner;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.GreenMail;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import uk.co.bigsoft.greenmail.http.commands.CfgClientCommand;
import uk.co.bigsoft.greenmail.http.commands.CfgGreenMailCommand;
import uk.co.bigsoft.greenmail.http.commands.DeleteMailboxCommand;
import uk.co.bigsoft.greenmail.http.commands.DeleteMessageCommand;
import uk.co.bigsoft.greenmail.http.commands.DeleteUserCommand;
import uk.co.bigsoft.greenmail.http.commands.ImapAllMessagesCommand;
import uk.co.bigsoft.greenmail.http.commands.ImapGetInBoxCommand;
import uk.co.bigsoft.greenmail.http.commands.ImapListMailBoxCommand;
import uk.co.bigsoft.greenmail.http.commands.ListUsersCommand;
import uk.co.bigsoft.greenmail.http.commands.MailboxMessagesCommand;
import uk.co.bigsoft.greenmail.http.commands.PurgeEmailFromAllMailboxesCommand;
import uk.co.bigsoft.greenmail.http.commands.ReceivedMessagesCommand;
import uk.co.bigsoft.greenmail.http.commands.ReceivedMessagesForDomainCommand;
import uk.co.bigsoft.greenmail.http.commands.ResetCommand;
import uk.co.bigsoft.greenmail.http.commands.ViewMessageCommand;
import uk.co.bigsoft.greenmail.javalin.AccessControlAllowOriginHandler;
import uk.co.bigsoft.greenmail.mailx.MimeMessageBuilder;

public class Main {

	private static Cfg cfg = new Cfg();

	public static void main(String[] args) {
		final Properties properties = System.getProperties();

		HttpGreenMailStandaloneRunner runner = new HttpGreenMailStandaloneRunner(properties);
		runner.doRun(properties);

		populate(runner.getMailer());
		startHttpServer(runner.getMailer());
	}

	private static void populate(GreenMail gm) {
		if (!cfg.useTestData()) {
			return;
		}

		String SUPERMAN_USER = "superman";
		String SUPERMAN_PASS = "x-ray-vision";
		String SPIDERMAN_USER = "spiderman";
		String SPIDERMAN_PASS = "spin-webs";
		String SUPERMAN = "clarke.kent@superman.com";
		String SPIDERMAN = "peter.parker@spiderman.co.uk";
		String BATMAN = "bruce.wayne@batman.gotham.us";
		String WONDER_WOMAN = "diana.prince@wonderwoman.com";
		String TGAH = "ralph.hinkley@thegreatestamericanhero.com";
		String SFH = "stringfellow.hawk@airwolf.com";

		try {

			UserManager um = gm.getManagers().getUserManager();
			ImapHostManager im = gm.getManagers().getImapHostManager();

			GreenMailUser superman = um.createUser(SUPERMAN, SUPERMAN_USER, SUPERMAN_PASS);
			GreenMailUser spiderman = um.createUser(SPIDERMAN, SPIDERMAN_USER, SPIDERMAN_PASS);

			MailFolder supermanInbox = im.getInbox(superman);
			MailFolder supermanPofF = im.createMailbox(superman, "PalaceOfF");
			MailFolder spidermanWebjuce = im.createMailbox(spiderman, "web-juice");

			MimeMessage m1 = new MimeMessageBuilder(gm.getSmtp().createSession()).withSubject("Ears").withFrom(SUPERMAN)
					.withTo(BATMAN).withBody("I like the pointy ears on your hat.").build();

			MimeMessage m2 = new MimeMessageBuilder(gm.getSmtp().createSession()).withSubject("Gym").withFrom(SPIDERMAN)
					.withTo(WONDER_WOMAN).withBody("You look great, can you recommend a good gym?").build();

			MimeMessage m3 = new MimeMessageBuilder(gm.getSmtp().createSession()).withSubject("Lift").withFrom(BATMAN)
					.withTo(SUPERMAN).withBody("I need a lift to Gotham.").build();

			MimeMessage m4 = new MimeMessageBuilder(gm.getSmtp().createSession()).withSubject("Re: Gym")
					.withFrom(WONDER_WOMAN).withTo(SPIDERMAN)
					.withBody("Why thank you! The best gym is Gymmy's in Birkdale, UK.").build();

			MimeMessage m5 = new MimeMessageBuilder(gm.getSmtp().createSession()).withSubject("New suit")
					.withFrom(BATMAN).withCc(BATMAN).withCc(WONDER_WOMAN).withTo(SUPERMAN).withTo(SPIDERMAN)
					.withBcc(TGAH).withBcc(SFH)
					.withBody("I'm having a party to show off my new suit. Do you want to come?").build();

			System.out.println("");
			System.out.println("================ STORING TEST EMAILS - Start");
			System.out.println("");
			System.out.println("On my computer storing emails takes ages.");
			System.out.println("So if it sticks for you then please wait a second or two.");
			System.out.println("If it does stick for you, maybe you can help me figure out why!");
			System.out.println("");

			System.out.println("|......|");
			System.out.print("|*");
			supermanInbox.store(m1);

			System.out.print("*");
			supermanInbox.store(m5);

			System.out.print("*");
			supermanInbox.store(m3);

			System.out.print("*");
			supermanPofF.store(m2);

			System.out.print("*");
			spidermanWebjuce.store(m4);
			System.out.println("|");

			System.out.println("================ STORING TEST EMAILS - Done");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void startHttpServer(GreenMail greenMail) {
		Javalin app = Javalin.create().start(7000);
		app.config.addStaticFiles("/frontend", Location.CLASSPATH);
		app.get("/imap/:email/inbox", new ImapGetInBoxCommand(greenMail));
		app.get("/imap/:email", new ImapListMailBoxCommand(greenMail));
		app.get("/imap", new ImapAllMessagesCommand(greenMail));
		app.get("/lu", new ListUsersCommand(greenMail));
		app.get("/p", new PurgeEmailFromAllMailboxesCommand(greenMail));
		app.get("/rm", new ReceivedMessagesCommand(greenMail));
		app.get("/rmd/:domain", new ReceivedMessagesForDomainCommand(greenMail));
		app.get("/r", new ResetCommand(greenMail));
		app.get("/cfg/greenmail", new CfgGreenMailCommand(greenMail));
		app.get("/cfg/client", new CfgClientCommand(greenMail));
		app.get("/m/:mailbox", new MailboxMessagesCommand(greenMail));
		app.get("/m/:mailbox/delete", new DeleteMailboxCommand(greenMail));
		app.get("/d/:mailbox/:uid", new DeleteMessageCommand(greenMail));
		app.get("/v/:mailbox/:uid", new ViewMessageCommand(greenMail));
		app.get("/u/:email/delete", new DeleteUserCommand(greenMail));

		if (cfg.useAccessControlAnywhere()) {
			System.out.println("Allow REST connections from anywhere");
			app.after("/*", new AccessControlAllowOriginHandler("*"));
		}
	}
}
