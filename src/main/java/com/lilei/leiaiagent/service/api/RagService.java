package com.lilei.leiaiagent.service.api;

import reactor.core.publisher.Flux;

public interface RagService {

    Flux<String> doChatWithRagQuery(String message, String chatId);

}
