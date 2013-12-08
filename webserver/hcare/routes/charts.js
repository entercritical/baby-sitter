
/*
 * GET home page.
 */
var fs = require('fs');
var jade = require('jade');

// mongo db & collection
var mongojs = require('mongojs');
var db = mongojs('hcare');
var userclt = db.collection('user');
var healthclt = db.collection('healthinfo');
var guideclt = db.collection('guide');


exports.list = function(request, response){
	var id = request.param('id');
	var operation = request.param('operation');

	var start = new Date();
	start.setDate(start.getDate()-operation);

//	var criteria  = {user_id:id, created:{$gt:start}};
	var criteria  = {};
	var projection = {timestamp:1, heat:1, _id:0};
	

	console.log('id=[%s] op=[%d] operation=[%s] dats[%s]', id, operation, operation, start);
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
		if(operation){
			criteria  = {created:{$gt:start}};
			console.log('id undefined, op defined\n');
		}
		else{
			criteria = {};
			console.log('id undefined, op undefined\n');
		}
	}	
*/
	healthclt.find(criteria, projection, function (error, data){
		console.log('health info find\n', error, data);
		response.render('chart.jade', {dbdata:data, test:'chart test'});
//		response.writeHead(200, {"Content-Type": "application/json"});
//		response.write(JSON.stringify(data));
//  		response.end();
		});
	
/*
	fs.readFile(__dirname + '/test.html', function (error, data) {
		res.writeHead(200, {'Content-Type':'text/html'});
		res.end(data);
		console.log(error, data);
	});
*/
};
