package com.lilei.leiaiagent.mapper;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.lilei.leiaiagent.dao.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageRepository extends CrudRepository<ChatMessageMapper, ChatMessage> {
}