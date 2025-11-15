import {get, post} from "../utils/requet.js";

//查询音频的基本信息
export const getVideoInfo = (id) => get(`/videoInfo/${id}`);

//请求音频基本信息
export const  getqwenTextAI=(subtitlesUrl,id,next)=>get(`/qwenTextAI/stream?subtitlesUrl=${subtitlesUrl}&id=${id}&next=${next}`);

export const instrtVideoInfo = (data) => post('/videoInfo/generate', data)