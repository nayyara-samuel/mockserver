package org.mockserver.proxy.filters;

import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @author jamesdbloom
 */
public class FiltersTest {

    @Test
    public void shouldCallMatchingFiltersBeforeForwardingRequest() throws Exception {
        // given
        Filters filters = new Filters();
        // add first filter
        HttpRequest httpRequest = new HttpRequest();
        ProxyRequestFilter filter = mock(ProxyRequestFilter.class);
        filters.withFilter(httpRequest, filter);
        // add first filter with other request
        HttpRequest someOtherRequest = new HttpRequest().withPath("some_other_path");
        filters.withFilter(someOtherRequest, filter);
        // add second filter
        ProxyRequestFilter someOtherFilter = mock(ProxyRequestFilter.class);
        filters.withFilter(someOtherRequest, someOtherFilter);

        // when
        filters.applyFilters(httpRequest);

        // then
        verify(filter, times(1)).onRequest(same(httpRequest));
        verify(filter, times(0)).onRequest(same(someOtherRequest));
        verifyZeroInteractions(someOtherFilter);
    }

    @Test
    public void shouldCallMatchingFiltersAfterForwardingRequest() throws Exception {
        // given
        Filters filters = new Filters();
        HttpResponse httpResponse = new HttpResponse();
        // add first filter
        HttpRequest httpRequest = new HttpRequest();
        ProxyResponseFilter filter = mock(ProxyResponseFilter.class);
        when(filter.onResponse(any(HttpRequest.class), any(HttpResponse.class))).thenReturn(new HttpResponse());
        filters.withFilter(httpRequest, filter);
        // add first filter with other request
        HttpRequest someOtherRequest = new HttpRequest().withPath("some_other_path");
        filters.withFilter(someOtherRequest, filter);
        // add second filter
        ProxyResponseFilter someOtherFilter = mock(ProxyResponseFilter.class);
        filters.withFilter(someOtherRequest, someOtherFilter);

        // when
        filters.applyFilters(httpRequest, httpResponse);

        // then
        verify(filter, times(1)).onResponse(same(httpRequest), same(httpResponse));
        verify(filter, times(0)).onResponse(same(someOtherRequest), same(httpResponse));
        verifyZeroInteractions(someOtherFilter);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfHttpResponseIsNull() throws Exception {
        // given
        Filters filters = new Filters();
        HttpResponse httpResponse = new HttpResponse();
        // add first filter
        HttpRequest httpRequest = new HttpRequest();
        ProxyResponseFilter filter = mock(ProxyResponseFilter.class);
        when(filter.onResponse(any(HttpRequest.class), any(HttpResponse.class))).thenReturn(null);
        filters.withFilter(httpRequest, filter);

        // when
        filters.applyFilters(httpRequest, httpResponse);
    }
}
