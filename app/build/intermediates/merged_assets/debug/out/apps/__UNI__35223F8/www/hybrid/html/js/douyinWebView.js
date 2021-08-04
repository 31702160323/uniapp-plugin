let URL = document.URL;
console.log('注入成功');

//根据加载的URL来判断
if(URL.indexOf('https://open.douyin.com') != -1){
	//首次进入获取二维码
	let imageUrl = document.querySelector('.qr').src;
	uni.postMessage({
		data: {
			code: 0,
			imageUrl: imageUrl
		}
	});
} else if(URL.indexOf('http://dy.molibiantv.com') != -1) {
	let bodyHtml = document.body.innerText;
	console.log(bodyHtml);
	//用户授权成功回调
	uni.postMessage({
		data: {
			code: 3,
			data: bodyHtml
		}
	});
	window.location.href = "http://dy.molibiantv.com/v1/accountAuth/code";
}

/**
 * 监听二维码是否失效
 */
// 选择需要观察变动的节点
const targetNode = document.querySelector('.auth-qr-container');
// 观察器的配置（需要观察什么变动）
const config = { attributes: true, childList: true, subtree: true };
// 当观察到变动时执行的回调函数
const callback = function(mutationsList, observer) {
	let imageUrl = document.querySelector('.qr').src;
	if(!imageUrl) return;
	
	let refresh = document.querySelector('.btn-refresh');
	
	let code = refresh ? 1 : 0;
	uni.postMessage({
		data: {
			code: code,
			imageUrl: imageUrl
		}
	});
};

// 创建一个观察器实例并传入回调函数
const observer = new MutationObserver(callback);

// 以上述配置开始观察目标节点
observer.observe(targetNode, config);
/**
 * 监听二维码是否失效 END
 */

/**
 * 结束二维码监听回调
 */
function observerDisconnect() {
	// 之后，可停止观察
	observer.disconnect();
}

/**
 * 根据模拟点击事件，来进行uni点击事件的传递
 */
function btnRefresh() {
	let refresh = document.querySelector('.btn-refresh');
	
	triggerClick(refresh);
}

/**
 * 模拟点击元素事件的方法
 * @param {Object} el Dom节点对象
 */
function triggerClick(el) {
    if(el.click) {
        el.click();
    }else{
        try{
            var evt = document.createEvent('Event');
            evt.initEvent('click',true,true);
            el.dispatchEvent(evt);
        }catch(e){alert(e)};       
    }
}