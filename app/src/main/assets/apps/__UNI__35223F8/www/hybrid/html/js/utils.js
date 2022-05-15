/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable prettier/prettier */

/**
 * ArrayBuffer转为字符串，参数为ArrayBuffer对象
 * @param {ArrayBuffer} buf 
 * @returns {string}
 */
function arrayBufferToString(buf) {
  const piece = 1024 * 2;
  const totalSize = buf.byteLength; // 总大小
  let start = 0; // 每次上传的开始字节
  let end = start + piece; // 每次上传的结尾字节
  let chunks = '';
  while (start < totalSize) {
    // 根据长度截取每次需要上传的数据
    chunks += String.fromCharCode.apply(null, new Uint16Array(buf.slice(start, end < totalSize ? end : totalSize)));
    start = end;
    end = start + piece;
  }
  return chunks;
}

/**
 * 字符串转为ArrayBuffer对象，参数为字符串
 * @param {string} str 
 * @returns {ArrayBuffer}
 */
function stringToArrayBuffer(str) {
  const buf = new ArrayBuffer(str.length * 2); // 每个字符占用2个字节
  const bufView = new Uint16Array(buf);
  for (let i = 0, strLen = str.length; i < strLen; i++) {
    bufView[i] = str.charCodeAt(i);
  }
  return buf;
}

/**
 * 
 * @param {MessageEvent} e 
 * @returns 
 */
function getData(e) {
  const type = e.data.type
  const msg = e.data.msg instanceof ArrayBuffer ? arrayBufferToString(e.data.msg) : e.data.msg
  return { type, data: type === 'string' || typeof msg === 'object' ? msg : JSON.parse(msg) }
}

/**
 * 
 * @param {any} msg 
 * @returns 
 */
function setData(msg) {
  const type = typeof msg
  const data = type === 'string' ? msg : JSON.stringify(msg)
  if (data.length > 1024 * 2) {
    const arrayBuffer = stringToArrayBuffer(data)
    return [{
      type: type,
      msg: arrayBuffer
    }, [arrayBuffer]]
  }
  return [{
    type: type,
    msg: msg
  }]
}