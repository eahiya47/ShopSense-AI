package com.shopsense.service;

import com.shopsense.dto.MessageResponse;
import com.shopsense.dto.SearchHistoryListResponse;
import com.shopsense.dto.SearchHistoryRequest;
import com.shopsense.entity.SearchHistory;
import com.shopsense.entity.User;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.SearchHistoryRepository;
import com.shopsense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchHistoryServiceImpl searchHistoryService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    @DisplayName("Should successfully save search history entry")
    void testSaveSearchHistory_Success() {
        SearchHistoryRequest request = SearchHistoryRequest.builder().query("iphone 16").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        MessageResponse response = searchHistoryService.saveSearchHistory(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Search history saved.");

        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }

    @Test
    @DisplayName("Should return user search history ordered by timestamp desc")
    void testGetUserSearchHistory_Success() {
        SearchHistory item = SearchHistory.builder()
                .id(100L)
                .user(sampleUser)
                .searchQuery("iphone 16")
                .searchedAt(LocalDateTime.now())
                .build();

        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(1L)).thenReturn(List.of(item));

        SearchHistoryListResponse response = searchHistoryService.getUserSearchHistory(1L);

        assertThat(response).isNotNull();
        assertThat(response.getHistory()).hasSize(1);
        assertThat(response.getHistory().get(0).getQuery()).isEqualTo("iphone 16");
    }

    @Test
    @DisplayName("Should delete search history item owned by user")
    void testDeleteSearchHistoryItem_Success() {
        SearchHistory item = SearchHistory.builder()
                .id(100L)
                .user(sampleUser)
                .searchQuery("iphone 16")
                .build();

        when(searchHistoryRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(item));

        searchHistoryService.deleteSearchHistoryItem(1L, 100L);

        verify(searchHistoryRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent or unowned search history entry")
    void testDeleteSearchHistoryItem_NotFound() {
        when(searchHistoryRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> searchHistoryService.deleteSearchHistoryItem(1L, 999L));
        verify(searchHistoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should bulk delete all search history for authenticated user")
    void testClearUserSearchHistory_Success() {
        searchHistoryService.clearUserSearchHistory(1L);

        verify(searchHistoryRepository, times(1)).deleteByUserId(1L);
    }
}
