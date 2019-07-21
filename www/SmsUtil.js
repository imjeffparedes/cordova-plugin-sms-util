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

safesmsExport.sendSMS = function(simId, address, text, successCallback, failureCallback) {
	var error = null;
	if(typeof address !== 'string'){
		error = "require one address as string";
	}else if(typeof text !== 'string'){
		error = "require text message as string";
	}else if(typeof simId !== 'number'){
		error = "require simId as number - could be 0 or 1";
	}

	if(error && typeof failureCallback === 'function'){
		return failureCallback(error);
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMS', [ simId, address, text ] );
};

safesmsExport.sendSMSOnSim1 = function(address, text, successCallback, failureCallback) {
	var error = null;
	if(typeof address !== 'string'){
		error = "require one address as string";
	}else if(typeof text !== 'string'){
		error = "require text message as string";
	}
	if(error && typeof failureCallback === 'function'){
		return failureCallback(error);
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMSOnSim1', [ address, text ] );
};

safesmsExport.sendSMSOnSim2 = function(address, text, successCallback, failureCallback) {
	var error = null;
	if(typeof address !== 'string'){
		error = "require one address as string";
	}else if(typeof text !== 'string'){
		error = "require text message as string";
	}
	if(error && typeof failureCallback === 'function'){
		return failureCallback(error);
	}
	
	cordova.exec( successCallback, failureCallback, 'SmsUtil', 'sendSMSOnSim2', [ address, text ] );
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

