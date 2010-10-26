package uk.org.elsie.eclipse.bot;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedServiceFactory;

import uk.org.elsie.osgi.bot.IrcEventConstants;
import uk.org.elsie.osgi.bot.PropertiesUtil;

public class ChannelCommandProvider implements CommandProvider {

	private static Log log = LogFactory.getLog(ChannelCommandProvider.class);
	private BundleContext bundleContext;
	private ConfigurationAdmin configAdmin;
	
	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}
	
	public void unsetConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = null;
	}

	public void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void _join(CommandInterpreter interpreter) throws Exception {
		String channelName = interpreter.nextArgument();
		if(channelName == null)
			throw new IllegalArgumentException("no channel name provided");
		
		Configuration chan = findChannel(channelName);
		if(chan != null)
			throw new IllegalArgumentException("channel already registered");

		Configuration channelConfig = configAdmin.createFactoryConfiguration("uk.org.elsie.osgi.bot.Channel", null);
		
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.putAll(PropertiesUtil.propertiesAsMap(channelConfig.getProperties()));
		props.put(IrcEventConstants.IRC_CHANNEL, channelName);
		channelConfig.update(props);
	}
	
	public void _part(CommandInterpreter interpreter) throws Exception {
		String channelName = interpreter.nextArgument();
		if(channelName == null)
			throw new IllegalArgumentException("no channel name provided");
		
		Configuration chan = findChannel(channelName);
		if(chan == null)
			throw new IllegalArgumentException("no such channel registered");
		
		chan.delete();
	}
	
	Configuration findChannel(String channelName) throws IOException, InvalidSyntaxException {
		Configuration[] configs = configAdmin.listConfigurations("(&(" + IrcEventConstants.IRC_CHANNEL + "=" + channelName + ")(service.factoryPid=uk.org.elsie.osgi.bot.Channel))");
		if(configs == null || configs.length == 0)
			return null;
		if(configs.length > 1) {
			log.warn("more than one channel named " + channelName);
		}
		return configs[0];
	}
}
