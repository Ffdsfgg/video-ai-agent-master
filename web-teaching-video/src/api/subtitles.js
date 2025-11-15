import {get} from "../utils/requet.js";

//获取字幕信息
export const getsubtitles = (id) => get(`/subtitles/${id}`);