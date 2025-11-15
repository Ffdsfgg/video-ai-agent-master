package com.lilei.leiaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilei.leiaiagent.dao.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}

