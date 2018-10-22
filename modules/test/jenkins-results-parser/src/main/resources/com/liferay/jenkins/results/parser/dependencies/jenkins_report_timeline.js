init_timeline = function() {
	var timelineData = new TimelineData(500);

	var canvas = document.getElementById("buildTimeline");

	var xData =  timelineData.timePeriods;
	var y1Data = timelineData.slaveUsageDataPoints;
	var y2Data = timelineData.invocationDataPoints;

	var timeline = new Chart(canvas, {
		data: {
			labels: xData,
			datasets: [
				{
					backgroundColor: 'rgba(255, 99, 132, 0.3)',
					borderWidth: 0,
					data: y1Data,
					label: 'Jenkins Slaves in Use'
				},
				{
					backgroundColor: 'rgba(54, 162, 235, 1)',
					borderWidth: 0,
					data: y2Data,
					label: 'Build Invocations'
				}
			]
		},
		options: {
			elements: {
				point: {
					hitRadius: 10,
					hoverRadius: 4,
					radius: 0
				}
			},
			maintainAspectRatio: false,
			responsive: true,
			scales: {
				xAxes: [
					{
						scaleLabel: {
							display: true,
							labelString: 'Elapsed Time (hh:mm:ss)'
						},
						ticks: {
							autoSkipPadding: 50,
							callback: function(value) {
								var time = new Date(value);

								var hours = time.getUTCHours();

								hours = hours.toString();

								var minutes = time.getUTCMinutes();

								minutes = minutes.toString();

								var seconds = time.getUTCSeconds();

								seconds = seconds.toString();

								if (hours.length == 1) {
									 hours = '0' + hours;
								}

								if (minutes.length == 1) {
									 minutes = '0' + minutes;
								}

								if (seconds.length == 1) {
									 seconds = '0' + seconds;
								}

								return hours + ':' + minutes + ':' + seconds;
							 }
						}
					}
				],
				yAxes: [
					{
						ticks: {
							beginAtZero: true
						}
					}
				]
			}
		},
		type: 'line'
	});
}

function TimelineData(size) {

	var getBuilds = function() {
		return build_database.builds;
	}

	var getInvocationCount = function(timePeriodStart) {
		var invocationCount = 0;

		var builds = getBuilds();
		var topLevelStartTime = getTopLevelStartTime();
		var timePeriodDuration = getTimePeriodDuration();

		for (build in builds) {
			var invocationTime = builds[build].invocation_time - topLevelStartTime;

			var timePeriodEnd = timePeriodStart + timePeriodDuration;

			if ((invocationTime >= timePeriodStart) && (invocationTime < timePeriodEnd)) {
				invocationCount++;
			}
		}

		return invocationCount;
	}

	var getSlaveUsageCount = function(timePeriodStart) {
		var slaveUsageCount = 0;

		var builds = getBuilds();
		var topLevelStartTime = getTopLevelStartTime();

		for (build in builds) {
			var buildJSON = builds[build];

			var relativeStartTime = buildJSON.start_time - topLevelStartTime;

			var relativeEndTime = relativeStartTime + buildJSON.build_duration;

			if ((timePeriodStart >= relativeStartTime) && (timePeriodStart <= relativeEndTime)) {
				slaveUsageCount++;
			}
		}

		return slaveUsageCount;
	}

	var getTimePeriodDuration = function() {
		var topLevelBuild = getTopLevelJSON();

		var topLevelDuration = topLevelBuild.build_duration;

		return Math.floor(topLevelDuration / (size - 1));
	}

	var getTopLevelJSON = function() {
		var builds = getBuilds();

		for (var build in builds) {
			if (build.includes("top_level_")) {
				return builds[build];
			}
		}

		return null;
	}

	var getTopLevelStartTime = function() {
		var topLevelBuild = getTopLevelJSON();

		return topLevelBuild.start_time;
	}

	this.size = size;

	if (this.size == null) {
		throw new Error("Please set size");
	}

	if (this.size < 1) {
		throw new Error("Invalid size " + this.size);
	}

	this.timePeriods = [];
	this.slaveUsageDataPoints = [];
	this.invocationDataPoints = [];

	var timePeriodDuration = getTimePeriodDuration();

	for (var i = 0; i < this.size; i++) {
		var timePeriodStart = timePeriodDuration * i;

		this.timePeriods.push(timePeriodStart);

		this.invocationDataPoints.push(getInvocationCount(timePeriodStart));
		this.slaveUsageDataPoints.push(getSlaveUsageCount(timePeriodStart));
	}

}