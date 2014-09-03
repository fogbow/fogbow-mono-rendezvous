package org.fogbowcloud.rendezvous.xmpp.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

public class RSM {

	private int max;
	private String after;

	public RSM(Element queryEl, int defaultMax) {
		Element setEl = queryEl.element("set");		

		if (setEl != null) {
			int max;
			if (setEl.element("max") != null || setEl.element("max").getText().isEmpty()) {
				String maxString = setEl.element("max").getText();
				max = Integer.parseInt(maxString);
			} else {
				max = Integer.MAX_VALUE;
			}
			this.max = Math.min(max, defaultMax);

			Element afterEl = setEl.element("after");
			if (afterEl != null) {
				this.after = afterEl.getText();
			}
		}
	}

	public static RSM parse(Element queryEl, int defaultMax) {
		return new RSM(queryEl, defaultMax);
	}

	public List<? extends FederationMember> filter(List<? extends FederationMember> list) {
		int size = list.size();
		List<FederationMember> orderedList = new LinkedList<FederationMember>(list);
		Collections.sort(orderedList);
		List<FederationMember> filteredList = new LinkedList<FederationMember>();
		int afterIndex = -1;
		if (after != null) {
			for (int i = 0; i < size; i++) {
				if (this.after.equals(orderedList.get(i).getId())) {
					afterIndex = i + 1;
					break;
				}
			}
		} else {
			afterIndex = 0;
		}
		if (size == 0 && after == null) {
			return list;
		}
		if (afterIndex != -1) {
			for (int i = afterIndex; i < afterIndex + max && i < size; i++) {
				filteredList.add(orderedList.get(i));
			}
		} else {
			return null;
		}
		return filteredList;
	}

	public Element appendSetElements(Element queryElement,
			List<? extends FederationMember> list) {
		String first = null;
		String last = null;
		Integer count = 0;
		if (!list.isEmpty()) {
			first = list.get(0).getId();
			last = list.get(list.size() - 1).getId();
			count = list.size();
		}

		Element setEl = queryElement.addElement("set",
				"http://jabber.org/protocol/rsm");
		if (first != null) {
			setEl.addElement("first").setText(first);
		}
		if (last != null) {
			setEl.addElement("last").setText(last);
		}
		setEl.addElement("count").setText(count.toString());
		return queryElement;
	}
}
