package com.shopsense.service;

import com.shopsense.dto.MessageResponse;
import com.shopsense.dto.SearchHistoryListResponse;
import com.shopsense.dto.SearchHistoryRequest;

public interface SearchHistoryService {

    MessageResponse saveSearchHistory(Long userId, SearchHistoryRequest request);

    SearchHistoryListResponse getUserSearchHistory(Long userId);

    void deleteSearchHistoryItem(Long userId, Long id);

    void clearUserSearchHistory(Long userId);
}
