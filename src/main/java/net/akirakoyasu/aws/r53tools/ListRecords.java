package net.akirakoyasu.aws.r53tools;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;

public class ListRecords extends R53Command {

	protected ListRecords(String cmdOptionsSyntax) {
		super(cmdOptionsSyntax, "List Route 53 records");
	}
	
	public void command(String name) {
		// Amazon Route 53 has a single endpoint: route53.amazonaws.com.
		// It only supports HTTPS requests.
		AmazonRoute53Client client = new AmazonRoute53Client(credential);
		
		HostedZone zone = Zones.findByUniqueName(client, name);
		
		ListResourceRecordSetsResult result = client.listResourceRecordSets(
				new ListResourceRecordSetsRequest()
					.withHostedZoneId(zone.getId()));
		for (ResourceRecordSet recordSet : result.getResourceRecordSets()) {
			recordSetPrinter.print(recordSet);
		}
		
		client.shutdown();
	}
	
	private static RecordSetPrinter recordSetPrinter = new RecordSetPrinter();
	
	private static class RecordSetPrinter {
		public void print(ResourceRecordSet recordSet) {
			System.out.printf("%s\t%s\t%s",
					recordSet.getName(),
					recordSet.getType(),
					recordSet.getTTL());
			Iterator<ResourceRecord> itr = recordSet.getResourceRecords().iterator();
			if (itr.hasNext()) {
				System.out.printf("\t%s", itr.next().getValue());
				while (itr.hasNext()) {
					System.out.printf(", %s", itr.next().getValue());
				}
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		ListRecords cmd = new ListRecords("<name>");
		
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
