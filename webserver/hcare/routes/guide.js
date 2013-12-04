
/*
 * GET users listing.
 */

var fs = require('fs');

//query specific user info
exports.list = function (request, response){

	var directory = request.param('id');
	var filename = request.param('operation');
	var path = '';

	if(directory){
		if(filename){
			path = './public/'+directory+'/'+filename+'.txt';
		}
	}
	console.log(path);
	fs.readFile (path, 'utf-8', function (error, data){
		console.log(error, data);
		response.writeHead(200, {"Content-Type": "text/plain"});
		response.write(JSON.stringify(data));
 		response.end();
	});
};

/*
exports.list = function(req, res){
  res.send("respond with a resource");
};
*/
