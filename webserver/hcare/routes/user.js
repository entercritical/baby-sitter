
/*
 * GET users listing.
 */

// mongo db & collection
var mongojs = require('mongojs');
var db = mongojs('hcare');
var userclt = db.collection('user');
var healthclt = db.collection('healthinfo');

//add user
exports.add = function(request, response){
	var name = request.param('name');
	var phone = request.param('phone');
	var birth = request.param('birth');

	console.log('add user', name, phone, birth);
	
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
	}
	else{
  		res.send("name + phone + birth", name, phone, birth);
	}
};

//get user info
exports.list = function (request, response){
	var query = {};
	var id = request.param('id');
	if(id)
		query = {phone:id};

	userclt.find(query, function (error, data){
	//	console.log('get specific user', data);
		response.writeHead(200, {"Content-Type": "application/json"});
		response.write(JSON.stringify(data));
  		response.end();
		});
};

//modify user info
exports.revise = function (request, response){
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
};

//delete user
exports.del = function (request, response){
	var id = request.param('id');
	userclt.remove({phone:id})
	console.log('del user', id);
};

/*
exports.list = function(req, res){
  res.send("respond with a resource");
};
*/

