
var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

var safesmsExport = {};

/*
 * Methods
 */

/*
 * set options:
 *  {
 *    position: integer, // default position
 *    x: integer,	// default X of banner
 *    y: integer,	// default Y of banner
 *    isTesting: boolean,	// if set to true, to receive test ads
 *    autoShow: boolean,	// if set to true, no need call showBanner or showInterstitial
 *   }
 */
safesmsExport.setOptions = function(options, successCallback, failureCallback) {
	  if(typeof options === 'object') {
		  cordova.exec( successCallback, failureCallback, 'SmsUtil', 'setOptions', [options] );
	  } else {
		  if(typeof failureCallback === 'function') {
			  failureCallback('options should be specified.');
		  }
	  }
	};

safesmsExport.startWatch = function(successCallback, failureCallback) {
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'startWatch', [] );
};

safesmsExport.stopWatch = function(successCallback, failureCallback) {
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'stopWatch', [] );
};

safesmsExport.enableIntercept = function(on_off, successCallback, failureCallback) {
	on_off = !! on_off;
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'enableIntercept', [ on_off ] );
};

safesmsExport.sendSMS = function(address, text, successCallback, failureCallback) {
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
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMSOnSim1', [ numbers, text ] );
};

safesmsExport.sendSMSOnSim1 = function(address, text, successCallback, failureCallback) {
	if(typeof address !== 'string'){

	}else if(typeof text !== 'string'){

	}else if(typeof failureCallback === 'function') {
		failureCallback("require text message, address or phone number as string");
		return;
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMSOnSim1', [ numbers, text ] );
};

safesmsExport.sendSMSOnSim2 = function(address, text, successCallback, failureCallback) {
	if(typeof address !== 'string'){

	}else if(typeof text !== 'string'){

	}else if(typeof failureCallback === 'function') {
		failureCallback("require text message, address or phone number as string");
		return;
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMSOnSim1', [ numbers, text ] );
};

safesmsExport.listSMS = function(filter, successCallback, failureCallback) {
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'listSMS', [ filter ] );
};

safesmsExport.deleteSMS = function(filter, successCallback, failureCallback) {
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'deleteSMS', [ filter ] );
};

safesmsExport.restoreSMS = function(msg, successCallback, failureCallback) {
	var smsList = [];
	if(Array.isArray(msg)) {
		if(msg.length > 0) smsList = msg;
	} else if(typeof msg === 'object') {
		if(msg !== null) smsList = [ msg ];
	}
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'restoreSMS', [ msg ] );
};

/*
 * Events:
 * 
 * document.addEventListener('onSMSArrive', function(e) { var sms = e.data; }
 * 
 */

module.exports = safesmsExport;

