/* eslint-disable prettier/prettier */
// 监听事件
importScripts('../../hybrid/html/js/utils.js')

this.addEventListener('message', (e) => {
  const { data } = getData(e)
  postMessage(...setData(data))
})