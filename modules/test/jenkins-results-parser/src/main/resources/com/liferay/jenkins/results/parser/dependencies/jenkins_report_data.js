init_data = function() {
	document.getElementById("averageDelayTimeToStart").innerHTML = getAverageDelayTimeToStart();
	document.getElementById("longestDelayTimeToStart").innerHTML = getLongestDelayTimeToStart();
	document.getElementById("longestRunningDownstreamBuild").innerHTML = getLongestRunningDownstreamBuild();
	document.getElementById("topLevelBuildTime").innerHTML = getTopLevelBuildTime();
	document.getElementById("topLevelLink").innerHTML = getTopLevelLink();
	document.getElementById("topLevelStartTime").innerHTML = getTopLevelStartTime();
	document.getElementById("totalCPUUsageTime").innerHTML = getTotalCPUUsageTime();
	document.getElementById("totalJenkinsSlavesUsed").innerHTML = getTotalJenkinsSlavesUsed();
}

getAverageDelayTimeToStart = function() {
	var downstreamBuildCount = 0;
	var totalDelayTimeToStart = 0;

	var topLevelJSON = _getTopLevelJSON();

	var downstreamRunIDs = topLevelJSON.downstream_run_ids.split(",");

	for (var downstreamRunID in downstreamRunIDs) {
		var runID = downstreamRunIDs[downstreamRunID];

		if (runID == "") {
			continue;
		}

		downstreamBuildCount++;

		var buildJSON = build_database.builds[runID];

		totalDelayTimeToStart += (buildJSON.start_time - buildJSON.invocation_time);
	}

	return _getFormattedTime(totalDelayTimeToStart / downstreamBuildCount);
}

getLongestDelayTimeToStart = function() {
	var longestDelayTimeToStart = 0;
	var longestDelayTimeToStartBuildJSON;

	var topLevelJSON = _getTopLevelJSON();

	var downstreamRunIDs = topLevelJSON.downstream_run_ids.split(",");

	for (var downstreamRunID in downstreamRunIDs) {
		var runID = downstreamRunIDs[downstreamRunID];

		if (runID == "") {
			continue;
		}

		var buildJSON = build_database.builds[runID];

		var currentDelayTimeToStart = buildJSON.start_time - buildJSON.invocation_time;

		if (currentDelayTimeToStart > longestDelayTimeToStart) {
			longestDelayTimeToStart = currentDelayTimeToStart;
			longestDelayTimeToStartBuildJSON = buildJSON;
		}
	}

	if (longestDelayTimeToStartBuildJSON == null) {
		return "";
	}

	var html = [];

	html.push("<a href=\"");
	html.push(longestDelayTimeToStartBuildJSON.build_url);
	html.push("\">build</a> in ");
	html.push(_getFormattedTime(longestDelayTimeToStart));

	return html.join("");
}

getLongestRunningDownstreamBuild = function() {
	var builds = build_database.builds;

	var longestDuration = 0;
	var longestDurationJSON;

	var topLevelJSON = _getTopLevelJSON();

	var downstreamRunIDs = topLevelJSON.downstream_run_ids.split(",");

	for (var downstreamRunID in downstreamRunIDs) {
		var runID = downstreamRunIDs[downstreamRunID];

		if (runID == "") {
			continue;
		}

		var buildJSON = build_database.builds[runID];

		var currentDuration = buildJSON.build_duration;

		if (currentDuration > longestDuration) {
			longestDuration = currentDuration;
			longestDurationJSON = buildJSON;
		}
	}

	if (longestDurationJSON == null) {
		return "";
	}

	var html = [];

	html.push("<a href=\"");
	html.push(longestDurationJSON.build_url);
	html.push("\">build</a> in ");
	html.push(_getFormattedTime(longestDurationJSON.build_duration));

	return html.join("");
}

getTopLevelBuildTime = function() {
	var topLevelJSON = _getTopLevelJSON();

	return _getFormattedTime(topLevelJSON.build_duration);
}

getTopLevelLink = function() {
	var html = [];

	var topLevelJSON = _getTopLevelJSON();

	html.push("<a href=\"");
	html.push(topLevelJSON.build_url);
	html.push("\">");
	html.push(topLevelJSON.build_url);
	html.push("</a>");

	return html.join("");
}

getTopLevelStartTime = function() {
	var topLevelJSON = _getTopLevelJSON();

	var topLevelStartTime = new Date(topLevelJSON.start_time);

	return topLevelStartTime.toString("MMMM d, yyyy h:mm:ss tt");
}

getTotalCPUUsageTime = function() {
	var builds = build_database.builds;

	var totalCPUUsageTime = 0;

	for (var build in builds) {
		totalCPUUsageTime += builds[build].build_duration;
	}

	return _getFormattedTime(totalCPUUsageTime);
}

getTotalJenkinsSlavesUsed = function() {
	var totalJenkinsSlavesUsed = 0;

	for (var build in build_database.builds) {
		totalJenkinsSlavesUsed++;
	}

	return totalJenkinsSlavesUsed;
}

_getFormattedTime = function(ms) {
	var time = [];

	var days = Math.floor(ms / (1000 * 60 * 60 * 24));

	if (days > 0) {
		time.push(days + " days ");
	}

	var hours = Math.floor((ms / (1000 * 60 * 60)) % 24);

	if (hours > 0) {
		time.push(hours + " hours ");
	}

	var minutes = Math.floor((ms / (1000 * 60)) % 60);

	if (minutes > 0) {
		time.push(minutes + " minutes ");
	}

	var seconds = Math.floor((ms / 1000) % 60);

	if (seconds > 0) {
		time.push(seconds + " seconds ");
	}

	var milliseconds = Math.floor(ms % 1000);

	if (milliseconds > 0) {
		time.push(milliseconds + " ms");
	}

	return time.join("");
}

_getTopLevelJSON = function() {
	var builds = build_database.builds;

	for (var build in builds) {
		if (build.includes("top_level_")) {
			return builds[build];
		}
	}

	return null;
}