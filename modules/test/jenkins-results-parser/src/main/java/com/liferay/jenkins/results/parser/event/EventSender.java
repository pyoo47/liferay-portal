/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser.event;

import com.liferay.jenkins.results.parser.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leslie Wong
 */
public class EventSender {

	public void notify(String eventName, Build build) {
		List<EventListener> eventListenerList = _eventListeners.get(eventName);

		for (EventListener eventListener : eventListenerList) {
			eventListener.update(build);
		}
	}

	public void subscribe(String eventName, EventListener eventListener) {
		List<EventListener> eventListenerList = _eventListeners.get(eventName);

		if (eventListenerList == null) {
			eventListenerList = new ArrayList<>();
		}

		eventListenerList.add(eventListener);

		_eventListeners.put(eventName, eventListenerList);
	}

	public void unsubscribe(String eventName, EventListener eventListener) {
		List<EventListener> eventListenerList = _eventListeners.get(eventName);

		if (eventListenerList != null) {
			eventListenerList.remove(eventListener);

			_eventListeners.put(eventName, eventListenerList);
		}
	}

	private final Map<String, List<EventListener>> _eventListeners =
		new HashMap<>();

}