
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


/*
 * Events:
 * 
 * document.addEventListener('onSMSArrive', function(e) { var sms = e.data; }
 * 
 */

module.exports = safesmsExport;

