
/*
 * GET users listing.
 */

// mongo db & collection
var mongojs = require('mongojs');
var db = mongojs('hcare');
var userclt = db.collection('user');
var healthclt = db.collection('healthinfo');

//add health infomation
exports.add = function (request, response){
	var id = request.param('id');
	var heat = request.param('heat');
	var wet = request.param('wet');
	var bpm = request.param('bpm');
	var mic = request.param('mic');
	var healthInfo = {
		user_id : id,
		heat : heat,
		wet : wet,
		bpm : bpm,
		mic : mic,
		created : new Date()};
	//TO DO: need to check registerd user
	userclt.find({phone:id},{_id:0}).count( function(error, count){
		console.log('check user', count, error);
		if(count > 0) {
			console.log('add health info', id);
			healthclt.insert(healthInfo)
			response.writeHead(200, {"Content-Type": "application/json"});
			response.write(JSON.stringify(healthInfo));
  			response.end();
		}
		else
		{
  			response.send("need to join user");
		}
	});
};

//query specific user info
exports.list = function (request, response){
	//var agent = request.header('User-Agent');
	var id = request.param('id');
	var operation = request.param('operation');

	var start = new Date();
	start.setDate(start.getDate()-operation);

	var criteria  = {user_id:id, created:{$gt:start}};
	//var criteria  = {created:{$gt:start}};
	var projection = {_id:0};
	

	//console.log('id=[%s] op=[%d] operation=[%s] dats[%s]', id, operation, operation, start);
	console.log('operation=[%s] date[%s]', operation, start);
	console.log(request.headers);
	//console.log(agent);	

	if(id){
		if(typeof operation == "undefined"){
			console.log('id defined, op undefined\n');
			criteria  = {user_id:id};
		}
		else
		{
			criteria  = {user_id:id, created:{$gt:start}};
			console.log('id defined, op defined\n');
		}
	}
	else
	{
		if(operation)
		{
			criteria  = {created:{$gt:start}};
			console.log('id undefined, op defined\n');
		}
		else{
			criteria = {};
			console.log('id undefined, op undefined\n');
		}
	}	

	healthclt.find(criteria, projection).sort({timestamp:1}, function (error, data){
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
};

exports.avg = function (request, response){
	var agent = request.header('User-Agent');
	var id = request.param('id');
	var operation = request.param('operation');

	var start = new Date();
	start.setDate(start.getDate()-operation);

	var criteria  = {created:{$gt:start}};
	var projection = {_id:0};
	
	console.log('operation=[%s] date[%s]', operation, start);
		if(operation){
			criteria  = {created:{$gt:start}};
			console.log('op defined\n');
		}
		else{
			criteria = {};
			console.log('op undefined\n');
		}
	
	healthclt.aggregate(
		{$match: criteria},
		{$group: {_id: '$user_id',
					heatavg: {$avg: '$heat'},
					wetavg: {$avg: '$wet'},
					bpnavg: {$avg: '$bpm'},

					heatmin: {$min: '$heat'},
					wetmin: {$min: '$wet'},
					bpnmin: {$min: '$bpm'},

					heatmax: {$max: '$heat'},
					wetmax: {$max: '$wet'},
					bpnmax: {$max: '$bpm'}

		}}, 
		function (error, data){
			var sumheat = 0, sumwet = 0, avgheat = 0, avgwet = 0;
			var size = data.length;
			var statisticInfo = {
				myAvgHeat : 0,
				myMinHeat : 0,
				myMaxHeat : 0,
				avgHeat : 0,
				minHeat : 1000,
				maxHeat : 0
				};

			//console.log(error, data);
			console.log("size=\n", size);
			for(var i = 0; i < size; i++)
			{
				console.log("myid=", id);
				console.log("dataid=", i, data[i]._id);
				sumheat = sumheat + data[i].heatavg;
				sumwet = sumwet + data[i].wetavg;
				
				if(data[i].heatmin < statisticInfo.minHeat)
					statisticInfo.minHeat = data[i].heatmin;

				if(data[i].heatmax > statisticInfo.maxHeat)
					statisticInfo.maxHeat = data[i].heatmax;

				if(data[i]._id === id)
				{
					statisticInfo.myAvgHeat = data[i].heatavg;
					statisticInfo.myMinHeat = data[i].heatmin;
					statisticInfo.myMaxHeat = data[i].heatmax;

					console.log("my avg=", statisticInfo.myAvgHeat);
				}
			}
			avgheat = sumheat / size;
			avgwet = sumwet / size;
	
			statisticInfo.avgHeat = avgheat;
			
			console.log("avg=", avgheat, avgwet);
			response.writeHead(200, {"Content-Type": "application/json"});
			response.write(JSON.stringify(statisticInfo));
			//response.write(JSON.stringify(data));
  			response.end();
		});
};

