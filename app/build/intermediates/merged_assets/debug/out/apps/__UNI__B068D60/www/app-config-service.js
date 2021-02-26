
var isReady=false;var onReadyCallbacks=[];
var isServiceReady=false;var onServiceReadyCallbacks=[];
var __uniConfig = {"pages":["pages/index/index","pages/login/login","pages/search/search","pages/play/play","pages/playlist/playlist","pages/webView/webView","pages/playlist/description","pages/song/song","pages/playlist/comment","pages/play/recentlyPlayed","pages/playlist/square","pages/toplist/toplist"],"window":{"navigationBarTextStyle":"white","navigationBarTitleText":"uni-app","navigationBarBackgroundColor":"#007AFF","backgroundColor":"#FFFFFF","softinputNavBar":"none"},"nvueCompiler":"uni-app","renderer":"native","splashscreen":{"alwaysShowBeforeRender":true,"autoclose":false},"appname":"musiciUniApp","compilerVersion":"3.0.5","entryPagePath":"pages/index/index","networkTimeout":{"request":60000,"connectSocket":60000,"uploadFile":60000,"downloadFile":60000}};
var __uniRoutes = [{"path":"/pages/index/index","meta":{"isQuit":true,"isNVue":true},"window":{"navigationStyle":"custom","navigationBarTitleText":"首页"}},{"path":"/pages/login/login","meta":{"isNVue":true},"window":{"navigationStyle":"custom","navigationBarTitleText":"登录页"}},{"path":"/pages/search/search","meta":{"isNVue":true},"window":{"navigationStyle":"custom","navigationBarTitleText":"搜索页","backgroundColor":"rgba(255, 255, 255,1)","animationType":"slide-in-bottom","animationDuration":250,"subNVues":[{"id":"search-nav-bar","path":"subNVue/search-nav-bar","style":{"position":"dock","dock":"top","height":"130rpx"}}]}},{"path":"/pages/play/play","meta":{"isNVue":true},"window":{"titleNView":{"titleAlign":"left","titleColor":"#FFFFFF","titleText":"加载中...","backgroundColor":"#171616","buttons":[{"fontSrc":"/static/font/iconfont.ttf","text":"","width":"40px","fontSize":"24px","color":"#FFFFFF","background":"rgba(0,0,0,0)"}]}}},{"path":"/pages/playlist/playlist","meta":{"isNVue":true},"window":{"titleNView":{"titleColor":"#FFFFFF","titleText":"歌单","backgroundColor":"rgba(218, 45, 30, 1)","buttons":[{"fontSrc":"/static/font/iconfont.ttf","text":"","width":"40px","fontSize":"24px","color":"#FFFFFF","background":"rgba(0,0,0,0)"},{"fontSrc":"/static/font/iconfont.ttf","text":"","width":"40px","fontSize":"24px","color":"#FFFFFF","background":"rgba(0,0,0,0)"}]}}},{"path":"/pages/webView/webView","meta":{"isNVue":true},"window":{"navigationBarTitleText":"","enablePullDownRefresh":false,"navigationBarBackgroundColor":"#FFFFFF","navigationBarTextStyle":"black"}},{"path":"/pages/playlist/description","meta":{"isNVue":true},"window":{"navigationBarTitleText":"","enablePullDownRefresh":false,"navigationStyle":"custom"}},{"path":"/pages/song/song","meta":{"isNVue":true},"window":{"navigationBarTitleText":"本地音乐","enablePullDownRefresh":false,"navigationBarBackgroundColor":"#FFFFFF","navigationBarTextStyle":"black"}},{"path":"/pages/playlist/comment","meta":{"isNVue":true},"window":{"navigationBarTitleText":"评论页","enablePullDownRefresh":false,"navigationBarBackgroundColor":"#FFFFFF","navigationBarTextStyle":"black"}},{"path":"/pages/play/recentlyPlayed","meta":{"isNVue":true},"window":{"navigationBarBackgroundColor":"#FFFFFF","navigationBarTextStyle":"black","titleNView":{"titleAlign":"left","titleColor":"#000000","titleText":"最近播放","backgroundColor":"#FFFFFF","buttons":[{"text":"清空","fontSize":"32rpx","color":"#000000","background":"rgba(0,0,0,0)"}]}}},{"path":"/pages/playlist/square","meta":{"isNVue":true},"window":{"navigationBarTitleText":"歌单广场","enablePullDownRefresh":false,"navigationBarBackgroundColor":"#FFFFFF","navigationBarTextStyle":"black"}},{"path":"/pages/toplist/toplist","meta":{"isNVue":true},"window":{"navigationBarTextStyle":"black","titleNView":{"titleAlign":"left","titleColor":"#171616","titleText":"排行榜","backgroundColor":"#FFFFFF"}}}];
__uniConfig.onReady=function(callback){if(__uniConfig.ready){callback()}else{onReadyCallbacks.push(callback)}};Object.defineProperty(__uniConfig,"ready",{get:function(){return isReady},set:function(val){isReady=val;if(!isReady){return}const callbacks=onReadyCallbacks.slice(0);onReadyCallbacks.length=0;callbacks.forEach(function(callback){callback()})}});
__uniConfig.onServiceReady=function(callback){if(__uniConfig.serviceReady){callback()}else{onServiceReadyCallbacks.push(callback)}};Object.defineProperty(__uniConfig,"serviceReady",{get:function(){return isServiceReady},set:function(val){isServiceReady=val;if(!isServiceReady){return}const callbacks=onServiceReadyCallbacks.slice(0);onServiceReadyCallbacks.length=0;callbacks.forEach(function(callback){callback()})}});
service.register("uni-app-config",{create(a,b,c){if(!__uniConfig.viewport){var d=b.weex.config.env.scale,e=b.weex.config.env.deviceWidth,f=Math.ceil(e/d);Object.assign(__uniConfig,{viewport:f,defaultFontSize:Math.round(f/20)})}return{instance:{__uniConfig:__uniConfig,__uniRoutes:__uniRoutes,global:void 0,window:void 0,document:void 0,frames:void 0,self:void 0,location:void 0,navigator:void 0,localStorage:void 0,history:void 0,Caches:void 0,screen:void 0,alert:void 0,confirm:void 0,prompt:void 0,fetch:void 0,XMLHttpRequest:void 0,WebSocket:void 0,webkit:void 0,print:void 0}}}});
