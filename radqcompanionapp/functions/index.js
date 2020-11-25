const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendContactNotification = functions.database.ref('/notifications/{contactId}/{notificationId}')
    .onCreate(async (change, context) => {
		const contactId = context.params.contactId;
		const notificationId = context.params.notificationId;
	
		const getNotificationTypePromise = admin.database().ref(`/notifications/${contactId}/${notificationId}/notification`).once('value');
		return getNotificationTypePromise.then(resultNotificationType =>{
			var notificationType = resultNotificationType.val();
			var notificationBody = 'radq_notification_error_body';
			var androidChannelId = 'default';
			
			if (notificationType == 'aW1va2F5YnV0dG9ucHJlc3NlZA'){
				console.log("[LOG] OK button pressed");
				notificationBody = 'aW1va2F5YnV0dG9ucHJlc3NlZA';		
				androidChannelId = 'channel_fall_detected_okay'
				
			} else if (notificationType == 'aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA'){
				console.log("[LOG] HELP button pressed");
				notificationBody = 'aW1ub3Rva2F5YnV0dG9ucHJlc3NlZA';		
				androidChannelId = 'channel_fall_detected_not_okay'
				
			} else if (notificationType == 'YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg'){	
				console.log("[LOG] NO buttons pressed");
				notificationBody = 'YnV0dG9ubm90cHJlc3NlZHRpbWVzb3Zlcg';		
				androidChannelId = 'channel_fall_detected_no_buttons_pressed'
				
			} else if (notificationType == 'b3ZlcnJpZGUgb3IgYmF0dGVyeSBsb3cu'){		
				console.log("[LOG] detection stopped");
				notificationBody = 'b3ZlcnJpZGUgb3IgYmF0dGVyeSBsb3cu';		
				androidChannelId = 'channel_detection_stopped'
				
			} else if (notificationType == 'c3RhcnRpbmdmYWxsZGV0ZWN0aW9u'){		
				console.log("[LOG] detection started");
				notificationBody = 'c3RhcnRpbmdmYWxsZGV0ZWN0aW9u';					
				androidChannelId = 'channel_detection_started'
			}
			
			const getDeviceTokensPromise = admin.database().ref(`/accounts/${contactId}/phoneKey`).once('value');
			return getDeviceTokensPromise.then(result => {
				var token = result.val();
			
				var payload = {
					notification: {
						title_loc_key:"radq_notification",
						body_loc_key: notificationBody,
						icon:"notification",
						clickAction:"android.intent.action.open_notifications",
						android_channel_id:androidChannelId
					}
				};
				
				return admin.messaging().sendToDevice(token, payload)
					.then((response)=> {
						console.log("[LOG] Successfully sent message: ", response);
					})
					.catch((error) =>{
						console.log("[LOG] Error sending message: ", error);
					});
			});			
		});
    });