package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;

@DefaultLocale("en_US")
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface WebTransMessages extends Messages {
	
	@DefaultMessage("{0} users online")
	@PluralText({"one", "One user online"})
	@Description("Title of the minimized users panel")
	String nUsersOnline(@PluralCount int numUsers);

	
	@DefaultMessage("{0}%")
	String statusBarLabelPercentage(int approved, @Optional int needReview, @Optional int untranslated);
	
	@DefaultMessage("{0}/{1}/{2}")
	String statusBarLabelUnits(int approved, int needReview, int untranslated);
		
}
