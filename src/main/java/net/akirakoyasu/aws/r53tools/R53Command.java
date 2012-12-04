package net.akirakoyasu.aws.r53tools;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

abstract public class R53Command {
	
	public static final String ENV_ACCESSKEY_ID = "R53TOOLS_ACCESSKEY_ID";
	public static final String ENV_SECRET_ACCESSKEY = "R53TOOLS_SECRET_ACCESSKEY";

	protected AWSCredentials credential;
	private final String cmdOptionsSyntax;
	private final String cmdDesc;

	protected R53Command(String cmdOptionsSyntax) {
		this(cmdOptionsSyntax, "");
	}

	protected R53Command(String cmdOptionsSyntax, String cmdDesc) {
		this.cmdOptionsSyntax = cmdOptionsSyntax;
		this.cmdDesc = cmdDesc;
	}
	
	protected CommandLine parse(String[] args) {
		return parse(new Options(), args);
	}
	
	@SuppressWarnings("static-access")		// commons-cli
	protected CommandLine parse(Options options, String[] args) {
		
		options.addOption(OptionBuilder
				.withDescription("AWS accesskey id, instead of env " + ENV_ACCESSKEY_ID)
				.hasArg()
				.withArgName("id")
				.withLongOpt("id")
				.create("i"));
		options.addOption(OptionBuilder
				.withDescription("AWS secret accesskey, instead of env " + ENV_SECRET_ACCESSKEY)
				.hasArg()
				.withArgName("secret")
				.withLongOpt("secret")
				.create("s"));
		options.addOption("h", "help", false, "Print help");
		options.addOption("v", "verbose", false, "In verbose, showing requests");

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cl = parser.parse(options, args);
	
			if (cl.hasOption("h")) {
				printHelp(options);
				return null;
			}
			
			Logger awsLogger = Logger.getLogger("com.amazonaws");
			if (cl.hasOption("v")) {
				awsLogger.setLevel(Level.FINE);
				awsLogger.addHandler(new ConsoleHandler(){{
					setOutputStream(System.out);
					setLevel(Level.FINE);
				}});
			}
			
			String id = cl.getOptionValue("i", System.getenv(ENV_ACCESSKEY_ID));
			checkArgument(id != null, "accesskey id must be set");
			String secret = cl.getOptionValue("s",
					System.getenv(ENV_SECRET_ACCESSKEY));
			checkArgument(secret != null, "secret accesskey must be set");
	
			credential = new BasicAWSCredentials(id, secret);
	
			return cl;
			
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options);
			return null;
		}
	}
	
	protected void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(this.getClass().getSimpleName()
				+ " [-i <id>] [-s <secret>] [-v] " + cmdOptionsSyntax, cmdDesc, options, "");
	}
}
