!function(t){var e={};function n(o){if(e[o])return e[o].exports;var r=e[o]={i:o,l:!1,exports:{}};return t[o].call(r.exports,r,r.exports,n),r.l=!0,r.exports}n.m=t,n.c=e,n.d=function(t,e,o){n.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:o})},n.r=function(t){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},n.t=function(t,e){if(1&e&&(t=n(t)),8&e)return t;if(4&e&&"object"==typeof t&&t&&t.__esModule)return t;var o=Object.create(null);if(n.r(o),Object.defineProperty(o,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var r in t)n.d(o,r,function(e){return t[e]}.bind(null,r));return o},n.n=function(t){var e=t&&t.__esModule?function(){return t.default}:function(){return t};return n.d(e,"a",e),e},n.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},n.p="",n(n.s=16)}([function(t,e){t.exports={"@VERSION":2}},function(t,e,n){"use strict";function o(t,e,n,o,r,u,i,s,a,c){var l,f="function"==typeof t?t.options:t;if(a){f.components||(f.components={});var p=Object.prototype.hasOwnProperty;for(var d in a)p.call(a,d)&&!p.call(f.components,d)&&(f.components[d]=a[d])}if(c&&((c.beforeCreate||(c.beforeCreate=[])).unshift((function(){this[c.__module]=this})),(f.mixins||(f.mixins=[])).push(c)),e&&(f.render=e,f.staticRenderFns=n,f._compiled=!0),o&&(f.functional=!0),u&&(f._scopeId="data-v-"+u),i?(l=function(t){(t=t||this.$vnode&&this.$vnode.ssrContext||this.parent&&this.parent.$vnode&&this.parent.$vnode.ssrContext)||"undefined"==typeof __VUE_SSR_CONTEXT__||(t=__VUE_SSR_CONTEXT__),r&&r.call(this,t),t&&t._registeredComponents&&t._registeredComponents.add(i)},f._ssrRegister=l):r&&(l=s?function(){r.call(this,this.$root.$options.shadowRoot)}:r),l)if(f.functional){f._injectStyles=l;var _=f.render;f.render=function(t,e){return l.call(e),_(t,e)}}else{var y=f.beforeCreate;f.beforeCreate=y?[].concat(y,l):[l]}return{exports:t,options:f}}n.d(e,"a",(function(){return o}))},,function(t,e,n){"use strict";var o=n(10),r=n(7),u=n(1);var i=Object(u.a)(r.default,o.b,o.c,!1,null,null,"6afe8d46",!1,o.a,void 0);(function(t){this.options.style||(this.options.style={}),Vue.prototype.__merge_style&&Vue.prototype.__$appStyle__&&Vue.prototype.__merge_style(Vue.prototype.__$appStyle__,this.options.style)}).call(i),e.default=i.exports},,,,function(t,e,n){"use strict";var o=n(8),r=n.n(o);e.default=r.a},function(t,e,n){"use strict";Object.defineProperty(e,"__esModule",{value:!0}),e.default=void 0;e.default={data:function(){return{}},methods:{}}},,function(t,e,n){"use strict";n.d(e,"b",(function(){return o})),n.d(e,"c",(function(){return r})),n.d(e,"a",(function(){}));var o=function(){var t=this.$createElement,e=this._self._c||t;return e("scroll-view",{staticStyle:{flexDirection:"column"},attrs:{scrollY:!0,showScrollbar:!0,enableBackToTop:!0,bubble:"true"}},[e("view",[e("u-text",[this._v("\n\ttest\n")])])])},r=[]},function(t,e,n){Vue.prototype.__$appStyle__={},Vue.prototype.__merge_style&&Vue.prototype.__merge_style(n(12).default,Vue.prototype.__$appStyle__)},function(t,e,n){"use strict";n.r(e);var o=n(0),r=n.n(o);for(var u in o)"default"!==u&&function(t){n.d(e,t,(function(){return o[t]}))}(u);e.default=r.a},,,,function(t,e,n){"use strict";n.r(e);n(11);var o=n(3);"undefined"==typeof Promise||Promise.prototype.finally||(Promise.prototype.finally=function(t){var e=this.constructor;return this.then((function(n){return e.resolve(t()).then((function(){return n}))}),(function(n){return e.resolve(t()).then((function(){throw n}))}))}),o.default.mpType="page",o.default.route="pages/test/test",o.default.el="#root",new Vue(o.default)}]);