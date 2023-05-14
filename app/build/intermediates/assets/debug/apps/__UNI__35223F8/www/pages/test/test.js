"use weex:vue";

if (typeof Promise !== 'undefined' && !Promise.prototype.finally) {
  Promise.prototype.finally = function(callback) {
    const promise = this.constructor
    return this.then(
      value => promise.resolve(callback()).then(() => value),
      reason => promise.resolve(callback()).then(() => {
        throw reason
      })
    )
  }
};

if (typeof uni !== 'undefined' && uni && uni.requireGlobal) {
  const global = uni.requireGlobal()
  ArrayBuffer = global.ArrayBuffer
  Int8Array = global.Int8Array
  Uint8Array = global.Uint8Array
  Uint8ClampedArray = global.Uint8ClampedArray
  Int16Array = global.Int16Array
  Uint16Array = global.Uint16Array
  Int32Array = global.Int32Array
  Uint32Array = global.Uint32Array
  Float32Array = global.Float32Array
  Float64Array = global.Float64Array
  BigInt64Array = global.BigInt64Array
  BigUint64Array = global.BigUint64Array
};


(()=>{var u=Object.create;var p=Object.defineProperty;var i=Object.getOwnPropertyDescriptor;var g=Object.getOwnPropertyNames;var f=Object.getPrototypeOf,d=Object.prototype.hasOwnProperty;var m=(e,t)=>()=>(t||e((t={exports:{}}).exports,t),t.exports);var w=(e,t,o,r)=>{if(t&&typeof t=="object"||typeof t=="function")for(let s of g(t))!d.call(e,s)&&s!==o&&p(e,s,{get:()=>t[s],enumerable:!(r=i(t,s))||r.enumerable});return e};var v=(e,t,o)=>(o=e!=null?u(f(e)):{},w(t||!e||!e.__esModule?p(o,"default",{value:e,enumerable:!0}):o,e));var _=m((A,a)=>{a.exports=Vue});var l=v(_());function y(e,t,...o){uni.__log__?uni.__log__(e,t,...o):console[e].apply(console,[...o,t])}var b=(e,t)=>{let o=e.__vccOpts||e;for(let[r,s]of t)o[r]=s;return o},h={data(){return{}},onLoad(e){y("log","at pages/test/test.nvue:16",e)},methods:{}};function x(e,t,o,r,s,$){return(0,l.openBlock)(),(0,l.createElementBlock)("scroll-view",{scrollY:!0,showScrollbar:!0,enableBackToTop:!0,bubble:"true",style:{flexDirection:"column"}},[(0,l.createElementVNode)("view",null,[(0,l.createElementVNode)("view",null,[(0,l.createElementVNode)("u-image",{mode:"widthFix",src:"/static/101297089.jpg"})])])])}var n=b(h,[["render",x]]);var c=plus.webview.currentWebview();if(c){let e=parseInt(c.id),t="pages/test/test",o={};try{o=JSON.parse(c.__query__)}catch(s){}n.mpType="page";let r=Vue.createPageApp(n,{$store:getApp({allowDefault:!0}).$store,__pageId:e,__pagePath:t,__pageQuery:o});r.provide("__globalStyles",Vue.useCssStyles([...__uniConfig.styles,...n.styles||[]])),r.mount("#root")}})();
