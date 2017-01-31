
var assert = require("assert");


var SomeObject = function (name) {
  "use strict";
   this.name = name;
};


someTest();



function  someTest() {

    "use strict";
    assert.equal("world", "world");
    console.log("I am happy with assert");

    var obgForTest = new SomeObject('test object');

    assert.equal(obgForTest.name, "test object");

    console.log("I am happy with strict mode");
}





