package net.akirakoyasu.aws.r53tools;

import static com.amazonaws.services.route53.model.ChangeAction.CREATE;
import static com.amazonaws.services.route53.model.ChangeAction.DELETE;

import java.util.List;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

class Records {
	private Records() {
	}
	
	private static final Function<String, ResourceRecord> toNewRecord
		= new Function<String, ResourceRecord>(){
		@Override
		public ResourceRecord apply(String input) {
			return new ResourceRecord()
					.withValue(input);
		}};
	
	public static Function<String, ResourceRecord> toNewRecord() {
		return toNewRecord;
	}
	
	public static ChangeBatch createBatch(String recordName, String type, String ttl, List<String> values) {
		return batch(CREATE, recordName, type, ttl, values);
	}
	
	public static ChangeBatch deleteBatch(String recordName, String type, String ttl, List<String> values) {
		return batch(DELETE, recordName, type, ttl, values);
	}
	
	private static ChangeBatch batch(ChangeAction action, String recordName,
			String type, String ttl, List<String> values) {
		List<ResourceRecord> records = Lists.newArrayList(
				Iterables.transform(values, toNewRecord()));
		
		return new ChangeBatch()
			.withChanges(new Change()
				.withAction(action)
				.withResourceRecordSet(new ResourceRecordSet()
					.withName(recordName)
					.withType(type)
					.withTTL(Long.valueOf(ttl))
					.withResourceRecords(records)));
	}
}
