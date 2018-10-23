init_table = function() {
	var buildTable = document.getElementById("buildTable");

	var table = buildTable.appendChild(document.createElement("table"));

	table.setAttribute("border", 1);

	table.appendChild(getTableCaption());

	table.appendChild(getTableHeader());

	table.appendChild(getTableBody());

	table.appendChild(getTableCaptionBottom());
}

getTableBody = function() {
	var tableBody = document.createElement("tbody");

	var portalBranchJSON = _getPortalBranchJSON();
	var topLevelJSON = _getTopLevelJSON();

	var commits = portalBranchJSON.commits;

	for (var commit in commits) {
		var commitJSON = commits[commit];

		var downstreamBuildJSON = _getDownstreamBuildJSONBySHA(topLevelJSON, commitJSON.sha);

		tableBody.appendChild(_getTableRow(commitJSON, downstreamBuildJSON));
	}

	var firstRow = tableBody.firstChild;

	var firstCell = firstRow.firstChild;

	firstCell.innerHTML = "*" + firstCell.innerHTML

	return tableBody;
}

getTableCaption = function() {
	var tableCaption = document.createElement("caption");

	var tableCaptionContent = tableCaption.appendChild(document.createElement("h1"));

	var portalBranchJSON = _getPortalBranchJSON();

	var html = [];

	html.push("Commit history of <a href=\"");

	var gitHubCommitURL = portalBranchJSON.git_hub_url.replace("/tree/", "/commits/");

	html.push(gitHubCommitURL);
	html.push("\"/>");
	html.push(gitHubCommitURL);
	html.push("</a>");

	tableCaptionContent.innerHTML = html.join("");

	return tableCaption;
}

getTableCaptionBottom = function() {
	var tableCaption = document.createElement("caption");

	tableCaption.setAttribute("class", "bottom-table-caption")

	var tableCaptionContent = tableCaption.appendChild(document.createElement("em"));

	tableCaptionContent.innerHTML = "<br />Indicates HEAD Commit (*)";

	return tableCaption;
}

getTableHeader = function() {
	var tableHeader = document.createElement("thead");

	var tableHeaderRow = tableHeader.appendChild(document.createElement("tr"));

	var tableHeaderCell1 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell2 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell3 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell4 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell5 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell6 = tableHeaderRow.appendChild(document.createElement("th"));
	var tableHeaderCell7 = tableHeaderRow.appendChild(document.createElement("th"));

	tableHeaderCell1.innerHTML = "Commit";
	tableHeaderCell2.innerHTML = "SHA";
	tableHeaderCell3.innerHTML = "Build";
	tableHeaderCell4.innerHTML = "Start Time";
	tableHeaderCell5.innerHTML = "Build Time";
	tableHeaderCell6.innerHTML = "Status";
	tableHeaderCell7.innerHTML = "Result";

	return tableHeader;
}

_getBuildResult = function(buildJSON) {
	var result = buildJSON.build_result;

	if (result == null) {
		return "";
	}

	return result;
}

_getBuilds = function() {
	return build_database.builds;
}

_getDownstreamBuildJSONBySHA = function(topLevelJSON, sha) {
	var downstreamRunIDs = topLevelJSON.downstream_run_ids.split(",");
	var builds = _getBuilds();

	for (var downstreamRunID in downstreamRunIDs) {
		var runID = downstreamRunIDs[downstreamRunID];

		if (runID == "") {
			continue;
		}

		var buildJSON = builds[runID];

		var portalBranchSHA = buildJSON.portal_branch_sha

		if (portalBranchSHA == sha) {
			return buildJSON;
		}
	}

	return null;
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

_getPortalBranchJSON = function() {
	var workspaceGitRepositories = build_database.workspace_git_repositories;

	return workspaceGitRepositories["portal"];
}

_getStartTime = function(buildJSON) {
	var date = new Date(buildJSON.start_time);

	return date.toString("MMMM d, yyyy h:mm:ss tt");
}

_getTableRow = function(commitJSON, buildJSON) {
	var tableRow = document.createElement("tr");

	var tableCell1 = tableRow.appendChild(document.createElement("td"));
	var tableCell2 = tableRow.appendChild(document.createElement("td"));
	var tableCell3 = tableRow.appendChild(document.createElement("td"));
	var tableCell4 = tableRow.appendChild(document.createElement("td"));
	var tableCell5 = tableRow.appendChild(document.createElement("td"));
	var tableCell6 = tableRow.appendChild(document.createElement("td"));
	var tableCell7 = tableRow.appendChild(document.createElement("td"));

	tableCell1.innerHTML = commitJSON.message;
	tableCell2.innerHTML = commitJSON.sha;

	if (buildJSON == null) {
		tableCell3.innerHTML = "";
		tableCell4.innerHTML = "";
		tableCell5.innerHTML = "";
		tableCell6.innerHTML = "";
		tableCell7.innerHTML = "";

		return tableRow;
	}

	tableCell3.innerHTML = "<a href=\"" + buildJSON.build_url + "\">build</a>";
	tableCell4.innerHTML = _getStartTime(buildJSON);
	tableCell5.innerHTML = _getFormattedTime(buildJSON.build_duration);
	tableCell6.innerHTML = buildJSON.build_status;
	tableCell7.innerHTML = _getBuildResult(buildJSON);

	return tableRow;
}

_getTopLevelJSON = function() {
	var builds = _getBuilds();

	for (var build in builds) {
		if (build.includes("top_level_")) {
			return builds[build];
		}
	}

	return null;
}