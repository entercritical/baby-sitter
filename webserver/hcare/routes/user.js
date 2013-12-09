
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
	var paramname = request.param('name');
	var paramphone = request.param('phone');
	var parambirth = request.param('birth');
	var userInfo = {
		name : paramname,
		phone : paramphone,
		birth : parambirth,
		created : new Date()
		};

	if(paramname && paramphone && parambirth) {
		userclt.find({phone:paramphone}).count(function(err, count){
			if(count > 0)
			{
				console.log('user aleady exist match counter', count);
  				response.send("user aleady exist!");
			}
			else
			{
				console.log('add user');
				userclt.insert(userInfo)
				response.writeHead(200, {"Content-Type": "application/json"});
				response.write(JSON.stringify(userInfo));
  				response.end();
			}
		});
	}
	else
	{
  		response.send("need to add name + phone + birth");
	};
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
		userclt.findAndModify({
			query:{phone:phone},
			update:{$set:{name:name, phone:phone, birth:birth}},
			upsert: false}, function(err, doc){
				if(doc)
				{
					console.log('revise user', name, phone, birth);
					response.writeHead(200, {"Content-Type": "application/json"});
					response.write(JSON.stringify(userInfo));
  					response.end();
				}
				else
				{
					console.log('user dose not exist!', name, phone, birth);
  					response.send("user dose not exist!");
				}
			});
	}
	else{
  		response.send("need to add name + phone + birth");
	}
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

