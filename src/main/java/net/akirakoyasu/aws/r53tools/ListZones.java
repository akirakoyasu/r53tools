package net.akirakoyasu.aws.r53tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;

public class ListZones extends R53Command {

	protected ListZones(String cmdOptionsSyntax) {
		super(cmdOptionsSyntax, "List Route 53 zones");
	}
	
	public void command() {
		// Amazon Route 53 has a single endpoint: route53.amazonaws.com.
		// It only supports HTTPS requests.
		AmazonRoute53Client client = new AmazonRoute53Client(credential);
		
		ListHostedZonesResult result = client.listHostedZones();
		
		for (HostedZone hostedZone : result.getHostedZones()) {
			zonePrinter.print(hostedZone);
		}
		while (result.isTruncated()) {
			result = client.listHostedZones(new ListHostedZonesRequest()
				.withMarker(result.getNextMarker()));
			for (HostedZone hostedZone : result.getHostedZones()) {
				zonePrinter.print(hostedZone);
			}
		}

		client.shutdown();
	}
	
	private static ZonePrinter zonePrinter = new ZonePrinter();
	
	private static class ZonePrinter {
		public void print(HostedZone hostedZone) {
			System.out.printf("%s\t%s\t%s%n",
					hostedZone.getId(),
					hostedZone.getName(),
					hostedZone.getResourceRecordSetCount());
		}
	}

	public static void main(String[] args) {
		ListZones cmd = new ListZones("");
		
		Options options = new Options();
		CommandLine cl = cmd.parse(options, args);
		if (cl == null) {
			return;
		}
		
		cmd.command();
	}
}
