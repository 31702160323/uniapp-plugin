if("undefined"==typeof Promise||Promise.prototype.finally||(Promise.prototype.finally=function(t){const i=this.constructor;return this.then((e=>i.resolve(t()).then((()=>e))),(e=>i.resolve(t()).then((()=>{throw e}))))}),"undefined"!=typeof uni&&uni&&uni.requireGlobal){const t=uni.requireGlobal();ArrayBuffer=t.ArrayBuffer,Int8Array=t.Int8Array,Uint8Array=t.Uint8Array,Uint8ClampedArray=t.Uint8ClampedArray,Int16Array=t.Int16Array,Uint16Array=t.Uint16Array,Int32Array=t.Int32Array,Uint32Array=t.Uint32Array,Float32Array=t.Float32Array,Float64Array=t.Float64Array,BigInt64Array=t.BigInt64Array,BigUint64Array=t.BigUint64Array}uni.restoreGlobal&&uni.restoreGlobal(Vue,weex,plus,setTimeout,clearTimeout,setInterval,clearInterval),function(t){"use strict";function i(t){return weex.requireModule(t)}function e(t,i,...e){uni.__log__?uni.__log__(t,i,...e):console[t].apply(console,[...e,i])}var n,o,s=/^(\[.+\])+/,a=/^\s*(\w+)\s*:(.*)$/,c=/^\s*(\d+)\s*:\s*(\d+(\s*[\.:]\s*\d+)?)\s*$/;function r(t){var i,e,o=function(t){t=t.trim();var i=s.exec(t);if(null===i)return null;var e=i[0],n=t.substr(e.length);return[e.slice(1,-1).split(/\]\s*\[/),n]}(t);try{if(o){var r=o[0],l=o[1];return c.test(r[0])?function(t,i){var e=[];return t.forEach((function(t){var i=c.exec(t),n=parseFloat(i[1]),o=parseFloat(i[2].replace(/\s+/g,"").replace(":","."));e.push(60*n+o)})),{type:n.TIME,timestamps:e,content:i.trim()}}(r,l):(i=r[0],e=a.exec(i),{type:n.INFO,key:e[1].trim(),value:e[2].trim()})}return{type:n.INVALID}}catch(u){return{type:n.INVALID}}}function l(t,i){for(void 0===i&&(i=2);t.toString().split(".")[0].length<i;)t="0"+t;return t}function u(t){return l(Math.floor(t/60))+":"+l((t%60).toFixed(2))}(o=n||(n={})).INVALID="INVALID",o.INFO="INFO",o.TIME="TIME";var p=function(){function t(){this.info={},this.lyrics=[]}return t.parse=function(t){var i=[],e={};t.split(/\r\n|[\n\r]/g).map((function(t){return r(t)})).forEach((function(t){switch(t.type){case n.INFO:e[t.key]=t.value;break;case n.TIME:t.timestamps.forEach((function(e){i.push({timestamp:e,content:t.content})}))}}));var o=new this;return o.lyrics=i,o.info=e,o},t.prototype.offset=function(t){this.lyrics.forEach((function(i){i.timestamp+=t,i.timestamp<0&&(i.timestamp=0)}))},t.prototype.clone=function(){function i(t){var i={};for(var e in t)i[e]=t[e];return i}var e=new t;return e.info=i(this.info),e.lyrics=this.lyrics.reduce((function(t,e){return t.push(i(e)),t}),[]),e},t.prototype.toString=function(t){void 0===t&&(t={}),t.combine=!("combine"in t)||t.combine,t.lineFormat="lineFormat"in t?t.lineFormat:"\r\n",t.sort=!("sort"in t)||t.sort;var i=[],e={},n=[];for(var o in this.info)i.push("["+o+":"+this.info[o]+"]");if(t.combine){for(var s in this.lyrics.forEach((function(t){t.content in e?e[t.content].push(t.timestamp):e[t.content]=[t.timestamp]})),e)t.sort&&e[s].sort(),n.push({timestamps:e[s],content:s});t.sort&&n.sort((function(t,i){return t.timestamps[0]-i.timestamps[0]})),n.forEach((function(t){i.push("["+t.timestamps.map((function(t){return u(t)})).join("][")+"]"+(t.content||""))}))}else this.lyrics.forEach((function(t){i.push("["+u(t.timestamp)+"]"+(t.content||""))}));return i.join(t.lineFormat)},t}(),m=function(){function t(t,i){void 0===t&&(t=new p),void 0===i&&(i=!0),this.offset=i,this._currentIndex=-1,this.setLrc(t)}return t.prototype.setLrc=function(t){this.lrc=t.clone(),this.lrcUpdate()},t.prototype.lrcUpdate=function(){this.offset&&this._offsetAlign(),this._sort()},t.prototype._offsetAlign=function(){if("offset"in this.lrc.info){var t=parseInt(this.lrc.info.offset)/1e3;isNaN(t)||(this.lrc.offset(t),delete this.lrc.info.offset)}},t.prototype._sort=function(){this.lrc.lyrics.sort((function(t,i){return t.timestamp-i.timestamp}))},t.prototype.timeUpdate=function(t){this._currentIndex>=this.lrc.lyrics.length?this._currentIndex=this.lrc.lyrics.length-1:this._currentIndex<-1&&(this._currentIndex=-1),this._currentIndex=this._findIndex(t,this._currentIndex)},t.prototype._findIndex=function(t,i){var e=-1==i?Number.NEGATIVE_INFINITY:this.lrc.lyrics[i].timestamp,n=i==this.lrc.lyrics.length-1?Number.POSITIVE_INFINITY:this.lrc.lyrics[i+1].timestamp;return t<e?this._findIndex(t,i-1):t===n?n===Number.POSITIVE_INFINITY?i:i+1:t>n?this._findIndex(t,i+1):i},t.prototype.getInfo=function(){return this.lrc.info},t.prototype.getLyrics=function(){return this.lrc.lyrics},t.prototype.getLyric=function(t){if(void 0===t&&(t=this.curIndex()),t>=0&&t<=this.lrc.lyrics.length-1)return this.lrc.lyrics[t];throw new Error("Index not exist")},t.prototype.curIndex=function(){return this._currentIndex},t.prototype.curLyric=function(){return this.getLyric()},t}();const h=[{id:1,musicName:"美人谷",musicArtist:"阿兰",musicAlbum:"美人谷",musicAlbumID:1,musicAlbumURl:"https://p1.music.126.net/byZ9hvAI2r20WBnuB-S_ng==/109951163069341151.jpg?imageView&thumbnail=360y360&quality=75&tostatic=0",musicPath:"/static/1.aac",musicYear:"2017-10-27",musicDuration:281e3,size:3436481,favour:!1,lyric:"[ti:美人谷]\n[ar:阿兰]\n[al:十念]\n\n[by:天龙888]\n[00:00.00]美人谷 - 阿兰\n[00:05.00]\n[00:07.95]词：毛慧\n[00:09.09]曲：阿兰/毛慧\n[00:11.31]编曲：叶月/王晨\n[00:13.57]制作人：毛慧\n[00:15.76]录音：张生磊（记忆时刻录音棚）\n[00:17.45]\n[00:38.13]越过山河的神秘\n[00:41.03]一幕自然的洗礼\n[00:44.18]看见山川出云泽被着大地\n[00:49.76]\n[00:50.59]雨顺菁华自天际\n[00:53.69]花随鱼翔潜水底\n[00:57.44]可有语言能形容的美丽\n[01:02.62]\n[01:03.44]如此熟悉的声音\n[01:06.38]莫名安全的气息\n[01:09.45]这是谁安排了我们的相聚\n[01:14.87]\n[01:15.84]没有前世的际遇\n[01:18.93]怎会有心灵相犀\n[01:22.48]任日夕月落耳鬓无语\n[01:28.34]\n[01:31.72]如果道路很遥远\n[01:35.19]你是否会愿意\n[01:38.44]等春播秋忙\n[01:40.34]夏耕冬藏\n[01:41.83]平凡的神谕\n[01:44.69]如果你看到我的心\n[01:47.84]就能看到自己\n[01:51.06]我会陪你一同醉去\n[01:57.00]\n[02:47.58]采撷飘来的飞絮\n[02:50.56]点缀纯净的天宇\n[02:53.73]谁让万物轮转都不会死去\n[02:58.84]\n[03:00.04]穿越最远的距离\n[03:03.14]唤醒沉睡的记忆\n[03:06.91]我的爱在这里等你\n[03:11.88]\n[03:12.74]如果道路很遥远\n[03:16.20]你是否会愿意\n[03:19.49]等春播秋忙\n[03:21.35]夏耕冬藏\n[03:22.89]平凡的神谕\n[03:25.78]如果你看到我的心\n[03:28.98]就能看到自己\n[03:32.01]我会陪你一同醉去\n[03:37.11]\n[03:38.01]如果时间很遥远\n[03:41.46]你是否会愿意\n[03:44.66]等朝花夕拾\n[03:46.57]缤纷舞尽\n[03:48.16]神灵的游戏\n[03:50.96]如果你能找到我心\n[03:54.19]就能找到自己\n[03:57.32]因为我的爱已随你而去\n[04:04.55]\n[04:06.88]lrc歌词编辑：天龙 QQ：26092798"},{id:2,musicName:"朝暮",musicArtist:"阿兰",musicAlbum:"朝暮",musicAlbumID:2,musicAlbumURl:"https://p1.music.126.net/hB5AqPeXTg5Q-5BsyLiCwg==/109951164325877834.jpg?imageView&thumbnail=360y360&quality=75&tostatic=0",musicPath:"/static/2.mp3",musicYear:"2019-08-28",musicDuration:247e3,size:3022101,favour:!1,lyric:"[ti:朝暮]\n[ar:阿兰]\n[al:朝暮]\n[by:]\n[offset:0]\n[00:00.22]朝暮 - 阿兰\n[00:01.15]词：林乔\n[00:01.75]曲：罗力威\n[00:02.55]编曲：郭峻江\n[00:03.47]制作人：郭峻江\n[00:04.87]人声监制/录音：张生磊\n[00:07.01]吉他录制：金天\n[00:08.23]弦乐录制：国际首席爱乐乐团\n[00:10.57]混音工程师：李嘉佳\n[00:12.15]混音工作室：记忆时刻\n[00:13.96]母带制作：玉霖@麦合星臣\n[00:15.87]出品：阿兰工作室\n[00:40.33]天雨润酥街 我遥遥 又怅羡\n[00:45.02]莫非清风转 你茫茫 化流年\n[00:49.84]落晖别有涟 撑纸伞\n[00:53.07]徐徐踱步犹犹几夜\n[00:59.55]跃上星宿间 我幽幽 洒心愿\n[01:04.26]月华徒倚圆 你返返 在何年\n[01:09.88]若祈愿苍天 聚散一眸间\n[01:18.76]朝朝若相盼 暮暮若相唤\n[01:23.60]何何你再伴 流流萤扑扇\n[01:28.28]长啸复琴弹 歌待欢\n[01:33.33]怎负花尽看 惹泪低惋\n[01:38.24]朝朝若相盼 暮暮若相唤\n[01:42.79]何何你再伴 红红烛窗婉\n[01:47.46]天涯再远端 近思眷\n[01:52.53]岁月终会换 执手彼岸 相挽\n[02:01.92]天雨润酥街 我遥遥 又怅羡\n[02:06.74]莫非清风转 你茫茫 化流年\n[02:11.65]落晖别有涟 撑纸伞\n[02:14.62]徐徐踱步犹犹几夜\n[02:21.26]跃上星宿间 我幽幽 洒心愿\n[02:26.02]月华徒倚圆 你返返 在何年\n[02:31.51]若祈愿苍天 聚散一眸间\n[03:02.02]朝朝若相盼 暮暮若相唤\n[03:06.76]何何你再伴 流流萤扑扇\n[03:11.51]长啸复琴弹 歌待欢\n[03:16.43]怎负花尽看 惹泪低惋\n[03:21.39]朝朝若相盼 暮暮若相唤\n[03:25.87]何何你再伴 红红烛窗婉\n[03:30.67]天涯再远端 近思眷\n[03:35.71]岁月终会换 执手彼岸 相挽\n[03:45.25]岁月终会换 执手彼岸 相挽"}],f=(t,i)=>{const e=t.__vccOpts||t;for(const[n,o]of i)e[n]=o;return e};const y=f({data:()=>({playIndex:0,playing:!1,playMode:"sequence",playlist:h,isLockActivity:!1,isCreateNotification:!1,systemNotification:!1,themeColor:"#55ff00ff"}),computed:{favour(){var t;return(null==(t=this.playlist[this.playIndex])?void 0:t.favour)||!1}},onLoad(){let t;this.audioMannager=uni.getBackgroundAudioManager(),this.musicNotification=i("XZH-musicNotification"),this.musicNotification.init({path:"/pages/test/test?id=1",icon:""}),this.musicNotification.getLocalSong((t=>{e("log","at pages/musicNotification/musicNotification.vue:77","songData",t)})),plus.globalEvent.addEventListener("musicLifecycle",(t=>{e("log","at pages/musicNotification/musicNotification.vue:90","生命周期",t)})),plus.globalEvent.addEventListener("musicNotificationPause",(t=>{e("log","at pages/musicNotification/musicNotification.vue:94","暂停或播放按钮事件回调",t),this.playOrPause()})),plus.globalEvent.addEventListener("musicNotificationPrevious",(t=>{e("log","at pages/musicNotification/musicNotification.vue:99","播放上一首按钮事件回调",t),this.last()})),plus.globalEvent.addEventListener("musicNotificationNext",(t=>{e("log","at pages/musicNotification/musicNotification.vue:104","播放下一首按钮事件回调",t),this.next()})),plus.globalEvent.addEventListener("musicNotificationFavourite",(t=>{e("log","at pages/musicNotification/musicNotification.vue:109","收藏按钮事件回调",t),this.setFavour()})),plus.globalEvent.addEventListener("musicNotificationClose",(t=>{e("log","at pages/musicNotification/musicNotification.vue:114","关闭按钮事件回调",t),this.logout()})),plus.globalEvent.addEventListener("musicMediaButton",(t=>{switch(e("log","at pages/musicNotification/musicNotification.vue:119","耳机按钮事件回调",t),uni.showToast({title:JSON.stringify(t),icon:"none",position:"center"}),t.type){case"headset":case"bluetooth":0===t.keyCode&&this.playOrPause(!1);break;case"mediaButton":switch(t.keyCode){case 79:this.playOrPause();break;case 87:this.next();break;case 88:this.last();break;case 126:this.playOrPause(!0);break;case 127:this.playOrPause(!1)}}})),plus.globalEvent.addEventListener("musicSeekTo",(t=>{e("log","at pages/musicNotification/musicNotification.vue:168","通知栏进度条拖动事件回调",t.position),this.audioMannager.seek(t.position)})),this.audioMannager.onPlay((()=>{t=new m(p.parse(this.playlist[this.playIndex].lyric)),this.audioMannager.onTimeUpdate((i=>{const n=Math.max(0,this.audioMannager.currentTime);e("log","at pages/musicNotification/musicNotification.vue:177","onTimeUpdate",n),this.musicNotification.setPosition(1e3*n),t.timeUpdate(n);const o=t.curLyric().content;o&&this.musicNotification.setLyric(o)}))})),this.audioMannager.onCanplay((()=>{switch(e("log","at pages/musicNotification/musicNotification.vue:188","onCanplay"),this.musicNotification.update({songName:this.playlist[this.playIndex].musicName,artistsName:this.playlist[this.playIndex].musicArtist,favour:this.playlist[this.playIndex].favour,picUrl:this.playlist[this.playIndex].musicAlbumURl,duration:1e3*this.audioMannager.duration}).code){case-1:return void e("log","at pages/musicNotification/musicNotification.vue:204","未知错误");case-2:return void this.musicNotification.openPermissionSetting()}})),this.audioMannager.onEnded((()=>this.next())),this.audioMannager.onError((()=>this.next()))},onUnload(){plus.globalEvent.removeEventListener("musicLifecycle"),plus.globalEvent.removeEventListener("musicNotificationPause"),plus.globalEvent.removeEventListener("musicNotificationPrevious"),plus.globalEvent.removeEventListener("musicNotificationNext"),plus.globalEvent.removeEventListener("musicNotificationFavourite"),plus.globalEvent.removeEventListener("musicNotificationClose"),plus.globalEvent.removeEventListener("musicMediaButton"),plus.globalEvent.removeEventListener("musicSeekTo")},onBackPress(){let t=plus.android.runtimeMainActivity();return plus.android.invoke(t,"moveTaskToBack",!1),!0},methods:{showFloatWindow(){this.checkOverlayDisplayPermission()?this.musicNotification.showFloatWindow("#AD1EF7"):this.showLockActivityModal((()=>this.musicNotification.showFloatWindow("#AD1EF7")))},hideFloatWindow(){this.musicNotification.hideFloatWindow()},checkOverlayDisplayPermission(){return this.musicNotification.checkOverlayDisplayPermission()},last(){this.play(--this.playIndex)},next(){this.play(++this.playIndex)},async play(t){this.isCreateNotification||await this.createNotification(),this.playIndex=t,this.playIndex<0?this.playIndex=this.playlist.length-1:this.playIndex>this.playlist.length-1&&(this.playIndex=0);const i=this.playlist[this.playIndex];this.audioMannager.title=i.musicName,this.audioMannager.singer=i.musicArtist,this.audioMannager.coverImgUrl=i.musicAlbumURl,this.audioMannager.src=i.musicPath,this.playOrPause(!0)},createNotification(){return new Promise(((t,i)=>{this.musicNotification.createNotification((()=>{this.isCreateNotification=!0,t()}))}))},playOrPause(t){this.playing="boolean"==typeof t?t:!this.playing,this.musicNotification.playOrPause(this.playing),this.musicNotification.setPosition(1e3*this.audioMannager.currentTime),this.playing?this.audioMannager.play():this.audioMannager.pause()},lockActivity(){this.checkOverlayDisplayPermission()?(this.musicNotification.openLockActivity(!this.isLockActivity),this.isLockActivity=!this.isLockActivity):this.showLockActivityModal((()=>this.lockActivity()))},showLockActivityModal(t){uni.showModal({content:"该功能需要开启悬浮窗权限",success:({confirm:i})=>{i&&(plus.globalEvent.addEventListener("openLockActivity",(({type:i})=>{i&&t(),plus.globalEvent.removeEventListener("openLockActivity")})),this.musicNotification.openOverlaySetting())}})},setFavour(){this.playlist[this.playIndex].favour=!this.playlist[this.playIndex].favour,this.musicNotification.favour(this.playlist[this.playIndex].favour)},switchNotification(){this.systemNotification=!this.systemNotification,this.musicNotification.switchNotification(this.systemNotification)},setWidgetStyle(){this.themeColor="#55ff00ff"===this.themeColor?"#55ffff00":"#55ff00ff",this.musicNotification.setWidgetStyle({themeColor:this.themeColor,titleColor:"#FFFFFF",artistColor:"#ff00ff"})},logout(){this.isCreateNotification=!1,e("log","at pages/musicNotification/musicNotification.vue:355","logout"),this.musicNotification.cancel(),this.audioMannager.stop()}}},[["render",function(i,e,n,o,s,a){return t.openBlock(),t.createElementBlock("view",{style:{"padding-bottom":"250rpx"}},[(t.openBlock(!0),t.createElementBlock(t.Fragment,null,t.renderList(s.playlist,((i,e)=>(t.openBlock(),t.createElementBlock("view",{key:i.id,onClick:t=>a.play(e),class:t.normalizeClass(["music-item",{"music-item__hover":s.playIndex===e}])},[t.createElementVNode("view",{class:"music-info"},[t.createElementVNode("text",{class:"music-name"},t.toDisplayString(i.musicName),1),t.createElementVNode("view",{class:"music-creators"},[t.createElementVNode("text",{class:"music-creator"},t.toDisplayString(i.musicArtist+" "),1),t.createElementVNode("text",{class:"music-al"},"- "+t.toDisplayString(i.musicAlbum),1)])])],10,["onClick"])))),128)),t.createElementVNode("view",{class:"bottom"},[t.createElementVNode("view",{onClick:e[0]||(e[0]=(...t)=>a.showFloatWindow&&a.showFloatWindow(...t))},"打开浮窗"),t.createElementVNode("view",{onClick:e[1]||(e[1]=(...t)=>a.hideFloatWindow&&a.hideFloatWindow(...t))},"关闭浮窗"),t.createElementVNode("view",{onClick:e[2]||(e[2]=(...t)=>a.setFavour&&a.setFavour(...t))},t.toDisplayString(a.favour?"搜藏":"未搜藏"),1),t.createElementVNode("view",{onClick:e[3]||(e[3]=(...t)=>a.last&&a.last(...t))},"上一首"),t.createElementVNode("view",{onClick:e[4]||(e[4]=(...t)=>a.playOrPause&&a.playOrPause(...t))},t.toDisplayString(s.playing?"暂停":"播放"),1),t.createElementVNode("view",{onClick:e[5]||(e[5]=(...t)=>a.next&&a.next(...t))},"下一首"),t.createElementVNode("view",{onClick:e[6]||(e[6]=(...t)=>a.switchNotification&&a.switchNotification(...t))},t.toDisplayString(s.systemNotification?"系统":"自定义"),1),t.createElementVNode("view",{onClick:e[7]||(e[7]=(...t)=>a.lockActivity&&a.lockActivity(...t))},t.toDisplayString(s.isLockActivity?"关闭":"打开")+"锁屏页",1),t.createElementVNode("view",{onClick:e[8]||(e[8]=(...t)=>a.setWidgetStyle&&a.setWidgetStyle(...t))},"修改小部件"),t.createElementVNode("view",{onClick:e[9]||(e[9]=(...t)=>a.logout&&a.logout(...t))},"退出")])])}]]),d=i("XZH-VaryDesktopIcons");const v=f({data:()=>({}),onLoad(t){e("log","at pages/test/test.vue:21",t)},methods:{replaceAppIcon1:function(){var t={iconName:"wrsicon1",restartSystemLauncher:!0};d.replaceAppIcon(t)},replaceAppIcon2:function(){var t={iconName:"wrsicon2",restartSystemLauncher:!0};d.replaceAppIcon(t)},replaceAppIcon3:function(){var t={iconName:"wrsicon3",restartSystemLauncher:!0};d.replaceAppIcon(t)},resetAppIcon:function(){d.resetAppIcon()}}},[["render",function(i,e,n,o,s,a){return t.openBlock(),t.createElementBlock("view",null,[t.createElementVNode("view",null,[t.createElementVNode("image",{mode:"widthFix",src:"/static/101297089.jpg"}),t.createElementVNode("button",{onClick:e[0]||(e[0]=t=>a.replaceAppIcon1())},"切换到icon图标-天气晴"),t.createElementVNode("button",{onClick:e[1]||(e[1]=t=>a.replaceAppIcon2())},"切换到icon图标-天气雨"),t.createElementVNode("button",{onClick:e[2]||(e[2]=t=>a.replaceAppIcon3())},"切换到icon图标-天气雪"),t.createElementVNode("button",{onClick:e[3]||(e[3]=t=>a.resetAppIcon())},"恢复原先icon")])])}]]);__definePage("pages/musicNotification/musicNotification",y),__definePage("pages/test/test",v);const g={onLaunch:function(){e("log","at App.vue:4","App Launch")},onShow:function(){if(e("log","at App.vue:7","App Show"),"android"==uni.getSystemInfoSync().platform){const t=plus.android.runtimeMainActivity(),i=plus.android.invoke(t,"getIntent"),e=plus.android.invoke(i,"getStringExtra","path");if(null===e)return;uni.navigateTo({url:e,complete:()=>{plus.android.invoke(i,"removeExtra","path"),plus.android.autoCollection(i)}})}},onHide:function(){e("log","at App.vue:27","App Hide")}};const{app:N,Vuex:I,Pinia:A}={app:t.createVueApp(g)};uni.Vuex=I,uni.Pinia=A,N.provide("__globalStyles",__uniConfig.styles),N._component.mpType="app",N._component.render=()=>{},N.mount("#app")}(Vue);
