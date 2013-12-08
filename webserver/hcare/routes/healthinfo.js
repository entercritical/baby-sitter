
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
	if(id) {
		//db.baby.insert({"name":name, "temp":[temp], "humidity":[humidity], "pulse":[pulse]})
		healthclt.insert(healthInfo)
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(healthInfo));
  		response.end();
	}
	console.log('add health info', id);
};

//query specific user info
exports.list = function (request, response){
	var agent = request.header('User-Agent');
//	var id = request.param('id');
	var operation = request.param('operation');

	var start = new Date();
	start.setDate(start.getDate()-operation);

	//var criteria  = {user_id:id, created:{$gt:start}};
	var criteria  = {created:{$gt:start}};
	var projection = {_id:0};
	

	//console.log('id=[%s] op=[%d] operation=[%s] dats[%s]', id, operation, operation, start);
	console.log('operation=[%s] date[%s]', operation, start);
	console.log(request.headers);
	console.log(agent);	
/*
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
	}else{
*/
		if(operation){
			criteria  = {created:{$gt:start}};
			console.log('id undefined, op defined\n');
		}
		else{
			criteria = {};
			console.log('id undefined, op undefined\n');
		}
//	}	

	//healthclt.find(criteria, projection, function (error, data){
	healthclt.find(criteria, projection).sort({timestamp:1}, function (error, data){
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
};

/*
exports.list = function(req, res){
  res.send("respond with a resource");
};
*/
