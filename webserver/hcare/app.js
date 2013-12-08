
/**
 * Module dependencies.
 */

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var healthinfo = require('./routes/healthinfo');
var charts = require('./routes/charts');
var http = require('http');
var path = require('path');

var app = express();

// mongo db & collection
var mongojs = require('mongojs');
var db = mongojs('hcare');
var userclt = db.collection('user');
var healthclt = db.collection('healthinfo');


// all environments
app.set('port', 52273);
app.set('views', path.join(__dirname, 'views'));
app.set('public', path.join(__dirname, 'public'));
app.set('view engine', 'jade');

app.use(express.favicon());
app.use(express.logger('default'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(express.bodyParser());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', routes.index);
app.post('/user', user.add);
app.get('/user/:id?', user.list);
app.put('/user/:id', user.revise);
app.del('/user/:id', user.del);

//add health infomation
app.post('/healthinfo/:id', healthinfo.add);

//query user health infomation
//app.get('/healthinfo/:id?/:operation?', healthinfo.list);
app.get('/healthinfo/:operation?', healthinfo.list);

//chart home page
app.get('/charts/:id?/:operation?', charts.list);

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});

/*
app.get('/add', function(req, res){
	p1 = parseint(req.query.p1);
	p2 = parseint(req.query.p2);
	ret = p1 + p2;
	res.setHeader('Content-Type', 'text/plain');
	res.send(String(ret));
});

app.get('/a', function (request, response) {
	response.writeHead(200, {'Content-Type': 'text/html'});
	response.end('<img src="/test.jpg" width="100%"/>');
});
*/


