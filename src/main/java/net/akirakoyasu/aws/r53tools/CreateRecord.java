package net.akirakoyasu.aws.r53tools;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;

public class CreateRecord extends R53Command {

	protected CreateRecord(String cmdOptionsSyntax) {
		super(cmdOptionsSyntax, "Create a Route 53 record");
	}
	
	public void command(String zoneName, String recordName, String type, String ttl, List<String> values) {
		// Amazon Route 53 has a single endpoint: route53.amazonaws.com.
		// It only supports HTTPS requests.
		AmazonRoute53Client client = new AmazonRoute53Client(credential);
		
		HostedZone zone = Zones.findByUniqueName(client, zoneName);
		
		ChangeResourceRecordSetsResult result = client.changeResourceRecordSets(
				new ChangeResourceRecordSetsRequest()
			.withHostedZoneId(zone.getId())
			.withChangeBatch(Records.createBatch(recordName, type, ttl, values)));
		
		ChangeInfo info = result.getChangeInfo();
		System.out.printf("Submitted at: %s, request id: %s%n", info.getSubmittedAt(), info.getId());
		
		client.shutdown();
	}
	
	public static void main(String[] args) {
		CreateRecord cmd = new CreateRecord(
				"-z <zone> -n <name> -t <type> -l <ttl> <value>...");
		
		Options options = new Options();
		options.addOption("z", "zone", true, "zone name");
		options.addOption("n", "name", true, "record name");
		options.addOption("t", "type", true, "TYPE");
		options.addOption("l", "ttl", true, "TTL");
		CommandLine cl = cmd.parse(options, args);
		if (cl == null) {
			return;
		}
		
		String zoneName = cl.getOptionValue("z");
		String recordName = cl.getOptionValue("n");
		String type = cl.getOptionValue("t");
		String ttl = cl.getOptionValue("l");
		
		@SuppressWarnings("unchecked")
		List<String> values = cl.getArgList();
		
		cmd.command(zoneName, recordName, type, ttl, values);
	}
}
