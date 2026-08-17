package com.shopsense.service;

import com.shopsense.dto.MessageResponse;
import com.shopsense.dto.SearchHistoryItemResponse;
import com.shopsense.dto.SearchHistoryListResponse;
import com.shopsense.dto.SearchHistoryRequest;
import com.shopsense.entity.SearchHistory;
import com.shopsense.entity.User;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.SearchHistoryRepository;
import com.shopsense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {

        private final SearchHistoryRepository searchHistoryRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public MessageResponse saveSearchHistory(Long userId, SearchHistoryRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                SearchHistory searchHistory = SearchHistory.builder()
                                .user(user)
                                .searchQuery(request.getQuery().trim())
                                .build();

                searchHistoryRepository.save(searchHistory);

                return new MessageResponse("Search history saved.");
        }

        @Override
        @Transactional(readOnly = true)
        public SearchHistoryListResponse getUserSearchHistory(Long userId) {
                List<SearchHistory> historyList = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);

                List<SearchHistoryItemResponse> items = historyList.stream()
                                .map(sh -> SearchHistoryItemResponse.builder()
                                                .id(sh.getId())
                                                .query(sh.getSearchQuery())
                                                .searchedAt(sh.getSearchedAt())
                                                .build())
                                .collect(Collectors.toList());

                return SearchHistoryListResponse.builder()
                                .history(items)
                                .build();
        }

        @Override
        @Transactional
        public void deleteSearchHistoryItem(Long userId, Long id) {
                SearchHistory item = searchHistoryRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Search history item not found with id: " + id));

                searchHistoryRepository.delete(item);
        }

        @Override
        @Transactional
        public void clearUserSearchHistory(Long userId) {
                searchHistoryRepository.deleteByUserId(userId);
        }
}
