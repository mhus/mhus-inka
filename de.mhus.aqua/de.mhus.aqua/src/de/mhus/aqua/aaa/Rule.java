package de.mhus.aqua.aaa;

import java.util.LinkedList;
import java.util.List;

import de.mhus.aqua.api.IUserRights;

public class Rule {

	private int policy;
	private List<Item> list = new LinkedList<Item>();
	
	public Rule(String right, Acl acl) {
		policy = acl.getPolicy();
	}

	public int validate(IUserRights rights) {
		int out = policy;
		for (Item item : list) {
			if (item.rg == UserRights.POLICY) {
				out = item.rgPolicy;
			} else
			if (rights.contains(item.rg,item.rgName)) {
				return item.rgPolicy;
			}
		}
		return out;
	}
	
	private class Item {

		public int rg;
		public String rgName;
		public int rgPolicy;

		public Item(int rg, String rgName, int rgPolicy) {
			this.rg = rg;
			this.rgName = rgName;
			this.rgPolicy = rgPolicy;
		}
		
		public String toString() {
			return "[" + rg +"," + rgName + "," + rgPolicy + "]";
		}
	}
	
		
		Rule getRule() {
			return Rule.this;
		}
		
		public void append(int rg, String rgName, int rgPolicy) {
			list.add(new Item(rg,rgName,rgPolicy));
		}

		public String toString() {
			return list.toString();
		}
		
	
}
