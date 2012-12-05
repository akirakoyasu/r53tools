package net.akirakoyasu.aws.r53tools;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.CreateHostedZoneRequest;
import com.amazonaws.services.route53.model.CreateHostedZoneResult;
import com.amazonaws.services.route53.model.HostedZone;

public class CreateZone extends R53Command {

	protected CreateZone(String cmdOptionsSyntax) {
		super(cmdOptionsSyntax, "Create a Route 53 zone");
	}
	
	public void command(String name) {
		// Amazon Route 53 has a single endpoint: route53.amazonaws.com.
		// It only supports HTTPS requests.
		AmazonRoute53Client client = new AmazonRoute53Client(credential);
		
		List<HostedZone> zones = Zones.findByName(client, name);
		
		if (!zones.isEmpty()) {
			System.out.println("[WARN] name: " + name + "is duplicated" );
		}
		
		CreateHostedZoneResult result = client.createHostedZone(
				new CreateHostedZoneRequest()
					.withName(name)
					.withCallerReference(
							Zones.computeCallerReference(CreateZone.class, name)));
		
		ChangeInfo info = result.getChangeInfo();
		System.out.printf("Submitted at: %s, request id: %s%n", info.getSubmittedAt(), info.getId());
		
		client.shutdown();
	}
	
	public static void main(String[] args) {
		CreateZone cmd = new CreateZone("<name>");
		
		Options options = new Options();
		CommandLine cl = cmd.parse(options, args);
		if (cl == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		List<String> argsRemaining = cl.getArgList();
		Iterator<String> i = argsRemaining.iterator();
		String name = i.next();
		
		cmd.command(name);
	}
}
