package uk.co.bigsoft.greenmail.http.commands;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserManager;

import io.javalin.http.Context;

public class Utils {

	public GreenMailUser getUser(Context ctx, UserManager um) {
		String user = ctx.pathParam("email");
		return um.getUserByEmail(user);
	}

	public MailFolder getMailbox(Context ctx, ImapHostManager im) {
		String mailbox = ctx.pathParam("mailbox").replace("%23", "#");
		MailFolder m = im.getStore().getMailbox(mailbox);
		return m;
	}

	public long getUid(Context ctx) {
		String sUid = ctx.pathParam("uid");
		Long uid = new Long(sUid);
		return uid.longValue();
	}
}
