
//Import moment.js in Parse.Module
var moment = require('cloud/moment/moment.js');

Parse.Cloud.define("checkBookingStatus", function(request, response) {
  	query = new Parse.Query("Booking");
  	query.equalTo("objectId",request.params.id);
	query.first({
	  		success: function(booking) {
	  			if (booking.get('status') === 'Attended') {
					response.error("Booking already attended by other driver");
	  			} else {
					response.success(booking);
	  			}
		  	}, error: function(error) {
		  		response.error(error.message);
	  		}
	});
});

Parse.Cloud.afterSave("Booking", function(request) {
	console.log("latitude --> " + request.object.get('origin').latitude);

	if (request.object.get('status') === 'Pending') {
		/** parse push notification */
		Parse.Push.send({
	    	channels: ["Drivers"],
		    data: {	    	
		      alert: "New booking request from " + request.object.get('bookedBy'),
		      json : {latitude : request.object.get('origin').latitude,
		  				longitude : request.object.get('origin').longitude,
		  				from : request.object.get('from'),
		  				to : request.object.get('to'),
		  				bookingId : request.object.id,
		  				bookingStatus : request.object.get('status') }
		      }
		    }, { success: function() { 
		    	console.log("SUCCESSSSS!");
		    }, error: function(error) { 
		      console.log(error)
		    }
		});
	} else if (request.object.get('status') === 'Attended') {
		/* send push notification to driver */
		Parse.Push.send({
	    	channels: ["Drivers"],
		    data: {	    	
		      alert: "",
		      json : {latitude : request.object.get('origin').latitude,
		  				longitude : request.object.get('origin').longitude,
		  				from : request.object.get('from'),
		  				to : request.object.get('to'),
		  				bookingId : request.object.id,
		  				bookingStatus : request.object.get('status') }
		      }
		    }, { success: function() { 
		    	console.log("SUCCESSSSS!");
		    }, error: function(error) { 
		      console.log(error)
		    }
		});
		console.log("BOOKED BY --> " + request.object.get('user').id);
		Parse.Push.send({
	    	channels: ["C"+request.object.get('user').id],
		    data: {	    	
		      alert: "Your booking with id of " + request.object.id + " will be attended by " +
		      			request.object.get('driverName'),
		      json : {	driverId : request.object.get('driver').id,
		      			bookingId : request.object.id,
		  				bookingStatus : request.object.get('status') }
		      }
		    }, { success: function() { 
		    	console.log("SUCCESSSSS!");
		    }, error: function(error) { 
		      console.log(error)
		    }
		});

	}
});

Parse.Cloud.job("clearExpiredTransaction", function(request, status) {
 
  // Set up to modify user data
  Parse.Cloud.useMasterKey();
  // Query for all expired booking
  var query = new Parse.Query("Booking");
  var today = new Date();
  //query.notEqualTo("createdAt", {"__type": "Date","iso": dateOfManila().toISOString()});
  query.notEqualTo("status","Expired");
  query.each(function(booking) {
      // Set and save the change
      var diff = Math.abs(today.getTime() - booking.get('createdAt').getTime()) / 3600000;
      console.log("DIFFERENCE ---> " + diff);
	  if (diff > 24) { 
	  	/* do something */ 
  	      booking.set("status", "Expired");
          return booking.save();
	  } else {
	  	return null;
	  }
  }).then(function() {
    // Set the job's success status
    status.success("Booking status successfully changed to Expired!");
  }, function(error) {
    // Set the job's error status
    status.error("Uh oh, something went wrong --> " + error.message);
  });
});

// Returns moment Date today
function dateOfManila() {
  var date = moment()//
  //date.utcOffset("+08:00");
  return date
}

// Returns 12:00am date today
function getStartDate() {
  var date = dateOfManila()
  date.startOf('day');
  return date
}

// Returns 11:59:59:999 date today
function getEndDate() {
  var date = dateOfManila()
  date.endOf("day")
  return date
}