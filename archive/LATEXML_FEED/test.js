var fs = require('fs');
var xpath = require('xpath');
var parse5 = require('parse5');
var xmlser = require('xmlserializer');
var dom = require('xmldom').DOMParser;
var data = [];

var Curl = require('node-curl/lib/Curl')
var curl = new Curl();

curl.setopt('URL','http://latexml.mathweb.org/convert');
curl.setopt('CURLOPT_POST', true);
curl.setopt('CURLOPT_POSTFIELDS',data);
curl.setopt('CURLOPT_HEADER', false);
curl.setopt('CURLOPT_RETURNTRANSFER', true);

fs.readFile('./test.html', function (err, html) {

    if (err) throw err; 
    
    var document = parse5.parse(html.toString());
    var xhtml = xmlser.serializeToString(document);
    var doc = new dom().parseFromString(xhtml);
    
    var select1 = xpath.useNamespaces({"x": "http://www.w3.org/1998/Math/MathML"});
    var nodes = select1("//x:math//x:annotation", doc);
    var select2 = xpath.useNamespaces({"y": "http://www.w3.org/1998/Math/MathML"});
    var id = select2("//y:math/@id", doc);

    var i = 0;
    for(i = 0; i < nodes.length; i++){

    	data.push([id[i].value, nodes[i].firstChild.data]);

    	console.log(id[i].value);
    	console.log('\n');
    	console.log(nodes[i].firstChild.data);
    	console.log('\n');
    	console.log("---------------------------------------");
    	console.log('\n');
    }		

    console.log(data);
  
});
