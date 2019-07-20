
var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var safesmsExport = {};

/*
 * Methods
 */

safesmsExport.sendSMS = function( simId, address, text, successCallback, failureCallback) {
	var numbers;
	if( Object.prototype.toString.call( address ) === '[object Array]' ) {
		numbers = address;
	} else if(typeof address === 'string') {
		numbers = [ address ];
	} else {
		if(typeof failureCallback === 'function') {
			failureCallback("require address, phone number as string, or array of string");
		}
		return;
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMS', [ simId, numbers, text ] );
};

module.exports = safesmsExport;

