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

		String maxString = setEl.element("max").getText();
		int max = Integer.parseInt(maxString);
		this.max = Math.min(max, defaultMax);

		Element afterEl = setEl.element("after");
		this.after = "";
		if (afterEl != null) {
			this.after = afterEl.getText();
		}
	}

	public static RSM parse(Element queryEl, int defaultMax) {
		return new RSM(queryEl, defaultMax);
	}

	public List<? extends RSMElement> filter(List<? extends RSMElement> list) {
		int size = list.size();
		List<RSMElement> orderedList = new LinkedList<RSMElement>(list);
		Collections.sort(orderedList);
		List<RSMElement> filteredList = new LinkedList<RSMElement>();
		if (size == 0) {
			return list;
		}
		int afterIndex = 0;
		for (int i = 0; i < size; i++) {
			if (this.after.equals(orderedList.get(i).getId())) {
				afterIndex = i + 1;
				break;
			}
		}
		for (int i = afterIndex; i < afterIndex + max && i < size; i++) {
			filteredList.add(orderedList.get(i));
		}
		return filteredList;
	}

	public Element appendSetElements(Element queryElement,
			List<? extends RSMElement> list) {
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
