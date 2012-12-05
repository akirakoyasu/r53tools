package net.akirakoyasu.aws.r53tools;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesRequest;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

class Zones {
	private Zones(){}
	
	public static List<HostedZone> findByName(AmazonRoute53Client client, String name) {
		
		if (!name.endsWith(".")) {
			name = name + ".";
		}
		final String zoneName = name;
		final Predicate<HostedZone> nameFilter = new Predicate<HostedZone>(){
			@Override
			public boolean apply(HostedZone input) {
				return input.getName().equals(zoneName);
			}
		};
		
		ListHostedZonesResult result = client.listHostedZones();
		Iterable<HostedZone> filterdZones
			= Iterables.filter(result.getHostedZones(), nameFilter);
		while (result.isTruncated()) {
			result = client.listHostedZones(new ListHostedZonesRequest()
				.withMarker(result.getNextMarker()));
			filterdZones = Iterables.concat(filterdZones,
					Iterables.filter(result.getHostedZones(), nameFilter));
		}
		List<HostedZone> zones = Lists.newArrayList(filterdZones);
		
		return zones;
	}
	
	public static HostedZone findByUniqueName(AmazonRoute53Client client, String name) {
		List<HostedZone> zones = findByName(client, name);
		
		checkArgument(zones.size() > 0, "Name: " + name + " not found");
		checkArgument(zones.size() < 2, "Duplicated Name: " + name);
		
		return zones.get(0);
	}
	
	public static String computeCallerReference(Class<?> clazz, String name) {
		return clazz.getName() + "_" + name + "_" + (new Date()).getTime();
	}
}
