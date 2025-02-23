/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

/**
 * Requests.
 *
 * <p>This package contains implementations of class Request.
 * The most popular and easy to use it {@link JdkRequest}.
 *
 * <p>However, in some situations {@link JdkRequest} falls short and
 * {@link ApacheRequest} should be used instead. For example,
 * {@link JdkRequest} doesn't support {@code PATCH} HTTP method due to
 * a bug in HttpURLConnection.
 *
 * @since 0.10
 */
package com.jcabi.http.request;
