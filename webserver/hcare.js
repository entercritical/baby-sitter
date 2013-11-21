var http = require('http');
var express = require('express');
var app = express();

//var db = require('mongojs').connect('hcare', ['user']);
var mongojs = require('mongojs');
var db = mongojs('hcare');
var userclt = db.collection('user');
var healthclt = db.collection('healthinfo');
//var userCollection = db.collection('user');

app.use(express.bodyParser());
app.use(app.router);

//query user info
app.get('/user/:id?', function (request, response){
	var query = {};
	var id = request.param('id');
	if(id)
		query = {phone:id};

	userclt.find(query, function (error, data){
		console.log('get specific user', data);
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
});

//add user
app.post('/user', function (request, response){
	var name = request.param('name');
	var phone = request.param('phone');
	var birth = request.param('birth');

	var userInfo = {
		name : name,
		phone : phone,
		birth : birth,
		created : new Date()
		};
	//TO DO : need to check aleady registed user
	if(name && phone && birth) {
		userclt.insert(userInfo)
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(userInfo));
  		response.end();
		console.log('add user', name);
	}
	else{
		console.log('invalid param add user', name);
	}
});

//add health infomation
app.post('/healthinfo/:id', function (request, response){
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
		created : Date()};
	//TO DO: need to check registerd user
	if(id) {
		//db.baby.insert({"name":name, "temp":[temp], "humidity":[humidity], "pulse":[pulse]})
		healthclt.insert(healthInfo)
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(healthInfo));
  		response.end();
	}
	console.log('add health info', id);
});

//modify user info
app.put('/user/:id', function (request, response){
	var id = request.param('id');
	var name = request.param('name');
	var temp = request.param('temp');
	var humidity = request.param('humidity');
	var pulse = request.param('pulse');

	if(name) {
		healthclt.update(
						{"name": name,},
						{$push: {"temp":temp}}, 
						{$push: {"humidity":humidity}}, 
						{$push: {"pulse":pulse}})
	}

	console.log('modify user', id);
	//reponse.send(id);
});

app.del('/user/:id', function (request, response){
	var id = request.param('id');
	userclt.remove({phone:id})
	console.log('del user', id);
});

//query specific user info
/*
app.get('/healthinfo/:id?', function (request, response){
	var query = {};
	var id = request.param('id');
	
	if(id) query = {user_id:id};
	healthclt.find(query, function (error, data){
		console.log('get %s health info', id, data);
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
});
*/
//query specific user info
app.get('/healthinfo/:id?/:operation?', function (request, response){
	var id = request.param('id');
	var operation = request.param('operation');

	var start = new Date();
	start.setDate(start.getDate()-operation);

	var criteria  = {user_id:id, created:{$gt:start}};
	var projection = {_id:0};
	

	console.log('id=[%s] op=[%d] operation=[%s] dats[%s]', id, operation, operation, start);
	
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
		if(operation){
			criteria  = {created:{$gt:start}};
			console.log('id undefined, op defined\n');
		}
		else{
			criteria = {};
			console.log('id undefined, op undefined\n');
		}
	}	

	healthclt.find(criteria, projection, function (error, data){
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
});


http.createServer(app).listen(52273, function(){	
	console.log('server running at 14.49.42.181:52273');
});


