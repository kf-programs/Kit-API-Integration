package com.example.kitApp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.kitApp.service.KitApiService;
import com.example.kitApp.model.KitApiSubscribersResponse;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger; // added import
import org.mockito.stubbing.Answer; // added import
import org.mockito.invocation.InvocationOnMock; // added import

/**
 * Tests for KitApiController#getSubscribers
 * TODO add tests for other endpoints
 */
@ExtendWith(MockitoExtension.class)
public class KitApiControllerTest {

    @Mock
    private KitApiService kitApiService;

    private KitApiController controller;

    @BeforeEach
    void setUp() {
        controller = new KitApiController(kitApiService);
    }

    @Test
    void whenKitResponseIsNull_thenReturnsBadRequest() {
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(null);

        ResponseEntity<?> resp = controller.getSubscribers();

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("No response from Kit API.", resp.getBody());
    }

    @Test
    void whenEmailsEmpty_thenReturnsNotFound() {
        KitApiSubscribersResponse emptyResp = new KitApiSubscribersResponse(Collections.emptyList(), null);
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(emptyResp);

        ResponseEntity<?> resp = controller.getSubscribers();

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("No subscribers found.", resp.getBody());
    }

    @Test
    void whenEndCursorIsNull_noPagination_returnsEmails() {
        List<String> testEmails = Arrays.asList("a@example.com", "b@example.com");
        KitApiSubscribersResponse mockedKitApiResp = new KitApiSubscribersResponse(testEmails, null);
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(mockedKitApiResp);

        ResponseEntity<?> resp = controller.getSubscribers();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        List<String> body = (List<String>) resp.getBody();
        assertEquals(2, body.size());
        assertTrue(body.containsAll(testEmails));
    }

    @Test
    void whenEndCursorIsEmpty_noPagination_returnsEmails() {
        List<String> testEmails = Arrays.asList("x@x.com");
        KitApiSubscribersResponse mockedKitApiResp = new KitApiSubscribersResponse(testEmails, "");
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(mockedKitApiResp);

        ResponseEntity<?> resp = controller.getSubscribers();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        List<String> body = (List<String>) resp.getBody();
        assertEquals(1, body.size());
        assertTrue(body.containsAll(testEmails));
    }

    // Updated test: now interrupts and fails if extra pagination calls are attempted
    @Test
    void safetyCountExceeded_stopsAfterLimit_andAggregatesEmails() {
        // initial page
        List<String> initialRespEmails = Arrays.asList("first@page.com");
        KitApiSubscribersResponse firstKitApiResp = new KitApiSubscribersResponse(initialRespEmails, "cursor0");
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(firstKitApiResp);

        // prepare 8 subsequent pages (safety limit is 8 in controller)
        List<KitApiSubscribersResponse> pages = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            //give each response a unique email and a non-empty end cursor
            pages.add(new KitApiSubscribersResponse(
                Arrays.asList("p" + i + "@example.com"),
                "cursor" + i // non-empty so pagination would continue
            ));
        }

        // Protect against an infinite loop: count paginated calls and throw if more calls than expected occur.
        AtomicInteger callCounter = new AtomicInteger(0);
        final int expectedPaginatedCalls = pages.size(); // 8

        Answer<KitApiSubscribersResponse> pagedAnswer = new Answer<KitApiSubscribersResponse>() {
            @Override
            public KitApiSubscribersResponse answer(InvocationOnMock invocation) {
                int idx = callCounter.getAndIncrement();
                if (idx >= expectedPaginatedCalls) {
                    // Cause the controller to receive an exception (which will surface as a non-OK response).
                    throw new RuntimeException("Exceeded expected pagination calls - possible infinite loop");
                }
                return pages.get(idx);
            }
        };

        Mockito.when(kitApiService.fetchSubscribers(Mockito.anyString())).thenAnswer(pagedAnswer);

        ResponseEntity<?> resp = controller.getSubscribers();

        // If the controller exceeded the expected calls, the Answer threw and controller will return BAD_REQUEST.
        // Provide an informative assertion message so test fails visibly in that case.
        assertEquals(HttpStatus.OK, resp.getStatusCode(), "Controller returned non-OK: " + resp.getBody());

        @SuppressWarnings("unchecked")
        List<String> body = (List<String>) resp.getBody();
        // initial (1) + 8 pages (8) = 9 emails total
        assertEquals(9, body.size());
        assertTrue(body.contains("first@page.com"));
        assertTrue(body.contains("p1@example.com"));
        assertTrue(body.contains("p8@example.com"));

        // Also assert that we did not exceed expected paginated calls
        assertEquals(expectedPaginatedCalls, callCounter.get(), "Unexpected number of paginated calls made");
    }

    @Test
    void happyPath_returnsProperlyConfiguredEmails() {
        List<String> testEmails = Arrays.asList("one@test.com", "two@test.com", "three@test.com");
        KitApiSubscribersResponse mockedKitApiResp = new KitApiSubscribersResponse(testEmails, null);
        Mockito.when(kitApiService.fetchSubscribers(Mockito.isNull())).thenReturn(mockedKitApiResp);

        ResponseEntity<?> resp = controller.getSubscribers();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        List<String> body = (List<String>) resp.getBody();
        assertEquals(3, body.size());
        assertTrue(body.containsAll(testEmails));
    }
}
