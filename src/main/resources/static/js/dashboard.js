/**
 * Social Media Manager Dashboard - Dynamic Interactive Controller
 * Backed by Spring Boot REST API
 */

document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    // -------------------------------------------------------------------------
    // CSRF Token Helper for AJAX Requests
    // -------------------------------------------------------------------------
    function getCsrfHeaders() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const headers = { 'Content-Type': 'application/json' };
        if (tokenMeta && headerMeta && tokenMeta.content && headerMeta.content) {
            headers[headerMeta.content] = tokenMeta.content;
        }
        return headers;
    }

    // -------------------------------------------------------------------------
    // Toast Notification System
    // -------------------------------------------------------------------------
    const toastEl = document.getElementById('toastNotification');
    let toastTimeout = null;

    function showToast(message, type = 'success') {
        if (!toastEl) return;
        clearTimeout(toastTimeout);
        toastEl.className = 'toast-notification ' + type + ' show';
        toastEl.innerHTML = `
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5">
                ${type === 'success' 
                    ? '<polyline points="20 6 9 17 4 12"></polyline>' 
                    : '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>'}
            </svg>
            <span>${message}</span>
        `;
        toastTimeout = setTimeout(() => {
            toastEl.classList.remove('show');
        }, 3500);
    }

    // -------------------------------------------------------------------------
    // 1. Chart.js: Dynamic Follower Growth Chart (REST API integration)
    // -------------------------------------------------------------------------
    const growthCanvas = document.getElementById('followerGrowthChart');
    let followerGrowthChart = null;

    if (growthCanvas && typeof Chart !== 'undefined') {
        const ctx = growthCanvas.getContext('2d');

        // Create elegant vertical gradient
        const primaryGradient = ctx.createLinearGradient(0, 0, 0, 300);
        primaryGradient.addColorStop(0, 'rgba(79, 70, 229, 0.28)');
        primaryGradient.addColorStop(1, 'rgba(79, 70, 229, 0.01)');

        followerGrowthChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Current Period',
                        data: [],
                        borderColor: '#4f46e5',
                        borderWidth: 2.8,
                        backgroundColor: primaryGradient,
                        fill: true,
                        tension: 0.38,
                        pointBackgroundColor: '#ffffff',
                        pointBorderColor: '#4f46e5',
                        pointBorderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 7,
                        pointHoverBackgroundColor: '#4f46e5',
                        pointHoverBorderColor: '#ffffff',
                        pointHoverBorderWidth: 2.5
                    },
                    {
                        label: 'Previous Period',
                        data: [],
                        borderColor: '#cbd5e1',
                        borderWidth: 1.8,
                        borderDash: [5, 5],
                        backgroundColor: 'transparent',
                        fill: false,
                        tension: 0.38,
                        pointRadius: 0,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: '#94a3b8'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        align: 'end',
                        labels: {
                            boxWidth: 12,
                            boxHeight: 12,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            font: { family: "'Plus Jakarta Sans', sans-serif", size: 12, weight: '600' },
                            color: '#475569',
                            padding: 16
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#ffffff',
                        bodyColor: '#cbd5e1',
                        titleFont: { family: "'Plus Jakarta Sans', sans-serif", size: 13, weight: '700' },
                        bodyFont: { family: "'Plus Jakarta Sans', sans-serif", size: 12 },
                        padding: 12,
                        cornerRadius: 8,
                        displayColors: true,
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                if (label) label += ': ';
                                if (context.parsed.y !== null) {
                                    label += new Intl.NumberFormat().format(context.parsed.y) + ' followers';
                                }
                                return label;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false, drawBorder: false },
                        ticks: { font: { family: "'Plus Jakarta Sans', sans-serif", size: 11 }, color: '#64748b' }
                    },
                    y: {
                        grid: { color: '#f1f5f9', drawBorder: false },
                        ticks: {
                            font: { family: "'Plus Jakarta Sans', sans-serif", size: 11 },
                            color: '#64748b',
                            callback: function (val) {
                                if (val >= 1000) return (val / 1000).toFixed(0) + 'K';
                                return val;
                            }
                        }
                    }
                }
            }
        });

        // Load 30d dynamically on start
        fetchFollowerGrowthData('30d');
    }

    // Dynamic Chart Data Fetcher
    function fetchFollowerGrowthData(period) {
        fetch(`/api/dashboard/charts/follower-growth?period=${encodeURIComponent(period)}`)
            .then(res => res.json())
            .then(data => {
                if (followerGrowthChart && data.labels) {
                    followerGrowthChart.data.labels = data.labels;
                    followerGrowthChart.data.datasets[0].data = data.currentPeriod;
                    followerGrowthChart.data.datasets[1].data = data.previousPeriod;
                    followerGrowthChart.update('active');
                }
            })
            .catch(err => console.warn('Could not fetch chart data:', err));
    }

    // Timeframe selector buttons interaction
    const timeframeButtons = document.querySelectorAll('#timeframeSelector .time-tab');
    timeframeButtons.forEach(button => {
        button.addEventListener('click', function () {
            timeframeButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            const period = this.getAttribute('data-period');
            fetchFollowerGrowthData(period);
        });
    });

    // -------------------------------------------------------------------------
    // 2. Chart.js: Dynamic Engagement Overview Bar Chart
    // -------------------------------------------------------------------------
    const engagementCanvas = document.getElementById('engagementOverviewChart');

    if (engagementCanvas && typeof Chart !== 'undefined') {
        const engCtx = engagementCanvas.getContext('2d');

        const engChart = new Chart(engCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Likes',
                        data: [],
                        backgroundColor: '#6366f1',
                        borderRadius: 6,
                        barPercentage: 0.7,
                        categoryPercentage: 0.8
                    },
                    {
                        label: 'Comments',
                        data: [],
                        backgroundColor: '#0ea5e9',
                        borderRadius: 6,
                        barPercentage: 0.7,
                        categoryPercentage: 0.8
                    },
                    {
                        label: 'Shares',
                        data: [],
                        backgroundColor: '#10b981',
                        borderRadius: 6,
                        barPercentage: 0.7,
                        categoryPercentage: 0.8
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#ffffff',
                        bodyColor: '#cbd5e1',
                        padding: 12,
                        cornerRadius: 8,
                        callbacks: {
                            label: function (context) {
                                return ` ${context.dataset.label}: ${new Intl.NumberFormat().format(context.parsed.y)}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        stacked: false,
                        grid: { display: false, drawBorder: false },
                        ticks: { font: { family: "'Plus Jakarta Sans', sans-serif", size: 11 }, color: '#64748b' }
                    },
                    y: {
                        stacked: false,
                        grid: { color: '#f1f5f9', drawBorder: false },
                        ticks: {
                            font: { family: "'Plus Jakarta Sans', sans-serif", size: 11 },
                            color: '#64748b',
                            callback: function (val) {
                                if (val >= 1000) return (val / 1000).toFixed(0) + 'K';
                                return val;
                            }
                        }
                    }
                }
            }
        });

        // Fetch dynamic weekly numbers
        fetch('/api/dashboard/charts/engagement')
            .then(r => r.json())
            .then(data => {
                if (data && data.labels) {
                    engChart.data.labels = data.labels;
                    engChart.data.datasets[0].data = data.likes;
                    engChart.data.datasets[1].data = data.comments;
                    engChart.data.datasets[2].data = data.shares;
                    engChart.update();
                }
            })
            .catch(() => {});
    }

    // -------------------------------------------------------------------------
    // 3. Live Stats Synchronization (Live Sync Button)
    // -------------------------------------------------------------------------
    const refreshStatsBtn = document.getElementById('refreshStatsBtn');

    function syncLiveStats() {
        if (refreshStatsBtn) {
            refreshStatsBtn.classList.add('syncing');
            const icon = refreshStatsBtn.querySelector('svg');
            if (icon) icon.classList.add('spin');
        }

        fetch('/api/dashboard/stats')
            .then(res => res.json())
            .then(stats => {
                const totalFollowersEl = document.getElementById('statTotalFollowers');
                const totalEngagementEl = document.getElementById('statTotalEngagement');
                const publishedPostsEl = document.getElementById('statPublishedPosts');
                const scheduledPostsEl = document.getElementById('statScheduledPosts');
                const queueBadgeText = document.getElementById('queueBadgeText');
                const netGrowthBadge = document.getElementById('netGrowthBadge');
                const statOrganic = document.getElementById('statOrganic');
                const statPaid = document.getElementById('statPaid');
                const statPeak = document.getElementById('statPeak');
                const statLikes = document.getElementById('statLikes');
                const statComments = document.getElementById('statComments');
                const statShares = document.getElementById('statShares');

                if (totalFollowersEl && stats.totalFollowers) totalFollowersEl.textContent = stats.totalFollowers;
                if (totalEngagementEl && stats.totalEngagement) totalEngagementEl.textContent = stats.totalEngagement;
                if (publishedPostsEl) publishedPostsEl.textContent = stats.publishedPosts;
                if (scheduledPostsEl) scheduledPostsEl.textContent = stats.scheduledPosts;
                if (queueBadgeText) queueBadgeText.textContent = `${stats.scheduledPosts} Queued`;
                if (netGrowthBadge && stats.netGrowth) netGrowthBadge.textContent = stats.netGrowth;
                if (statOrganic && stats.organicGrowth) statOrganic.textContent = stats.organicGrowth;
                if (statPaid && stats.paidGrowth) statPaid.textContent = stats.paidGrowth;
                if (statPeak && stats.peakDay) statPeak.textContent = stats.peakDay;
                if (statLikes && stats.totalLikes) statLikes.textContent = stats.totalLikes;
                if (statComments && stats.totalComments) statComments.textContent = stats.totalComments;
                if (statShares && stats.totalShares) statShares.textContent = stats.totalShares;

                showToast('Live metrics synchronized successfully!', 'success');
            })
            .catch(err => {
                console.error('Failed to sync live stats:', err);
                showToast('Failed to sync live stats.', 'error');
            })
            .finally(() => {
                if (refreshStatsBtn) {
                    refreshStatsBtn.classList.remove('syncing');
                    const icon = refreshStatsBtn.querySelector('svg');
                    if (icon) icon.classList.remove('spin');
                }
            });
    }

    if (refreshStatsBtn) {
        refreshStatsBtn.addEventListener('click', syncLiveStats);
    }

    // -------------------------------------------------------------------------
    // 4. Dynamic Create & Schedule Post (Persistence via REST API)
    // -------------------------------------------------------------------------
    const createPostForm = document.getElementById('createPostForm');
    const modalSubmitBtn = document.getElementById('modalSubmitBtn');
    const scheduledItemsContainer = document.getElementById('scheduledItemsContainer');
    const recentPostsContainer = document.getElementById('recentPostsContainer');

    if (createPostForm) {
        createPostForm.addEventListener('submit', function (e) {
            e.preventDefault();

            const checkedBoxes = createPostForm.querySelectorAll('input[name="channels"]:checked');
            const channels = Array.from(checkedBoxes).map(cb => cb.value);
            if (channels.length === 0) {
                showToast('Please select at least one social channel.', 'error');
                return;
            }

            const content = document.getElementById('postContentInput').value.trim();
            if (!content) {
                showToast('Please enter post content.', 'error');
                return;
            }

            const publishOption = document.getElementById('publishOptionSelect').value;
            const scheduledDateTime = document.getElementById('scheduleDateTimeInput').value;
            const selectedMediaRadio = createPostForm.querySelector('input[name="mediaType"]:checked');
            const mediaType = selectedMediaRadio ? selectedMediaRadio.value : 'Single Image';
            const mediaUrlInput = document.getElementById('postMediaUrlInput');
            const mediaUrl = mediaUrlInput ? mediaUrlInput.value : '';

            // Post Reminder Preferences (Email & SMS/MSG)
            const remindEmailCb = document.getElementById('postRemindEmailCheckbox');
            const remindSmsCb = document.getElementById('postRemindSmsCheckbox');
            const timingSelect = document.getElementById('postReminderTimingSelect');
            const contactInput = document.getElementById('postRecipientContactInput');

            const remindEmail = remindEmailCb ? remindEmailCb.checked : true;
            const remindSms = remindSmsCb ? remindSmsCb.checked : true;
            const reminderMinutes = timingSelect ? parseInt(timingSelect.value, 10) : 15;
            const contactVal = contactInput ? contactInput.value.trim() : '';

            const payload = {
                channels: channels,
                content: content,
                publishOption: publishOption,
                scheduledDateTime: scheduledDateTime,
                mediaType: mediaType,
                mediaUrl: mediaUrl,
                remindEmail: remindEmail,
                remindSms: remindSms,
                reminderMinutesBefore: reminderMinutes,
                recipientEmail: (contactVal && contactVal.includes('@')) ? contactVal : null,
                recipientPhone: (contactVal && !contactVal.includes('@')) ? contactVal : null
            };

            const submitOriginalText = modalSubmitBtn.innerHTML;
            modalSubmitBtn.disabled = true;
            modalSubmitBtn.innerHTML = `<span>Saving...</span>`;

            fetch('/api/dashboard/posts', {
                method: 'POST',
                headers: getCsrfHeaders(),
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (!res.ok) throw new Error('Server returned ' + res.status);
                return res.json();
            })
            .then(data => {
                if (data.success && data.posts) {
                    showToast(data.message || 'Post saved successfully!', 'success');

                    // If scheduled, dynamically insert new cards into Scheduled list
                    if (publishOption === 'schedule' && scheduledItemsContainer) {
                        const emptyCard = scheduledItemsContainer.querySelector('.empty-state-card');
                        if (emptyCard) emptyCard.remove();
                        data.posts.forEach(post => {
                            const newCard = createScheduledPostCardHtml(post);
                            scheduledItemsContainer.insertAdjacentHTML('afterbegin', newCard);
                            bindScheduledCardActions(post.id);
                        });
                    }

                    // If published now, dynamically prepend to Recent Posts list
                    if (publishOption === 'now' && recentPostsContainer) {
                        const emptyCard = recentPostsContainer.querySelector('.empty-state-card');
                        if (emptyCard) emptyCard.remove();
                        data.posts.forEach(post => {
                            const newPostRow = createRecentPostRowHtml(post);
                            recentPostsContainer.insertAdjacentHTML('afterbegin', newPostRow);
                        });
                    }

                    // Update live metric counts
                    if (data.stats) {
                        const schedCount = document.getElementById('statScheduledPosts');
                        const pubCount = document.getElementById('statPublishedPosts');
                        const scheduledBadgeCount = document.getElementById('scheduledBadgeCount');
                        const queueBadgeText = document.getElementById('queueBadgeText');

                        if (schedCount) schedCount.textContent = data.stats.scheduledPosts;
                        if (pubCount) pubCount.textContent = data.stats.publishedPosts;
                        if (scheduledBadgeCount) scheduledBadgeCount.textContent = `${data.stats.scheduledPosts} in queue`;
                        if (queueBadgeText) queueBadgeText.textContent = `${data.stats.scheduledPosts} Queued`;
                    }

                    // Close modal and reset form
                    closeCreateModal();
                    createPostForm.reset();
                    const charCountSpan = document.getElementById('charCountSpan');
                    if (charCountSpan) charCountSpan.textContent = '0 / 2,200 chars';
                }
            })
            .catch(err => {
                console.error('Error saving post:', err);
                showToast('Failed to save post. Please try again.', 'error');
            })
            .finally(() => {
                modalSubmitBtn.disabled = false;
                modalSubmitBtn.innerHTML = submitOriginalText;
            });
        });
    }

    // Helper: Build HTML for newly added Scheduled Post Card
    function createScheduledPostCardHtml(post) {
        return `
            <div class="scheduled-item-card" id="scheduled-card-${post.id}">
                <div class="scheduled-top-row">
                    <div class="scheduled-platform-info">
                        <div class="mini-platform-icon icon-${post.platformClass}">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                                <circle cx="12" cy="12" r="10"></circle>
                            </svg>
                        </div>
                        <span class="scheduled-handle">${escapeHtml(post.accountHandle || '@codemetric')}</span>
                    </div>
                    <div class="scheduled-countdown-pill status-scheduled">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                        <span>Scheduled</span>
                    </div>
                </div>
                <p class="scheduled-caption">${escapeHtml(post.caption)}</p>
                ${post.mediaUrl ? `
                    <div style="margin: 8px 0; border-radius: var(--radius-sm); overflow: hidden; max-height: 120px; background: #000;">
                        ${(post.mediaType && post.mediaType.toLowerCase().includes('video')) || post.mediaUrl.startsWith('data:video') || post.mediaUrl.includes('.mp4') ?
                            `<video src="${escapeHtml(post.mediaUrl)}" controls style="width: 100%; max-height: 120px; object-fit: cover; display: block;"></video>` :
                            `<img src="${escapeHtml(post.mediaUrl)}" alt="Attached media" style="width: 100%; height: 120px; object-fit: cover; display: block;">`}
                    </div>
                ` : ''}
                <div class="scheduled-bottom-row">
                    <div class="scheduled-meta">
                        <span class="scheduled-date-badge">${escapeHtml(post.formattedScheduledDate || 'Scheduled')}</span>
                        <span class="scheduled-format">${escapeHtml(post.mediaType || 'Image')}</span>
                    </div>
                    <div class="scheduled-actions">
                        <button type="button" class="btn-icon-subtle action-publish-now" data-id="${post.id}" title="Publish immediately">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        </button>
                        <button type="button" class="btn-icon-subtle action-delete-post" data-id="${post.id}" title="Remove from queue">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    // Helper: Build HTML for newly added Recent Post Row
    function createRecentPostRowHtml(post) {
        return `
            <div class="post-item-row" data-platform="${post.platformClass}" id="post-row-${post.id}">
                <div class="post-platform-badge badge-${post.platformClass}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="19" height="19">
                        <circle cx="12" cy="12" r="10"></circle>
                    </svg>
                </div>
                <div class="post-main-content">
                    <div class="post-header-line">
                        <h4 class="post-title">${escapeHtml(post.title)}</h4>
                        <span class="post-time">Just now</span>
                    </div>
                    <p class="post-caption">${escapeHtml(post.caption)}</p>
                    ${post.mediaUrl ? `
                        <div style="margin: 8px 0; max-width: 280px; border-radius: var(--radius-sm); overflow: hidden; background: #000;">
                            ${(post.mediaType && post.mediaType.toLowerCase().includes('video')) || post.mediaUrl.startsWith('data:video') || post.mediaUrl.includes('.mp4') ?
                                `<video src="${escapeHtml(post.mediaUrl)}" controls style="width: 100%; max-height: 140px; object-fit: cover; display: block;"></video>` :
                                `<img src="${escapeHtml(post.mediaUrl)}" alt="Attached media" style="width: 100%; max-height: 140px; object-fit: cover; display: block;">`}
                        </div>
                    ` : ''}
                    <div class="post-tags-row">
                        <span class="media-tag">${escapeHtml(post.mediaType || 'Update')}</span>
                        <span class="status-pill-green">PUBLISHED</span>
                    </div>
                </div>
                <div class="post-metrics-cluster">
                    <div class="stat-cell" title="Likes">
                        <span class="stat-num">${post.formattedLikes || (post.likesCount ? new Intl.NumberFormat().format(post.likesCount) : '0')}</span>
                    </div>
                    <div class="stat-cell" title="Comments">
                        <span class="stat-num">${post.formattedComments || (post.commentsCount ? new Intl.NumberFormat().format(post.commentsCount) : '0')}</span>
                    </div>
                    <div class="stat-cell" title="Shares">
                        <span class="stat-num">${post.formattedShares || (post.sharesCount ? new Intl.NumberFormat().format(post.sharesCount) : '0')}</span>
                    </div>
                    <div class="stat-cell engagement-pill-cell" title="Engagement Rate">
                        <span class="eng-rate-badge">${post.engagementRate ? post.engagementRate + '%' : '0.0%'}</span>
                    </div>
                </div>
            </div>
        `;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // -------------------------------------------------------------------------
    // 5. Actions: Quick Publish Now & Delete Post
    // -------------------------------------------------------------------------
    function bindScheduledCardActions(postId) {
        const card = document.getElementById(`scheduled-card-${postId}`);
        if (!card) return;

        const publishBtn = card.querySelector('.action-publish-now');
        if (publishBtn) {
            publishBtn.addEventListener('click', function () {
                const id = this.getAttribute('data-id');
                fetch(`/api/dashboard/posts/${id}/publish`, {
                    method: 'POST',
                    headers: getCsrfHeaders()
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        showToast('Post published live to feed!', 'success');
                        card.remove();
                        syncLiveStats();
                    }
                })
                .catch(() => showToast('Failed to publish post.', 'error'));
            });
        }

        const deleteBtn = card.querySelector('.action-delete-post');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', function () {
                const id = this.getAttribute('data-id');
                if (!confirm('Are you sure you want to remove this post from schedule?')) return;

                fetch(`/api/dashboard/posts/${id}`, {
                    method: 'DELETE',
                    headers: getCsrfHeaders()
                })
                .then(r => r.json())
                .then(res => {
                    if (res.success) {
                        showToast('Post removed from queue.', 'success');
                        card.remove();
                        syncLiveStats();
                    }
                })
                .catch(() => showToast('Failed to delete post.', 'error'));
            });
        }
    }

    // Bind existing scheduled action buttons
    document.querySelectorAll('.scheduled-item-card').forEach(card => {
        const id = card.id.replace('scheduled-card-', '');
        if (id) bindScheduledCardActions(id);
    });

    // -------------------------------------------------------------------------
    // 6. Recent Posts Platform Filter
    // -------------------------------------------------------------------------
    const filterButtons = document.querySelectorAll('#recentPostsFilter .filter-pill');
    const postRows = document.querySelectorAll('.post-item-row');

    filterButtons.forEach(button => {
        button.addEventListener('click', function () {
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            const filterValue = this.getAttribute('data-filter');

            document.querySelectorAll('.post-item-row').forEach(row => {
                const platform = row.getAttribute('data-platform');
                if (filterValue === 'all' || platform === filterValue) {
                    row.classList.remove('hidden');
                } else {
                    row.classList.add('hidden');
                }
            });
        });
    });

    // -------------------------------------------------------------------------
    // 7. Responsive Mobile Sidebar Toggle & Backdrop
    // -------------------------------------------------------------------------
    const sidebar = document.getElementById('appSidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarCloseBtn');
    const backdrop = document.getElementById('sidebarBackdrop');

    function openMobileSidebar() {
        if (sidebar && backdrop) {
            sidebar.classList.add('mobile-open');
            backdrop.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeMobileSidebar() {
        if (sidebar && backdrop) {
            sidebar.classList.remove('mobile-open');
            backdrop.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    if (sidebarToggle) sidebarToggle.addEventListener('click', openMobileSidebar);
    if (sidebarClose) sidebarClose.addEventListener('click', closeMobileSidebar);
    if (backdrop) backdrop.addEventListener('click', closeMobileSidebar);

    // -------------------------------------------------------------------------
    // 8. Notifications Dropdown Toggle
    // -------------------------------------------------------------------------
    const notificationBtn = document.getElementById('notificationBtn');
    const notificationsMenu = document.getElementById('notificationsMenu');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const notifBadge = document.querySelector('.notification-indicator-badge');

    if (notificationBtn && notificationsMenu) {
        notificationBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            if (profileMenu) profileMenu.classList.remove('show');
            notificationsMenu.classList.toggle('show');
            const isOpen = notificationsMenu.classList.contains('show');
            notificationBtn.setAttribute('aria-expanded', isOpen);
        });
    }

    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', function () {
            fetch('/api/dashboard/notifications/mark-read', {
                method: 'PUT',
                headers: getCsrfHeaders()
            })
            .then(r => r.json())
            .then(data => {
                document.querySelectorAll('.notification-item.unread').forEach(item => {
                    item.classList.remove('unread');
                });
                if (notifBadge) notifBadge.style.display = 'none';
                const subtitle = document.querySelector('.notifications-menu .dropdown-subtitle');
                if (subtitle) subtitle.textContent = '0 unread updates';
                showToast(data.message || 'All notifications marked as read', 'success');
            })
            .catch(() => {
                showToast('Failed to mark notifications as read', 'error');
            });
        });
    }

    const clearAllNotificationsBtn = document.getElementById('clearAllNotificationsBtn');
    const notifListContainer = document.getElementById('notificationsListContainer') || document.querySelector('.notifications-list');

    // Handler: Delete Single Notification Permanently
    if (notificationsMenu) {
        notificationsMenu.addEventListener('click', function (e) {
            const deleteBtn = e.target.closest('.notif-delete-btn');
            if (!deleteBtn) return;
            e.stopPropagation();

            const notifId = deleteBtn.getAttribute('data-id');
            const notifItem = deleteBtn.closest('.notification-item');
            if (!notifId || !notifItem) return;

            deleteBtn.disabled = true;
            deleteBtn.style.opacity = '0.3';

            fetch(`/api/dashboard/notifications/${notifId}`, {
                method: 'DELETE',
                headers: getCsrfHeaders()
            })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    notifItem.style.transition = 'all 0.25s ease';
                    notifItem.style.opacity = '0';
                    notifItem.style.transform = 'translateX(20px)';
                    setTimeout(() => {
                        notifItem.remove();
                        updateNotificationBadge(data.unreadCount);

                        const remaining = notificationsMenu.querySelectorAll('.notification-item');
                        if (remaining.length === 0 && notifListContainer) {
                            notifListContainer.innerHTML = '<div class="notif-empty-state" style="padding: 24px; text-align: center; color: var(--text-muted); font-size: 13px;">No notifications right now</div>';
                        }
                    }, 250);
                    showToast('Notification permanently deleted', 'success');
                } else {
                    deleteBtn.disabled = false;
                    deleteBtn.style.opacity = '1';
                    showToast(data.message || 'Failed to delete notification', 'error');
                }
            })
            .catch(err => {
                deleteBtn.disabled = false;
                deleteBtn.style.opacity = '1';
                showToast('Network error while deleting notification', 'error');
            });
        });
    }

    // Handler: Clear ALL Notifications Permanently from Database
    if (clearAllNotificationsBtn) {
        clearAllNotificationsBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            if (!confirm('Are you sure you want to permanently delete all notifications from the database?')) {
                return;
            }

            clearAllNotificationsBtn.disabled = true;
            clearAllNotificationsBtn.style.opacity = '0.5';

            fetch('/api/dashboard/notifications', {
                method: 'DELETE',
                headers: getCsrfHeaders()
            })
            .then(r => r.json())
            .then(data => {
                clearAllNotificationsBtn.disabled = false;
                clearAllNotificationsBtn.style.opacity = '1';
                if (data.success) {
                    if (notifListContainer) {
                        notifListContainer.innerHTML = '<div class="notif-empty-state" style="padding: 24px; text-align: center; color: var(--text-muted); font-size: 13px;">No notifications right now</div>';
                    }
                    updateNotificationBadge(0);
                    showToast('All notifications permanently deleted from database', 'success');
                } else {
                    showToast('Failed to clear notifications', 'error');
                }
            })
            .catch(err => {
                clearAllNotificationsBtn.disabled = false;
                clearAllNotificationsBtn.style.opacity = '1';
                showToast('Network error while clearing notifications', 'error');
            });
        });
    }

    function updateNotificationBadge(unreadCount) {
        const count = typeof unreadCount === 'number' ? unreadCount : 0;
        if (notifBadge) {
            if (count > 0) {
                notifBadge.textContent = count;
                notifBadge.style.display = 'inline-flex';
            } else {
                notifBadge.style.display = 'none';
            }
        }
        const subtitle = document.querySelector('.notifications-menu .dropdown-subtitle');
        if (subtitle) {
            subtitle.textContent = count + ' unread updates';
        }
    }

    // -------------------------------------------------------------------------
    // 9. User Profile Dropdown Toggle
    // -------------------------------------------------------------------------
    const userProfileBtn = document.getElementById('userProfileBtn');
    const profileMenu = document.getElementById('profileMenu');

    if (userProfileBtn && profileMenu) {
        userProfileBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            if (notificationsMenu) notificationsMenu.classList.remove('show');
            profileMenu.classList.toggle('show');
            const isOpen = profileMenu.classList.contains('show');
            userProfileBtn.setAttribute('aria-expanded', isOpen);
        });
    }

    document.addEventListener('click', function (e) {
        if (notificationsMenu && !notificationsMenu.contains(e.target) && e.target !== notificationBtn) {
            notificationsMenu.classList.remove('show');
            if (notificationBtn) notificationBtn.setAttribute('aria-expanded', 'false');
        }
        if (profileMenu && !profileMenu.contains(e.target) && !userProfileBtn.contains(e.target)) {
            profileMenu.classList.remove('show');
            if (userProfileBtn) userProfileBtn.setAttribute('aria-expanded', 'false');
        }
    });

    // -------------------------------------------------------------------------
    // 10. Global Search & In-Page Filter (Ctrl + K / Cmd + K)
    // -------------------------------------------------------------------------
    const globalSearchInput = document.getElementById('globalSearchInput');
    const searchDropdown = document.getElementById('searchResultsDropdown');
    let searchDebounceTimer = null;
    let selectedSearchIndex = -1;

    function closeSearchDropdown() {
        if (searchDropdown) {
            searchDropdown.style.display = 'none';
            searchDropdown.innerHTML = '';
        }
        selectedSearchIndex = -1;
    }

    if (globalSearchInput && searchDropdown) {
        globalSearchInput.addEventListener('input', function () {
            const query = this.value.trim();

            clearTimeout(searchDebounceTimer);

            // In-page live filtering for post items on Dashboard / Content pages
            const postRows = document.querySelectorAll('.post-item-row');
            if (postRows.length > 0) {
                const qLower = query.toLowerCase();
                postRows.forEach(row => {
                    const text = row.textContent.toLowerCase();
                    if (!query || text.includes(qLower)) {
                        row.classList.remove('hidden');
                    } else {
                        row.classList.add('hidden');
                    }
                });
            }

            if (query.length < 2) {
                closeSearchDropdown();
                return;
            }

            searchDebounceTimer = setTimeout(() => {
                fetch(`/api/dashboard/search?q=${encodeURIComponent(query)}`)
                    .then(res => res.json())
                    .then(data => renderSearchResults(data, query))
                    .catch(err => console.warn('Search error:', err));
            }, 200);
        });

        globalSearchInput.addEventListener('keydown', function (e) {
            const items = searchDropdown.querySelectorAll('.search-result-item');
            if (items.length === 0) return;

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                selectedSearchIndex = (selectedSearchIndex + 1) % items.length;
                updateActiveSearchItem(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                selectedSearchIndex = (selectedSearchIndex - 1 + items.length) % items.length;
                updateActiveSearchItem(items);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (selectedSearchIndex >= 0 && items[selectedSearchIndex]) {
                    items[selectedSearchIndex].click();
                } else if (items.length > 0) {
                    items[0].click();
                }
            }
        });

        document.addEventListener('click', function (e) {
            if (!globalSearchInput.contains(e.target) && !searchDropdown.contains(e.target)) {
                closeSearchDropdown();
            }
        });
    }

    function updateActiveSearchItem(items) {
        items.forEach((item, idx) => {
            if (idx === selectedSearchIndex) {
                item.classList.add('active');
                item.scrollIntoView({ block: 'nearest' });
            } else {
                item.classList.remove('active');
            }
        });
    }

    function renderSearchResults(data, query) {
        if (!searchDropdown) return;

        if (!data || data.totalResults === 0) {
            searchDropdown.innerHTML = `
                <div class="search-empty-state">
                    No results found for "<strong>${escapeHtml(query)}</strong>"
                </div>
            `;
            searchDropdown.style.display = 'block';
            return;
        }

        let html = '';

        // 1. Pages
        if (data.pages && data.pages.length > 0) {
            html += `<div class="search-category-header">Pages &amp; Navigation</div>`;
            data.pages.forEach(p => {
                html += `
                    <a href="${p.url}" class="search-result-item">
                        <div class="search-result-icon">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <polyline points="12 6 12 12 14 14"></polyline>
                            </svg>
                        </div>
                        <div class="search-result-body">
                            <div class="search-result-title">${escapeHtml(p.title)}</div>
                            <div class="search-result-subtitle">${escapeHtml(p.url)}</div>
                        </div>
                    </a>
                `;
            });
        }

        // 2. Accounts
        if (data.accounts && data.accounts.length > 0) {
            html += `<div class="search-category-header">Social Channels</div>`;
            data.accounts.forEach(a => {
                html += `
                    <a href="/accounts" class="search-result-item">
                        <div class="search-result-icon">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path>
                                <rect x="2" y="9" width="4" height="12"></rect>
                                <circle cx="4" cy="4" r="2"></circle>
                            </svg>
                        </div>
                        <div class="search-result-body">
                            <div class="search-result-title">${escapeHtml(a.platform)} (${escapeHtml(a.handle)})</div>
                            <div class="search-result-subtitle">${escapeHtml(a.followers)} Followers</div>
                        </div>
                        <span class="status-pill-green" style="font-size: 10px; padding: 2px 8px;">CONNECTED</span>
                    </a>
                `;
            });
        }

        // 3. Posts
        if (data.posts && data.posts.length > 0) {
            html += `<div class="search-category-header">Social Posts &amp; Updates</div>`;
            data.posts.forEach(post => {
                const statusBadge = post.status === 'PUBLISHED'
                    ? `<span class="status-pill-green" style="font-size: 10px; padding: 2px 8px;">PUBLISHED</span>`
                    : `<span class="badge-pill-amber" style="font-size: 10px; padding: 2px 8px; border-radius: 999px;">SCHEDULED</span>`;

                html += `
                    <a href="/content" class="search-result-item">
                        <div class="search-result-icon">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="17" y1="10" x2="3" y2="10"></line>
                                <line x1="21" y1="6" x2="3" y2="6"></line>
                                <line x1="21" y1="14" x2="3" y2="14"></line>
                                <line x1="17" y1="18" x2="3" y2="18"></line>
                            </svg>
                        </div>
                        <div class="search-result-body">
                            <div class="search-result-title">${escapeHtml(post.title || post.platform)}</div>
                            <div class="search-result-subtitle">${escapeHtml(post.caption)}</div>
                        </div>
                        <div class="search-result-meta">${statusBadge}</div>
                    </a>
                `;
            });
        }

        searchDropdown.innerHTML = html;
        searchDropdown.style.display = 'block';
        selectedSearchIndex = -1;
    }

    document.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            if (globalSearchInput) {
                globalSearchInput.focus();
                globalSearchInput.select();
            }
        }
        if (e.key === 'Escape') {
            closeSearchDropdown();
            closeMobileSidebar();
            if (notificationsMenu) notificationsMenu.classList.remove('show');
            if (profileMenu) profileMenu.classList.remove('show');
            closeCreateModal();
        }
    });

    // -------------------------------------------------------------------------
    // 11. Create Post Modal Controls
    // -------------------------------------------------------------------------
    const createPostModal = document.getElementById('createPostModal');
    const createPostBtn = document.getElementById('createPostBtn');
    const schedulePostBannerBtn = document.getElementById('schedulePostBannerBtn');
    const quickAddPostBtn = document.getElementById('quickAddPostBtn');
    const viewFullQueueBtn = document.getElementById('viewFullQueueBtn');
    const modalCloseBtn = document.getElementById('modalCloseBtn');
    const modalCancelBtn = document.getElementById('modalCancelBtn');
    const postContentInput = document.getElementById('postContentInput');
    const charCountSpan = document.getElementById('charCountSpan');

    function openCreateModal() {
        if (createPostModal) {
            createPostModal.classList.add('show');
            createPostModal.setAttribute('aria-hidden', 'false');
            document.body.style.overflow = 'hidden';
            if (postContentInput) {
                setTimeout(() => postContentInput.focus(), 150);
            }
        }
    }

    function closeCreateModal() {
        if (createPostModal) {
            createPostModal.classList.remove('show');
            createPostModal.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
            if (typeof window.clearMediaAttachment === 'function') {
                window.clearMediaAttachment();
            }
        }
    }

    if (createPostBtn) createPostBtn.addEventListener('click', openCreateModal);
    if (schedulePostBannerBtn) schedulePostBannerBtn.addEventListener('click', openCreateModal);
    if (quickAddPostBtn) quickAddPostBtn.addEventListener('click', openCreateModal);
    if (viewFullQueueBtn) viewFullQueueBtn.addEventListener('click', openCreateModal);
    if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeCreateModal);
    if (modalCancelBtn) modalCancelBtn.addEventListener('click', closeCreateModal);

    if (createPostModal) {
        createPostModal.addEventListener('click', function (e) {
            if (e.target === createPostModal) {
                closeCreateModal();
            }
        });
    }

    // Live character counter
    if (postContentInput && charCountSpan) {
        postContentInput.addEventListener('input', function () {
            const currentLen = this.value.length;
            charCountSpan.textContent = `${new Intl.NumberFormat().format(currentLen)} / 2,200 chars`;
            if (currentLen > 2200) {
                charCountSpan.style.color = 'var(--danger)';
            } else {
                charCountSpan.style.color = 'var(--text-muted)';
            }
        });
    }

    // Channel check pills toggle styling
    const channelPills = document.querySelectorAll('.channel-check-pill');
    channelPills.forEach(pill => {
        const checkbox = pill.querySelector('input[type="checkbox"]');
        if (checkbox) {
            checkbox.addEventListener('change', function () {
                if (this.checked) {
                    pill.classList.add('selected');
                } else {
                    pill.classList.remove('selected');
                }
            });
        }
    });

    // -------------------------------------------------------------------------
    // 12. Media Upload & Attachment Controller (Image & Video)
    // -------------------------------------------------------------------------
    function initMediaAttachment() {
        const mediaRadios = document.querySelectorAll('input[name="mediaType"]');
        const mediaBox = document.getElementById('mediaUploadBox');
        const mediaFileInput = document.getElementById('mediaFileInput');
        const browseBtn = document.getElementById('browseMediaBtn');
        const postMediaUrlInput = document.getElementById('postMediaUrlInput');
        const mediaPrompt = document.getElementById('mediaUploadPrompt');
        const mediaPreview = document.getElementById('mediaPreviewWrapper');
        const imgPreview = document.getElementById('imagePreviewElement');
        const videoPreview = document.getElementById('videoPreviewElement');
        const removeMediaBtn = document.getElementById('removeMediaBtn');
        const mediaTypeLabel = document.getElementById('mediaTypeLabel');
        const mediaFormatHint = document.getElementById('mediaFormatHint');
        const mediaUploadIcon = document.getElementById('mediaUploadIcon');
        const mediaUrlDirectInput = document.getElementById('mediaUrlDirectInput');
        const applyMediaUrlBtn = document.getElementById('applyMediaUrlBtn');

        if (!mediaBox || !mediaFileInput) return;

        window.clearMediaAttachment = function() {
            if (postMediaUrlInput) postMediaUrlInput.value = '';
            if (mediaFileInput) mediaFileInput.value = '';
            if (mediaUrlDirectInput) mediaUrlDirectInput.value = '';
            if (imgPreview) {
                imgPreview.src = '';
                imgPreview.style.display = 'none';
            }
            if (videoPreview) {
                videoPreview.src = '';
                videoPreview.style.display = 'none';
                try { videoPreview.pause(); } catch(e){}
            }
            if (mediaPrompt) mediaPrompt.style.display = 'flex';
            if (mediaPreview) mediaPreview.style.display = 'none';
        };

        function updateMediaMode(type) {
            document.querySelectorAll('.media-btn').forEach(btn => {
                const radio = btn.querySelector('input[name="mediaType"]');
                if (radio && radio.value === type) {
                    btn.classList.add('active');
                    radio.checked = true;
                } else {
                    btn.classList.remove('active');
                }
            });

            if (type === 'Text Update') {
                mediaBox.style.display = 'none';
                window.clearMediaAttachment();
                return;
            }

            mediaBox.style.display = 'block';

            if (type.includes('Video')) {
                mediaFileInput.accept = 'video/mp4,video/webm,video/quicktime,video/*';
                if (mediaTypeLabel) mediaTypeLabel.textContent = 'Video';
                if (mediaFormatHint) mediaFormatHint.textContent = 'Supports MP4, WebM, MOV';
                if (mediaUploadIcon) {
                    mediaUploadIcon.innerHTML = `
                        <svg viewBox="0 0 24 24" width="30" height="30" fill="none" stroke="currentColor" stroke-width="1.8">
                            <polygon points="23 7 16 12 23 17 23 7"></polygon>
                            <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
                        </svg>`;
                }
            } else {
                mediaFileInput.accept = 'image/png,image/jpeg,image/webp,image/gif,image/*';
                if (mediaTypeLabel) mediaTypeLabel.textContent = type.includes('Carousel') ? 'Carousel Slide' : 'Image';
                if (mediaFormatHint) mediaFormatHint.textContent = 'Supports PNG, JPG, WEBP, GIF (Max 25MB)';
                if (mediaUploadIcon) {
                    mediaUploadIcon.innerHTML = `
                        <svg viewBox="0 0 24 24" width="30" height="30" fill="none" stroke="currentColor" stroke-width="1.8">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                            <polyline points="21 15 16 10 5 21"></polyline>
                        </svg>`;
                }
            }
        }

        mediaRadios.forEach(radio => {
            const parentLabel = radio.closest('.media-btn');
            if (parentLabel) {
                parentLabel.addEventListener('click', function (e) {
                    updateMediaMode(radio.value);
                });
            }
        });

        if (browseBtn) {
            browseBtn.addEventListener('click', (e) => {
                e.preventDefault();
                mediaFileInput.click();
            });
        }

        mediaFileInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;

            const isVideo = file.type.startsWith('video/') || file.name.match(/\.(mp4|webm|mov)$/i);
            const reader = new FileReader();

            reader.onload = function (e) {
                displayMediaPreview(e.target.result, isVideo, file.name);
            };

            reader.readAsDataURL(file);
        });

        if (applyMediaUrlBtn && mediaUrlDirectInput) {
            applyMediaUrlBtn.addEventListener('click', function (e) {
                e.preventDefault();
                const url = mediaUrlDirectInput.value.trim();
                if (!url) return;
                const isVideo = url.match(/\.(mp4|webm|mov)(\?.*)?$/i) || url.toLowerCase().includes('video');
                displayMediaPreview(url, isVideo, 'Attached Link');
            });
        }

        mediaBox.addEventListener('dragover', function (e) {
            e.preventDefault();
            this.classList.add('drag-over');
        });
        mediaBox.addEventListener('dragleave', function (e) {
            e.preventDefault();
            this.classList.remove('drag-over');
        });
        mediaBox.addEventListener('drop', function (e) {
            e.preventDefault();
            this.classList.remove('drag-over');
            if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                const file = e.dataTransfer.files[0];
                const isVideo = file.type.startsWith('video/') || file.name.match(/\.(mp4|webm|mov)$/i);
                const reader = new FileReader();
                reader.onload = function (ev) {
                    displayMediaPreview(ev.target.result, isVideo, file.name);
                };
                reader.readAsDataURL(file);
            }
        });

        function displayMediaPreview(url, isVideo, name) {
            if (postMediaUrlInput) postMediaUrlInput.value = url;
            if (mediaPrompt) mediaPrompt.style.display = 'none';
            if (mediaPreview) mediaPreview.style.display = 'block';

            const nameBadge = document.getElementById('mediaFileNameBadge');
            if (nameBadge) nameBadge.textContent = name || (isVideo ? 'Video attached' : 'Image attached');

            if (isVideo) {
                if (imgPreview) imgPreview.style.display = 'none';
                if (videoPreview) {
                    videoPreview.src = url;
                    videoPreview.style.display = 'block';
                }
                const videoRadio = document.querySelector('input[name="mediaType"][value="Video"]');
                if (videoRadio) {
                    videoRadio.checked = true;
                    updateMediaMode('Video');
                }
            } else {
                if (videoPreview) {
                    videoPreview.style.display = 'none';
                    try { videoPreview.pause(); } catch(e){}
                }
                if (imgPreview) {
                    imgPreview.src = url;
                    imgPreview.style.display = 'block';
                }
            }
        }

        window.setComposerMedia = function(url, caption, isVideo) {
            displayMediaPreview(url, isVideo, caption);
        };

        if (removeMediaBtn) {
            removeMediaBtn.addEventListener('click', (e) => {
                e.preventDefault();
                window.clearMediaAttachment();
            });
        }

        updateMediaMode('Single Image');
    }

    // -------------------------------------------------------------------------
    // Custom User-Created Folders & Records System
    // -------------------------------------------------------------------------
    function initFoldersManager() {
        const foldersGrid = document.getElementById('foldersGrid');
        const foldersCountBadge = document.getElementById('foldersCountBadge');
        const sidebarFolderCountBadge = document.getElementById('sidebarFolderCountBadge');
        const folderSearchInput = document.getElementById('folderSearchInput');
        const btnOpenNewFolderModal = document.getElementById('btnOpenNewFolderModal');

        // Modals
        const createFolderModal = document.getElementById('createFolderModal');
        const createFolderForm = document.getElementById('createFolderForm');
        const folderModalTitle = document.getElementById('folderModalTitle');
        const folderEditId = document.getElementById('folderEditId');
        const folderNameInput = document.getElementById('folderNameInput');
        const folderDescInput = document.getElementById('folderDescInput');
        const folderSelectedColor = document.getElementById('folderSelectedColor');
        const folderSelectedIcon = document.getElementById('folderSelectedIcon');
        const folderModalCloseBtn = document.getElementById('folderModalCloseBtn');
        const folderModalCancelBtn = document.getElementById('folderModalCancelBtn');

        // Folder Detail Modal
        const folderDetailModal = document.getElementById('folderDetailModal');
        const folderDetailBanner = document.getElementById('folderDetailBanner');
        const folderDetailTitle = document.getElementById('folderDetailTitle');
        const folderDetailDesc = document.getElementById('folderDetailDesc');
        const folderDetailRecordBadge = document.getElementById('folderDetailRecordBadge');
        const folderDetailIconSlot = document.getElementById('folderDetailIconSlot');
        const folderRecordsContainer = document.getElementById('folderRecordsContainer');
        const folderDetailCloseBtn = document.getElementById('folderDetailCloseBtn');
        const folderDetailCloseFooterBtn = document.getElementById('folderDetailCloseFooterBtn');
        const btnDeleteCurrentFolder = document.getElementById('btnDeleteCurrentFolder');
        const btnExportFolderCsv = document.getElementById('btnExportFolderCsv');
        const btnToggleAddRecordPanel = document.getElementById('btnToggleAddRecordPanel');
        const addRecordPanel = document.getElementById('addRecordPanel');
        const addRecordForm = document.getElementById('addRecordForm');
        const currentFolderIdForRecord = document.getElementById('currentFolderIdForRecord');
        const btnCancelAddRecord = document.getElementById('btnCancelAddRecord');

        // Quick File Post Modal
        const quickFilePostModal = document.getElementById('quickFilePostModal');
        const quickFilePostId = document.getElementById('quickFilePostId');
        const quickFilePostTitleText = document.getElementById('quickFilePostTitleText');
        const quickFolderPickerList = document.getElementById('quickFolderPickerList');
        const quickFileCloseBtn = document.getElementById('quickFileCloseBtn');
        const quickFileCancelBtn = document.getElementById('quickFileCancelBtn');

        let allFolders = [];
        let currentActiveFolder = null;
        let currentFolderRecords = [];
        let currentRecordTypeFilter = 'all';

        function hexToRgba(hex, alpha = 0.12) {
            if (!hex || !hex.startsWith('#') || hex.length < 7) return `rgba(79, 70, 229, ${alpha})`;
            const r = parseInt(hex.slice(1, 3), 16);
            const g = parseInt(hex.slice(3, 5), 16);
            const b = parseInt(hex.slice(5, 7), 16);
            return `rgba(${r}, ${g}, ${b}, ${alpha})`;
        }

        function getFolderIconSvg(iconType) {
            switch (iconType) {
                case 'campaign':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path></svg>';
                case 'digest':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>';
                case 'review':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>';
                case 'star':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>';
                case 'folder':
                default:
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>';
            }
        }

        function getRecordTypeIconSvg(type) {
            switch ((type || '').toUpperCase()) {
                case 'POST':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>';
                case 'ACCOUNT':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle></svg>';
                case 'REPORT':
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>';
                case 'NOTE':
                case 'CAMPAIGN':
                default:
                    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>';
            }
        }

        // Fetch folders from backend API
        async function fetchFolders() {
            try {
                const res = await fetch('/api/folders');
                if (!res.ok) throw new Error('Failed to load folders');
                const data = await res.json();
                allFolders = data;
                renderFoldersGrid(allFolders);
                updateBadges();
            } catch (err) {
                console.error('Error fetching folders:', err);
            }
        }

        function updateBadges() {
            const count = allFolders.length;
            const text = `${count} ${count === 1 ? 'Folder' : 'Folders'}`;
            if (foldersCountBadge) foldersCountBadge.textContent = text;
            if (sidebarFolderCountBadge) sidebarFolderCountBadge.textContent = count;
        }

        // Render Folder Cards in Grid
        function renderFoldersGrid(folders) {
            if (!foldersGrid) return;
            foldersGrid.innerHTML = '';

            if (!folders || folders.length === 0) {
                foldersGrid.innerHTML = `
                    <div style="grid-column: 1 / -1; text-align: center; padding: 36px 20px; background: var(--bg-card); border-radius: var(--radius-lg); border: 1px dashed var(--border-color); color: var(--text-muted);">
                        <svg viewBox="0 0 24 24" width="42" height="42" fill="none" stroke="currentColor" stroke-width="1.8" style="margin-bottom: 10px; opacity: 0.6;">
                            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
                        </svg>
                        <h4 style="margin: 0 0 4px 0; font-size: 15px; color: var(--text-primary);">No custom folders yet</h4>
                        <p style="margin: 0 0 16px 0; font-size: 13px;">Create folders like "Campaigns 2026" or "Weekly Digests" to organize your dashboard.</p>
                        <button type="button" class="btn btn-primary btn-sm" id="btnEmptyCreateFolder">
                            <span>+ Create First Folder</span>
                        </button>
                    </div>
                `;
                const btnEmpty = document.getElementById('btnEmptyCreateFolder');
                if (btnEmpty) btnEmpty.addEventListener('click', openCreateFolderModal);
                return;
            }

            folders.forEach(folder => {
                const card = document.createElement('div');
                card.className = 'folder-card';
                card.dataset.folderId = folder.id;
                card.dataset.folderName = folder.name;
                card.style.setProperty('--folder-accent', folder.color);
                card.style.setProperty('--folder-tint', hexToRgba(folder.color, 0.12));

                card.innerHTML = `
                    <div class="folder-card-accent-bar"></div>
                    <div class="folder-card-top">
                        <div class="folder-icon-circle">
                            ${getFolderIconSvg(folder.icon)}
                        </div>
                        <div class="folder-actions-menu">
                            <button type="button" class="folder-menu-trigger" aria-label="Folder actions" title="Folder actions">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="12" cy="12" r="1.5"></circle>
                                    <circle cx="19" cy="12" r="1.5"></circle>
                                    <circle cx="5" cy="12" r="1.5"></circle>
                                </svg>
                            </button>
                            <div class="folder-menu-dropdown">
                                <button type="button" class="folder-menu-item action-open-folder-menu">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                                    <span>Open Folder</span>
                                </button>
                                <button type="button" class="folder-menu-item action-edit-folder-menu">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                                    <span>Edit Folder</span>
                                </button>
                                <button type="button" class="folder-menu-item action-export-folder-menu">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                    <span>Export CSV</span>
                                </button>
                                <button type="button" class="folder-menu-item danger-item action-delete-folder-menu">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                                    <span>Delete Folder</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <h4 class="folder-name-title">${escapeHtml(folder.name)}</h4>
                    <p class="folder-desc-text">${escapeHtml(folder.description || 'Custom collection of dashboard items')}</p>

                    <div class="folder-card-footer">
                        <span class="folder-records-count-pill">
                            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                            </svg>
                            <span>${folder.itemCount} ${folder.itemCount === 1 ? 'record' : 'records'}</span>
                        </span>
                        <button type="button" class="btn-open-folder">
                            <span>Open</span>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                                <polyline points="12 5 19 12 12 19"></polyline>
                            </svg>
                        </button>
                    </div>
                `;

                // Drag & Drop: Drop target events
                card.addEventListener('dragover', (e) => {
                    e.preventDefault();
                    e.dataTransfer.dropEffect = 'copy';
                    card.classList.add('drag-over');
                });

                card.addEventListener('dragleave', () => {
                    card.classList.remove('drag-over');
                });

                card.addEventListener('drop', async (e) => {
                    e.preventDefault();
                    card.classList.remove('drag-over');
                    const rawData = e.dataTransfer.getData('text/plain') || e.dataTransfer.getData('application/json');
                    if (!rawData) return;
                    let postId = null;
                    let postTitle = 'Post';
                    try {
                        const parsed = JSON.parse(rawData);
                        postId = parsed.postId;
                        postTitle = parsed.postTitle || 'Post';
                    } catch (parseErr) {
                        postId = rawData;
                    }

                    if (postId) {
                        await tagPostIntoFolder(folder.id, postId, postTitle, folder.name);
                    }
                });

                // Dropdown menu toggle
                const menuTrigger = card.querySelector('.folder-menu-trigger');
                const menuDropdown = card.querySelector('.folder-menu-dropdown');
                menuTrigger.addEventListener('click', (e) => {
                    e.stopPropagation();
                    document.querySelectorAll('.folder-menu-dropdown.show').forEach(m => {
                        if (m !== menuDropdown) m.classList.remove('show');
                    });
                    menuDropdown.classList.toggle('show');
                });

                // Menu items
                card.querySelector('.action-open-folder-menu').addEventListener('click', (e) => {
                    e.stopPropagation();
                    menuDropdown.classList.remove('show');
                    openFolderDetail(folder.id);
                });

                card.querySelector('.action-edit-folder-menu').addEventListener('click', (e) => {
                    e.stopPropagation();
                    menuDropdown.classList.remove('show');
                    openEditFolderModal(folder);
                });

                card.querySelector('.action-export-folder-menu').addEventListener('click', async (e) => {
                    e.stopPropagation();
                    menuDropdown.classList.remove('show');
                    await exportFolderToCsv(folder.id, folder.name);
                });

                card.querySelector('.action-delete-folder-menu').addEventListener('click', (e) => {
                    e.stopPropagation();
                    menuDropdown.classList.remove('show');
                    confirmAndDeleteFolder(folder.id, folder.name);
                });

                // Open button
                card.querySelector('.btn-open-folder').addEventListener('click', () => {
                    openFolderDetail(folder.id);
                });

                foldersGrid.appendChild(card);
            });
        }

        // Tag post into folder API
        async function tagPostIntoFolder(folderId, postId, postTitle, folderName) {
            try {
                const res = await fetch(`/api/folders/${folderId}/tag-post/${postId}`, {
                    method: 'POST',
                    headers: getCsrfHeaders()
                });
                const data = await res.json();
                if (data.success) {
                    showToast(`✓ Filed "${postTitle}" into "${folderName}"!`, 'success');
                    await fetchFolders();
                } else {
                    showToast(data.message || 'Could not file post', 'error');
                }
            } catch (err) {
                console.error('Error filing post:', err);
                showToast('Network error while filing post', 'error');
            }
        }

        // Setup Draggable cards on post rows and scheduled cards
        function bindDraggablePosts() {
            document.querySelectorAll('.draggable-post-item').forEach(card => {
                if (card.dataset.dragBound === 'true') return;
                card.dataset.dragBound = 'true';
                card.setAttribute('draggable', 'true');

                card.addEventListener('dragstart', (e) => {
                    const postId = card.dataset.postId || card.dataset.id;
                    const postTitle = card.dataset.postTitle || card.dataset.title || 'Social Post';
                    const payload = JSON.stringify({ postId, postTitle });
                    e.dataTransfer.setData('text/plain', payload);
                    e.dataTransfer.setData('application/json', payload);
                    e.dataTransfer.effectAllowed = 'copyMove';
                    card.classList.add('dragging');
                });

                card.addEventListener('dragend', () => {
                    card.classList.remove('dragging');
                });
            });

            // "File into Folder" quick button handler
            document.querySelectorAll('.action-file-post-btn').forEach(btn => {
                if (btn.dataset.bound === 'true') return;
                btn.dataset.bound = 'true';
                btn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const postId = btn.dataset.id;
                    const postTitle = btn.dataset.title || 'Social Post';
                    openQuickFileModal(postId, postTitle);
                });
            });
        }

        // Quick File Modal
        function openQuickFileModal(postId, postTitle) {
            if (!quickFilePostModal) return;
            quickFilePostId.value = postId;
            quickFilePostTitleText.textContent = `File "${postTitle}" into:`;
            quickFolderPickerList.innerHTML = '';

            if (allFolders.length === 0) {
                quickFolderPickerList.innerHTML = '<p style="text-align:center; color: var(--text-muted); padding: 12px;">No folders available. Create a folder first.</p>';
            } else {
                allFolders.forEach(folder => {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'quick-folder-option-btn';
                    btn.innerHTML = `
                        <div class="quick-folder-icon" style="background-color: ${folder.color};">
                            ${getFolderIconSvg(folder.icon)}
                        </div>
                        <div style="flex: 1; text-align: left;">
                            <div class="quick-folder-name">${escapeHtml(folder.name)}</div>
                            <div class="quick-folder-meta">${folder.itemCount} items &bull; ${escapeHtml(folder.description || '')}</div>
                        </div>
                    `;
                    btn.addEventListener('click', async () => {
                        await tagPostIntoFolder(folder.id, postId, postTitle, folder.name);
                        closeQuickFileModal();
                    });
                    quickFolderPickerList.appendChild(btn);
                });
            }

            quickFilePostModal.classList.add('show');
            quickFilePostModal.setAttribute('aria-hidden', 'false');
        }

        function closeQuickFileModal() {
            if (quickFilePostModal) {
                quickFilePostModal.classList.remove('show');
                quickFilePostModal.setAttribute('aria-hidden', 'true');
            }
        }

        if (quickFileCloseBtn) quickFileCloseBtn.addEventListener('click', closeQuickFileModal);
        if (quickFileCancelBtn) quickFileCancelBtn.addEventListener('click', closeQuickFileModal);

        // Create / Edit Folder Modal Logic
        function openCreateFolderModal() {
            if (!createFolderModal) return;
            folderModalTitle.textContent = 'Create Custom Folder';
            folderEditId.value = '';
            createFolderForm.reset();
            folderSelectedColor.value = '#4f46e5';
            folderSelectedIcon.value = 'folder';

            // Reset selected states
            document.querySelectorAll('#folderColorPicker .color-swatch-btn').forEach(btn => {
                btn.classList.toggle('selected', btn.dataset.color === '#4f46e5');
            });
            document.querySelectorAll('#folderIconPicker .icon-option-pill').forEach(btn => {
                btn.classList.toggle('selected', btn.dataset.icon === 'folder');
            });

            createFolderModal.classList.add('show');
            createFolderModal.setAttribute('aria-hidden', 'false');
            if (folderNameInput) folderNameInput.focus();
        }

        function openEditFolderModal(folder) {
            if (!createFolderModal) return;
            folderModalTitle.textContent = 'Edit Custom Folder';
            folderEditId.value = folder.id;
            folderNameInput.value = folder.name;
            folderDescInput.value = folder.description || '';
            folderSelectedColor.value = folder.color || '#4f46e5';
            folderSelectedIcon.value = folder.icon || 'folder';

            document.querySelectorAll('#folderColorPicker .color-swatch-btn').forEach(btn => {
                btn.classList.toggle('selected', btn.dataset.color === folder.color);
            });
            document.querySelectorAll('#folderIconPicker .icon-option-pill').forEach(btn => {
                btn.classList.toggle('selected', btn.dataset.icon === folder.icon);
            });

            createFolderModal.classList.add('show');
            createFolderModal.setAttribute('aria-hidden', 'false');
        }

        function closeCreateFolderModal() {
            if (createFolderModal) {
                createFolderModal.classList.remove('show');
                createFolderModal.setAttribute('aria-hidden', 'true');
            }
        }

        if (btnOpenNewFolderModal) btnOpenNewFolderModal.addEventListener('click', openCreateFolderModal);
        if (folderModalCloseBtn) folderModalCloseBtn.addEventListener('click', closeCreateFolderModal);
        if (folderModalCancelBtn) folderModalCancelBtn.addEventListener('click', closeCreateFolderModal);

        // Color picker interaction
        document.querySelectorAll('#folderColorPicker .color-swatch-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#folderColorPicker .color-swatch-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                folderSelectedColor.value = btn.dataset.color;
            });
        });

        // Icon picker interaction
        document.querySelectorAll('#folderIconPicker .icon-option-pill').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#folderIconPicker .icon-option-pill').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                folderSelectedIcon.value = btn.dataset.icon;
            });
        });

        // Create / Edit Form Submission
        if (createFolderForm) {
            createFolderForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const id = folderEditId.value;
                const payload = {
                    name: folderNameInput.value.trim(),
                    description: folderDescInput.value.trim(),
                    color: folderSelectedColor.value,
                    icon: folderSelectedIcon.value
                };

                const url = id ? `/api/folders/${id}` : '/api/folders';
                const method = id ? 'PUT' : 'POST';

                try {
                    const res = await fetch(url, {
                        method: method,
                        headers: getCsrfHeaders(),
                        body: JSON.stringify(payload)
                    });
                    if (!res.ok) throw new Error('Failed to save folder');
                    showToast(id ? '✓ Folder updated successfully!' : '✓ Folder created successfully!', 'success');
                    closeCreateFolderModal();
                    await fetchFolders();
                } catch (err) {
                    console.error('Error saving folder:', err);
                    showToast('Failed to save folder', 'error');
                }
            });
        }

        // Delete Folder
        async function confirmAndDeleteFolder(folderId, folderName) {
            if (!confirm(`Are you sure you want to delete folder "${folderName}"? All tagged records inside this folder will be removed.`)) {
                return;
            }
            try {
                const res = await fetch(`/api/folders/${folderId}`, {
                    method: 'DELETE',
                    headers: getCsrfHeaders()
                });
                if (res.ok) {
                    showToast(`✓ Folder "${folderName}" deleted`, 'success');
                    closeFolderDetailModal();
                    await fetchFolders();
                } else {
                    showToast('Could not delete folder', 'error');
                }
            } catch (err) {
                console.error('Error deleting folder:', err);
                showToast('Error deleting folder', 'error');
            }
        }

        // Folder Detail View Modal
        async function openFolderDetail(folderId) {
            if (!folderDetailModal) return;
            try {
                const res = await fetch(`/api/folders/${folderId}`);
                if (!res.ok) throw new Error('Folder not found');
                const data = await res.json();
                currentActiveFolder = data.folder;
                currentFolderRecords = data.records || [];
                currentFolderIdForRecord.value = folderId;

                // Populate Header Banner
                folderDetailBanner.style.background = `linear-gradient(135deg, ${currentActiveFolder.color} 0%, #111827 100%)`;
                folderDetailTitle.textContent = currentActiveFolder.name;
                folderDetailDesc.textContent = currentActiveFolder.description || 'Custom collection of dashboard items';
                folderDetailRecordBadge.innerHTML = `📁 ${currentFolderRecords.length} ${currentFolderRecords.length === 1 ? 'Record' : 'Records'} Archived`;
                folderDetailIconSlot.innerHTML = getFolderIconSvg(currentActiveFolder.icon);

                // Reset filter & add panel
                currentRecordTypeFilter = 'all';
                document.querySelectorAll('#folderRecordTypeFilter .folder-filter-pill').forEach(p => {
                    p.classList.toggle('active', p.dataset.type === 'all');
                });
                if (addRecordPanel) addRecordPanel.classList.remove('show');

                renderFolderRecords(currentFolderRecords);

                folderDetailModal.classList.add('show');
                folderDetailModal.setAttribute('aria-hidden', 'false');
            } catch (err) {
                console.error('Error opening folder detail:', err);
                showToast('Failed to load folder details', 'error');
            }
        }
        window.openFolderDetail = openFolderDetail;
        window.fetchFolders = fetchFolders;

        function closeFolderDetailModal() {
            if (folderDetailModal) {
                folderDetailModal.classList.remove('show');
                folderDetailModal.setAttribute('aria-hidden', 'true');
            }
        }

        if (folderDetailCloseBtn) folderDetailCloseBtn.addEventListener('click', closeFolderDetailModal);
        if (folderDetailCloseFooterBtn) folderDetailCloseFooterBtn.addEventListener('click', closeFolderDetailModal);

        if (btnDeleteCurrentFolder) {
            btnDeleteCurrentFolder.addEventListener('click', () => {
                if (currentActiveFolder) {
                    confirmAndDeleteFolder(currentActiveFolder.id, currentActiveFolder.name);
                }
            });
        }

        // Media upload & tabs elements
        const btnToggleAddMediaPanel = document.getElementById('btnToggleAddMediaPanel');
        const tabAddNoteBtn = document.getElementById('tabAddNoteBtn');
        const tabAddMediaBtn = document.getElementById('tabAddMediaBtn');
        const recordTitleLabel = document.getElementById('recordTitleLabel');
        const recordCategoryGroup = document.getElementById('recordCategoryGroup');
        const folderMediaDropzoneArea = document.getElementById('folderMediaDropzoneArea');
        const folderMediaFileInput = document.getElementById('folderMediaFileInput');
        const folderMediaDropzone = document.getElementById('folderMediaDropzone');
        const folderMediaUrlInput = document.getElementById('folderMediaUrlInput');
        const btnApplyFolderMediaUrl = document.getElementById('btnApplyFolderMediaUrl');
        const folderMediaPreviewWrapper = document.getElementById('folderMediaPreviewWrapper');
        const folderImgPreview = document.getElementById('folderImgPreview');
        const folderVideoPreview = document.getElementById('folderVideoPreview');
        const folderMediaNameBadge = document.getElementById('folderMediaNameBadge');
        const btnRemoveFolderMedia = document.getElementById('btnRemoveFolderMedia');
        const recordMediaUrl = document.getElementById('recordMediaUrl');
        const recordMediaType = document.getElementById('recordMediaType');

        let currentAddMode = 'note'; // 'note' or 'media'

        function setAddPanelMode(mode) {
            currentAddMode = mode;
            if (tabAddNoteBtn) tabAddNoteBtn.classList.toggle('active', mode === 'note');
            if (tabAddMediaBtn) tabAddMediaBtn.classList.toggle('active', mode === 'media');

            if (mode === 'media') {
                if (recordTitleLabel) recordTitleLabel.textContent = 'Media Title / Caption *';
                if (recordCategoryGroup) recordCategoryGroup.style.display = 'none';
                if (folderMediaDropzoneArea) folderMediaDropzoneArea.style.display = 'block';
            } else {
                if (recordTitleLabel) recordTitleLabel.textContent = 'Record Title *';
                if (recordCategoryGroup) recordCategoryGroup.style.display = 'block';
                if (folderMediaDropzoneArea) folderMediaDropzoneArea.style.display = 'none';
            }
        }

        if (tabAddNoteBtn) tabAddNoteBtn.addEventListener('click', () => setAddPanelMode('note'));
        if (tabAddMediaBtn) tabAddMediaBtn.addEventListener('click', () => setAddPanelMode('media'));

        function clearFolderMediaPreview() {
            if (recordMediaUrl) recordMediaUrl.value = '';
            if (recordMediaType) recordMediaType.value = '';
            if (folderMediaUrlInput) folderMediaUrlInput.value = '';
            if (folderMediaFileInput) folderMediaFileInput.value = '';
            if (folderMediaPreviewWrapper) folderMediaPreviewWrapper.style.display = 'none';
            if (folderImgPreview) { folderImgPreview.src = ''; folderImgPreview.style.display = 'none'; }
            if (folderVideoPreview) { folderVideoPreview.src = ''; folderVideoPreview.style.display = 'none'; }
            if (folderMediaDropzone) folderMediaDropzone.style.display = 'block';
        }

        function setFolderMediaPreview(url, fileName, isVideo) {
            if (!url) return;
            if (recordMediaUrl) recordMediaUrl.value = url;
            if (recordMediaType) recordMediaType.value = isVideo ? 'Video' : 'Image';
            if (folderMediaNameBadge) folderMediaNameBadge.textContent = fileName || (isVideo ? 'Video file' : 'Image file');

            if (folderMediaDropzone) folderMediaDropzone.style.display = 'none';
            if (folderMediaPreviewWrapper) folderMediaPreviewWrapper.style.display = 'block';

            if (isVideo) {
                if (folderImgPreview) folderImgPreview.style.display = 'none';
                if (folderVideoPreview) {
                    folderVideoPreview.src = url;
                    folderVideoPreview.style.display = 'block';
                }
            } else {
                if (folderVideoPreview) folderVideoPreview.style.display = 'none';
                if (folderImgPreview) {
                    folderImgPreview.src = url;
                    folderImgPreview.style.display = 'block';
                }
            }
        }

        if (btnRemoveFolderMedia) {
            btnRemoveFolderMedia.addEventListener('click', clearFolderMediaPreview);
        }

        // Folder Media File Input
        if (folderMediaDropzone && folderMediaFileInput) {
            folderMediaDropzone.addEventListener('click', () => folderMediaFileInput.click());

            folderMediaFileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (!file) return;
                const isVideo = file.type.startsWith('video/');
                const reader = new FileReader();
                reader.onload = (loadEvent) => {
                    setFolderMediaPreview(loadEvent.target.result, file.name, isVideo);
                };
                reader.readAsDataURL(file);
            });

            // Drag and drop onto media dropzone
            folderMediaDropzone.addEventListener('dragover', (e) => {
                e.preventDefault();
                folderMediaDropzone.style.borderColor = 'var(--primary)';
                folderMediaDropzone.style.backgroundColor = 'rgba(79, 70, 229, 0.08)';
            });

            folderMediaDropzone.addEventListener('dragleave', () => {
                folderMediaDropzone.style.borderColor = 'rgba(79, 70, 229, 0.4)';
                folderMediaDropzone.style.backgroundColor = 'rgba(79, 70, 229, 0.03)';
            });

            folderMediaDropzone.addEventListener('drop', (e) => {
                e.preventDefault();
                folderMediaDropzone.style.borderColor = 'rgba(79, 70, 229, 0.4)';
                folderMediaDropzone.style.backgroundColor = 'rgba(79, 70, 229, 0.03)';
                const file = e.dataTransfer.files[0];
                if (file) {
                    const isVideo = file.type.startsWith('video/');
                    const reader = new FileReader();
                    reader.onload = (loadEvent) => {
                        setFolderMediaPreview(loadEvent.target.result, file.name, isVideo);
                    };
                    reader.readAsDataURL(file);
                }
            });
        }

        // Direct URL Input for Folder Media
        if (btnApplyFolderMediaUrl && folderMediaUrlInput) {
            btnApplyFolderMediaUrl.addEventListener('click', () => {
                const url = folderMediaUrlInput.value.trim();
                if (!url) return;
                const isVideo = url.endsWith('.mp4') || url.endsWith('.webm') || url.endsWith('.mov') || url.includes('video');
                setFolderMediaPreview(url, 'Linked Media', isVideo);
            });
        }

        // Render Records List inside Folder Detail
        function renderFolderRecords(records) {
            if (!folderRecordsContainer) return;
            folderRecordsContainer.innerHTML = '';

            const filtered = records.filter(rec => {
                if (currentRecordTypeFilter === 'all') return true;
                if (currentRecordTypeFilter === 'media') {
                    return rec.hasMedia || rec.mediaUrl || rec.itemType === 'IMAGE' || rec.itemType === 'VIDEO';
                }
                return (rec.itemType || '').toLowerCase() === currentRecordTypeFilter.toLowerCase();
            });

            if (filtered.length === 0) {
                folderRecordsContainer.innerHTML = `
                    <div class="empty-folder-state">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line>
                            <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                        <h4 style="margin: 0 0 4px 0; color: var(--text-primary); font-size: 14px;">No records found</h4>
                        <p style="margin: 0; font-size: 12px;">Drag posts into this folder, or click "+ Add Record" or "Upload Media" above.</p>
                    </div>
                `;
                return;
            }

            filtered.forEach(rec => {
                const item = document.createElement('div');
                item.className = 'folder-record-item';

                const typeLower = (rec.itemType || 'note').toLowerCase();
                const badgeColorClass = `badge-${rec.badgeColor || 'primary'}`;

                // Media Preview HTML
                let mediaHtml = '';
                if (rec.hasMedia || rec.mediaUrl) {
                    const isVid = rec.video || (rec.mediaType && rec.mediaType.toLowerCase().includes('video')) || (rec.mediaUrl && (rec.mediaUrl.startsWith('data:video') || rec.mediaUrl.includes('.mp4') || rec.mediaUrl.includes('.webm') || rec.mediaUrl.includes('.mov')));
                    if (isVid) {
                        mediaHtml = `
                            <div class="folder-record-media-box">
                                <video controls src="${rec.mediaUrl}"></video>
                            </div>
                            <div class="folder-record-media-actions">
                                <button type="button" class="btn-use-media-in-post" data-media-url="${rec.mediaUrl}" data-media-type="Video" data-caption="${escapeHtml(rec.title)}">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="13 2 13 9 20 9"></polyline><polygon points="6 2 18 2 18 6 6 6 6 2"></polygon><line x1="6" y1="18" x2="6.01" y2="18"></line><line x1="18" y1="18" x2="18.01" y2="18"></line></svg>
                                    <span>🚀 Use in New Post</span>
                                </button>
                            </div>
                        `;
                    } else {
                        mediaHtml = `
                            <div class="folder-record-media-box">
                                <img src="${rec.mediaUrl}" alt="${escapeHtml(rec.title)}" loading="lazy">
                            </div>
                            <div class="folder-record-media-actions">
                                <button type="button" class="btn-use-media-in-post" data-media-url="${rec.mediaUrl}" data-media-type="Single Image" data-caption="${escapeHtml(rec.title)}">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="13 2 13 9 20 9"></polyline><polygon points="6 2 18 2 18 6 6 6 6 2"></polygon><line x1="6" y1="18" x2="6.01" y2="18"></line><line x1="18" y1="18" x2="18.01" y2="18"></line></svg>
                                    <span>🚀 Use in New Post</span>
                                </button>
                            </div>
                        `;
                    }
                }

                item.innerHTML = `
                    <div class="folder-record-left">
                        <div class="record-type-badge badge-type-${typeLower}" title="${rec.itemType}">
                            ${getRecordTypeIconSvg(rec.itemType)}
                        </div>
                        <div class="folder-record-info">
                            <div class="folder-record-title">${escapeHtml(rec.title)}</div>
                            ${rec.subtitle ? `<div class="folder-record-subtitle">${escapeHtml(rec.subtitle)}</div>` : ''}
                            ${rec.content ? `<div class="folder-record-content-snippet">${escapeHtml(rec.content)}</div>` : ''}
                            ${mediaHtml}
                        </div>
                    </div>
                    <div class="folder-record-right">
                        ${rec.badge ? `<span class="folder-record-badge ${badgeColorClass}">${escapeHtml(rec.badge)}</span>` : ''}
                        <span style="font-size: 0.75rem; color: var(--text-muted); white-space: nowrap;">${rec.createdAtFormatted || ''}</span>
                        <button type="button" class="btn-remove-record" data-record-id="${rec.id}" title="Remove record from folder">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="12" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    </div>
                `;

                // "Use in New Post" button click
                const usePostBtn = item.querySelector('.btn-use-media-in-post');
                if (usePostBtn) {
                    usePostBtn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const mediaUrl = usePostBtn.dataset.mediaUrl;
                        const mediaType = usePostBtn.dataset.mediaType;
                        const caption = usePostBtn.dataset.caption;

                        closeFolderDetailModal();

                        // Open Create Post Modal
                        const createPostModal = document.getElementById('createPostModal');
                        if (createPostModal) {
                            createPostModal.classList.add('show');
                            createPostModal.setAttribute('aria-hidden', 'false');

                            const postCaptionInput = document.getElementById('postCaptionInput');
                            if (postCaptionInput && caption) {
                                postCaptionInput.value = caption;
                            }

                            // Attach to composer
                            if (window.setComposerMedia) {
                                window.setComposerMedia(mediaUrl, caption, mediaType === 'Video');
                            }

                            showToast('✓ Loaded media from folder into Post Composer!', 'success');
                        }
                    });
                }

                // Delete Record button
                item.querySelector('.btn-remove-record').addEventListener('click', async (e) => {
                    e.stopPropagation();
                    await removeRecordFromFolder(currentActiveFolder.id, rec.id, rec.title);
                });

                folderRecordsContainer.appendChild(item);
            });
        }

        // Filter pills inside folder detail
        document.querySelectorAll('#folderRecordTypeFilter .folder-filter-pill').forEach(pill => {
            pill.addEventListener('click', () => {
                document.querySelectorAll('#folderRecordTypeFilter .folder-filter-pill').forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                currentRecordTypeFilter = pill.dataset.type;
                renderFolderRecords(currentFolderRecords);
            });
        });

        // Toggle Add Record Panel
        if (btnToggleAddRecordPanel) {
            btnToggleAddRecordPanel.addEventListener('click', () => {
                if (addRecordPanel) {
                    addRecordPanel.classList.toggle('show');
                    if (addRecordPanel.classList.contains('show')) {
                        setAddPanelMode('note');
                        const titleInput = document.getElementById('newRecordTitle');
                        if (titleInput) titleInput.focus();
                    }
                }
            });
        }

        // Toggle Upload Media Panel
        if (btnToggleAddMediaPanel) {
            btnToggleAddMediaPanel.addEventListener('click', () => {
                if (addRecordPanel) {
                    addRecordPanel.classList.add('show');
                    setAddPanelMode('media');
                    const titleInput = document.getElementById('newRecordTitle');
                    if (titleInput) titleInput.focus();
                }
            });
        }

        if (btnCancelAddRecord) {
            btnCancelAddRecord.addEventListener('click', () => {
                if (addRecordPanel) addRecordPanel.classList.remove('show');
                clearFolderMediaPreview();
            });
        }

        // Add Record Form Submission (Supports Notes, Reports, Images & Videos!)
        if (addRecordForm) {
            addRecordForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const folderId = currentFolderIdForRecord.value;
                const title = document.getElementById('newRecordTitle').value.trim();
                let itemType = document.getElementById('newRecordType').value;
                const content = document.getElementById('newRecordContent').value.trim();
                const mediaUrlVal = recordMediaUrl ? recordMediaUrl.value : '';
                const mediaTypeVal = recordMediaType ? recordMediaType.value : '';

                if (currentAddMode === 'media' || (mediaUrlVal && mediaUrlVal.length > 0)) {
                    itemType = mediaTypeVal.toLowerCase().includes('video') ? 'VIDEO' : 'IMAGE';
                }

                const payload = {
                    title: title,
                    itemType: itemType,
                    subtitle: `Added to ${currentActiveFolder ? currentActiveFolder.name : 'folder'}`,
                    content: content,
                    mediaUrl: mediaUrlVal,
                    mediaType: mediaTypeVal,
                    badge: itemType === 'VIDEO' ? 'Video' : (itemType === 'IMAGE' ? 'Image' : (itemType === 'NOTE' ? 'Task' : 'Report')),
                    badgeColor: (itemType === 'IMAGE' || itemType === 'VIDEO') ? 'info' : (itemType === 'NOTE' ? 'warning' : 'primary')
                };

                try {
                    const res = await fetch(`/api/folders/${folderId}/records`, {
                        method: 'POST',
                        headers: getCsrfHeaders(),
                        body: JSON.stringify(payload)
                    });
                    if (!res.ok) throw new Error('Failed to add record');
                    const savedRecord = await res.json();
                    currentFolderRecords.unshift(savedRecord);
                    renderFolderRecords(currentFolderRecords);
                    folderDetailRecordBadge.innerHTML = `📁 ${currentFolderRecords.length} ${currentFolderRecords.length === 1 ? 'Record' : 'Records'} Archived`;
                    addRecordForm.reset();
                    clearFolderMediaPreview();
                    if (addRecordPanel) addRecordPanel.classList.remove('show');
                    showToast(itemType === 'VIDEO' || itemType === 'IMAGE' ? '✓ Media file saved to folder!' : '✓ Record added to folder', 'success');
                    await fetchFolders(); // update card counts on dashboard
                } catch (err) {
                    console.error('Error adding record:', err);
                    showToast('Failed to add record', 'error');
                }
            });
        }

        // Remove Record from Folder API
        async function removeRecordFromFolder(folderId, recordId, recordTitle) {
            try {
                const res = await fetch(`/api/folders/${folderId}/records/${recordId}`, {
                    method: 'DELETE',
                    headers: getCsrfHeaders()
                });
                if (res.ok) {
                    currentFolderRecords = currentFolderRecords.filter(r => r.id !== recordId);
                    renderFolderRecords(currentFolderRecords);
                    folderDetailRecordBadge.innerHTML = `📁 ${currentFolderRecords.length} ${currentFolderRecords.length === 1 ? 'Record' : 'Records'} Archived`;
                    showToast(`✓ Removed "${recordTitle}" from folder`, 'success');
                    await fetchFolders();
                } else {
                    showToast('Failed to remove record', 'error');
                }
            } catch (err) {
                console.error('Error removing record:', err);
                showToast('Error removing record', 'error');
            }
        }

        // Export Folder Records to CSV
        async function exportFolderToCsv(folderId, folderName) {
            try {
                let records = currentFolderRecords;
                if (!records || records.length === 0 || !currentActiveFolder || currentActiveFolder.id !== folderId) {
                    const res = await fetch(`/api/folders/${folderId}/records`);
                    if (res.ok) records = await res.json();
                }

                if (!records || records.length === 0) {
                    showToast('This folder is currently empty. Nothing to export.', 'error');
                    return;
                }

                const headers = ['ID', 'Type', 'Title', 'Subtitle', 'Content', 'Badge', 'Created Date'];
                const rows = records.map(r => [
                    r.id,
                    `"${(r.itemType || '').replace(/"/g, '""')}"`,
                    `"${(r.title || '').replace(/"/g, '""')}"`,
                    `"${(r.subtitle || '').replace(/"/g, '""')}"`,
                    `"${(r.content || '').replace(/"/g, '""')}"`,
                    `"${(r.badge || '').replace(/"/g, '""')}"`,
                    `"${(r.createdAtFormatted || '').replace(/"/g, '""')}"`
                ]);

                const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(e => e.join(','))].join('\n');
                const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `${folderName.replace(/\s+/g, '_')}_records.csv`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
                showToast(`✓ Exported "${folderName}" records to CSV!`, 'success');
            } catch (err) {
                console.error('Export error:', err);
                showToast('Failed to export CSV', 'error');
            }
        }

        if (btnExportFolderCsv) {
            btnExportFolderCsv.addEventListener('click', () => {
                if (currentActiveFolder) {
                    exportFolderToCsv(currentActiveFolder.id, currentActiveFolder.name);
                }
            });
        }

        // Filter folders search input
        if (folderSearchInput) {
            folderSearchInput.addEventListener('input', (e) => {
                const q = e.target.value.toLowerCase().trim();
                if (!q) {
                    renderFoldersGrid(allFolders);
                } else {
                    const filtered = allFolders.filter(f =>
                        f.name.toLowerCase().includes(q) ||
                        (f.description && f.description.toLowerCase().includes(q))
                    );
                    renderFoldersGrid(filtered);
                }
            });
        }

        // Global click to close folder menus
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.folder-actions-menu')) {
                document.querySelectorAll('.folder-menu-dropdown.show').forEach(m => m.classList.remove('show'));
            }
        });

        // HTML escape helper
        function escapeHtml(str) {
            if (!str) return '';
            return str
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        // Initial setup
        fetchFolders();
        bindDraggablePosts();

        // Also re-bind draggable posts whenever posts change
        const observer = new MutationObserver(() => {
            bindDraggablePosts();
        });
        const recentContainer = document.getElementById('recentPostsContainer');
        const scheduledContainer = document.getElementById('scheduledQueueList');
        if (recentContainer) observer.observe(recentContainer, { childList: true });
        if (scheduledContainer) observer.observe(scheduledContainer, { childList: true });
    }

    initFoldersManager();
    initMediaAttachment();

    // -------------------------------------------------------------------------
    // Designer Vault: Graphics, Mockups, Brand Kits (Single-Admin Managed)
    // -------------------------------------------------------------------------
    function initDesignerVault() {
        const designerAssetsGrid = document.getElementById('designerAssetsGrid');
        const designerAssetCountBadge = document.getElementById('designerAssetCountBadge');
        const btnOpenAddDesignerModal = document.getElementById('btnOpenAddDesignerModal');
        const addDesignerAssetModal = document.getElementById('addDesignerAssetModal');
        const closeDesignerModalBtn = document.getElementById('closeDesignerModalBtn');
        const cancelDesignerModalBtn = document.getElementById('cancelDesignerModalBtn');
        const addDesignerAssetForm = document.getElementById('addDesignerAssetForm');
        const btnExploreDesignerFolder = document.getElementById('btnExploreDesignerFolder');
        const filterPills = document.querySelectorAll('#designerFilterPills .studio-pill');

        const designerMediaFileInput = document.getElementById('designerMediaFileInput');
        const designerMediaDropzone = document.getElementById('designerMediaDropzone');
        const designerMediaUrlInput = document.getElementById('designerMediaUrlInput');
        const btnApplyDesignerMediaUrl = document.getElementById('btnApplyDesignerMediaUrl');
        const designerPreviewWrapper = document.getElementById('designerPreviewWrapper');
        const designerImgPreview = document.getElementById('designerImgPreview');
        const designerVideoPreview = document.getElementById('designerVideoPreview');
        const designerMediaNameBadge = document.getElementById('designerMediaNameBadge');
        const btnRemoveDesignerMedia = document.getElementById('btnRemoveDesignerMedia');
        const designerMediaUrlHidden = document.getElementById('designerMediaUrlHidden');
        const designerMediaTypeHidden = document.getElementById('designerMediaTypeHidden');

        let currentDesignerFolder = null;
        let allDesignerRecords = [];
        let activeFilter = 'all';

        async function fetchDesignerAssets() {
            if (!designerAssetsGrid) return;
            try {
                const res = await fetch('/api/folders/designer', { headers: getCsrfHeaders() });
                if (!res.ok) throw new Error('Failed to fetch designer vault');
                const data = await res.json();
                currentDesignerFolder = data.folder;
                allDesignerRecords = data.records || [];
                if (designerAssetCountBadge) {
                    designerAssetCountBadge.textContent = `${allDesignerRecords.length} ${allDesignerRecords.length === 1 ? 'Asset' : 'Assets'}`;
                }
                renderDesignerGrid();
            } catch (err) {
                console.error('Error loading designer assets:', err);
            }
        }

        function renderDesignerGrid() {
            if (!designerAssetsGrid) return;
            let list = allDesignerRecords;
            if (activeFilter === 'mockup') {
                list = allDesignerRecords.filter(r => (r.itemType && r.itemType.toLowerCase().includes('mockup')) || (r.title && r.title.toLowerCase().includes('mockup')));
            } else if (activeFilter === 'banner') {
                list = allDesignerRecords.filter(r => (r.itemType && r.itemType.toLowerCase().includes('banner')) || (r.title && r.title.toLowerCase().includes('banner')));
            } else if (activeFilter === 'kit') {
                list = allDesignerRecords.filter(r => (r.itemType && (r.itemType.toLowerCase().includes('kit') || r.itemType.toLowerCase().includes('figma'))) || (r.title && r.title.toLowerCase().includes('kit')));
            }

            if (list.length === 0) {
                designerAssetsGrid.innerHTML = `
                    <div style="grid-column: 1 / -1; padding: 40px 20px; text-align: center; color: var(--text-muted);">
                        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 8px; opacity: 0.5;">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        <p style="font-size: 13px; font-weight: 600; margin-bottom: 4px;">No designer assets found</p>
                        <p style="font-size: 12px; margin: 0;">Click "+ Add Asset" above to store your first mockup or banner.</p>
                    </div>
                `;
                return;
            }

            designerAssetsGrid.innerHTML = list.map(item => {
                const isVideo = (item.mediaType && item.mediaType.toLowerCase().includes('video')) || (item.mediaUrl && (item.mediaUrl.includes('.mp4') || item.mediaUrl.includes('.webm')));
                const hasMedia = item.mediaUrl && item.mediaUrl.trim().length > 0;

                let mediaHtml = '';
                if (hasMedia) {
                    if (isVideo) {
                        mediaHtml = `<video src="${item.mediaUrl}" controls></video>`;
                    } else {
                        mediaHtml = `<img src="${item.mediaUrl}" alt="${escapeHtml(item.title)}" loading="lazy">`;
                    }
                } else {
                    mediaHtml = `
                        <div class="designer-thumb-placeholder">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                                <circle cx="13.5" cy="6.5" r=".5" fill="currentColor"></circle>
                                <circle cx="17.5" cy="10.5" r=".5" fill="currentColor"></circle>
                                <circle cx="8.5" cy="7.5" r=".5" fill="currentColor"></circle>
                                <circle cx="6.5" cy="12.5" r=".5" fill="currentColor"></circle>
                                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.563-2.512 5.563-5.563C22 6.5 17.5 2 12 2z"></path>
                            </svg>
                            <span style="font-size: 10px; font-weight: 700;">${escapeHtml(item.itemType || 'ASSET')}</span>
                        </div>
                    `;
                }

                const badgeColor = item.badgeColor === 'success' ? '#10b981' : (item.badgeColor === 'warning' ? '#f59e0b' : '#ec4899');

                return `
                    <div class="designer-asset-card" data-id="${item.id}">
                        <div class="designer-asset-thumb-box">
                            ${mediaHtml}
                            <span class="designer-badge-overlay" style="border-left: 3px solid ${badgeColor};">
                                ${escapeHtml(item.badge || 'Asset')}
                            </span>
                        </div>
                        <div class="designer-asset-body">
                            <h4 class="designer-asset-title" title="${escapeHtml(item.title)}">${escapeHtml(item.title)}</h4>
                            <div class="designer-asset-specs">${escapeHtml(item.subtitle || 'Design Resource')}</div>
                            ${item.content ? `<div class="designer-asset-notes">${escapeHtml(item.content)}</div>` : ''}
                            <div class="designer-asset-footer">
                                ${hasMedia ? `
                                    <button type="button" class="btn-use-designer-asset" data-media="${escapeHtml(item.mediaUrl)}" data-type="${isVideo ? 'Video' : 'Image'}" data-title="${escapeHtml(item.title)}" title="Load into new social post">
                                        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                                        <span>Use in Post</span>
                                    </button>
                                ` : `
                                    <button type="button" class="btn-copy-script btn-copy-designer-title" data-title="${escapeHtml(item.title)}" title="Copy asset name">
                                        📋 Copy
                                    </button>
                                `}
                                <div style="display: flex; gap: 4px;">
                                    ${hasMedia ? `
                                        <button type="button" class="btn-icon-subtle btn-copy-media-link" data-url="${escapeHtml(item.mediaUrl)}" title="Copy media URL">
                                            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path></svg>
                                        </button>
                                    ` : ''}
                                    <button type="button" class="btn-icon-subtle btn-delete-designer-asset" data-id="${item.id}" title="Remove asset">
                                        <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="12" x2="18" y2="18"></line></svg>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');

            // Bind actions in grid
            designerAssetsGrid.querySelectorAll('.btn-use-designer-asset').forEach(btn => {
                btn.addEventListener('click', () => {
                    const mediaUrl = btn.dataset.media;
                    const mediaType = btn.dataset.type;
                    const title = btn.dataset.title;

                    const createPostModal = document.getElementById('createPostModal');
                    const postTitleInput = document.getElementById('postTitleInput');
                    const postMediaUrlInput = document.getElementById('postMediaUrlInput');
                    const mediaPreviewWrapper = document.getElementById('mediaPreviewWrapper');
                    const imagePreviewElement = document.getElementById('imagePreviewElement');
                    const videoPreviewElement = document.getElementById('videoPreviewElement');
                    const mediaUploadPrompt = document.getElementById('mediaUploadPrompt');
                    const mediaFileNameBadge = document.getElementById('mediaFileNameBadge');

                    if (createPostModal) {
                        if (postTitleInput && title) postTitleInput.value = title;
                        if (postMediaUrlInput) postMediaUrlInput.value = mediaUrl;
                        if (mediaUploadPrompt) mediaUploadPrompt.style.display = 'none';
                        if (mediaPreviewWrapper) mediaPreviewWrapper.style.display = 'block';

                        if (mediaType === 'Video') {
                            if (videoPreviewElement) {
                                videoPreviewElement.src = mediaUrl;
                                videoPreviewElement.style.display = 'block';
                            }
                            if (imagePreviewElement) imagePreviewElement.style.display = 'none';
                            if (mediaFileNameBadge) mediaFileNameBadge.textContent = '🎥 Video Asset Loaded';
                        } else {
                            if (imagePreviewElement) {
                                imagePreviewElement.src = mediaUrl;
                                imagePreviewElement.style.display = 'block';
                            }
                            if (videoPreviewElement) videoPreviewElement.style.display = 'none';
                            if (mediaFileNameBadge) mediaFileNameBadge.textContent = '🎨 Designer Asset Loaded';
                        }

                        createPostModal.classList.add('show');
                        createPostModal.setAttribute('aria-hidden', 'false');
                        showToast(`✓ Designer asset loaded into post composer!`, 'success');
                    }
                });
            });

            designerAssetsGrid.querySelectorAll('.btn-copy-designer-title').forEach(btn => {
                btn.addEventListener('click', () => {
                    navigator.clipboard.writeText(btn.dataset.title).then(() => {
                        showToast('✓ Asset name copied to clipboard!', 'success');
                    });
                });
            });

            designerAssetsGrid.querySelectorAll('.btn-copy-media-link').forEach(btn => {
                btn.addEventListener('click', () => {
                    navigator.clipboard.writeText(btn.dataset.url).then(() => {
                        showToast('✓ Media link copied to clipboard!', 'success');
                    }).catch(() => {
                        showToast('Failed to copy link', 'error');
                    });
                });
            });

            designerAssetsGrid.querySelectorAll('.btn-delete-designer-asset').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const recordId = btn.dataset.id;
                    if (!confirm('Are you sure you want to remove this designer asset?')) return;
                    if (!currentDesignerFolder) return;
                    try {
                        const res = await fetch(`/api/folders/${currentDesignerFolder.id}/records/${recordId}`, {
                            method: 'DELETE',
                            headers: getCsrfHeaders()
                        });
                        if (res.ok) {
                            showToast('Asset removed from Designer Vault', 'success');
                            fetchDesignerAssets();
                            if (typeof window.fetchFolders === 'function') window.fetchFolders();
                        } else {
                            showToast('Failed to delete asset', 'error');
                        }
                    } catch (err) {
                        console.error('Delete error:', err);
                        showToast('Error removing asset', 'error');
                    }
                });
            });
        }

        // Filter pills click
        filterPills.forEach(pill => {
            pill.addEventListener('click', () => {
                filterPills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                activeFilter = pill.dataset.filter || 'all';
                renderDesignerGrid();
            });
        });

        // Open Explore Folder
        if (btnExploreDesignerFolder) {
            btnExploreDesignerFolder.addEventListener('click', () => {
                if (currentDesignerFolder && typeof window.openFolderDetail === 'function') {
                    window.openFolderDetail(currentDesignerFolder.id);
                }
            });
        }

        // Modal Open / Close
        if (btnOpenAddDesignerModal && addDesignerAssetModal) {
            btnOpenAddDesignerModal.addEventListener('click', () => {
                addDesignerAssetForm.reset();
                if (designerPreviewWrapper) designerPreviewWrapper.style.display = 'none';
                if (designerMediaUrlHidden) designerMediaUrlHidden.value = '';
                addDesignerAssetModal.classList.add('show');
                addDesignerAssetModal.setAttribute('aria-hidden', 'false');
            });
        }

        function closeDesignerModal() {
            if (addDesignerAssetModal) {
                addDesignerAssetModal.classList.remove('show');
                addDesignerAssetModal.setAttribute('aria-hidden', 'true');
            }
        }

        if (closeDesignerModalBtn) closeDesignerModalBtn.addEventListener('click', closeDesignerModal);
        if (cancelDesignerModalBtn) cancelDesignerModalBtn.addEventListener('click', closeDesignerModal);

        // Media attachment logic for designer modal
        if (designerMediaDropzone && designerMediaFileInput) {
            designerMediaDropzone.addEventListener('click', () => designerMediaFileInput.click());

            ['dragenter', 'dragover'].forEach(evt => {
                designerMediaDropzone.addEventListener(evt, (e) => {
                    e.preventDefault();
                    designerMediaDropzone.style.borderColor = '#ec4899';
                    designerMediaDropzone.style.background = 'rgba(236, 72, 153, 0.08)';
                });
            });

            ['dragleave', 'drop'].forEach(evt => {
                designerMediaDropzone.addEventListener(evt, (e) => {
                    e.preventDefault();
                    designerMediaDropzone.style.borderColor = 'rgba(236, 72, 153, 0.4)';
                    designerMediaDropzone.style.background = 'rgba(236, 72, 153, 0.03)';
                });
            });

            designerMediaDropzone.addEventListener('drop', (e) => {
                if (e.dataTransfer.files && e.dataTransfer.files[0]) {
                    handleDesignerFile(e.dataTransfer.files[0]);
                }
            });

            designerMediaFileInput.addEventListener('change', (e) => {
                if (e.target.files && e.target.files[0]) {
                    handleDesignerFile(e.target.files[0]);
                }
            });
        }

        function handleDesignerFile(file) {
            const isVideo = file.type.startsWith('video');
            const reader = new FileReader();
            reader.onload = (e) => {
                const dataUrl = e.target.result;
                if (designerMediaUrlHidden) designerMediaUrlHidden.value = dataUrl;
                if (designerMediaTypeHidden) designerMediaTypeHidden.value = isVideo ? 'Video' : 'Image';
                if (designerMediaNameBadge) designerMediaNameBadge.textContent = file.name;

                if (designerPreviewWrapper) designerPreviewWrapper.style.display = 'block';
                if (isVideo) {
                    if (designerVideoPreview) {
                        designerVideoPreview.src = dataUrl;
                        designerVideoPreview.style.display = 'block';
                    }
                    if (designerImgPreview) designerImgPreview.style.display = 'none';
                } else {
                    if (designerImgPreview) {
                        designerImgPreview.src = dataUrl;
                        designerImgPreview.style.display = 'block';
                    }
                    if (designerVideoPreview) designerVideoPreview.style.display = 'none';
                }
            };
            reader.readAsDataURL(file);
        }

        if (btnApplyDesignerMediaUrl && designerMediaUrlInput) {
            btnApplyDesignerMediaUrl.addEventListener('click', () => {
                const url = designerMediaUrlInput.value.trim();
                if (!url) return;
                const isVideo = url.endsWith('.mp4') || url.endsWith('.webm');
                if (designerMediaUrlHidden) designerMediaUrlHidden.value = url;
                if (designerMediaTypeHidden) designerMediaTypeHidden.value = isVideo ? 'Video' : 'Image';
                if (designerMediaNameBadge) designerMediaNameBadge.textContent = 'Linked Media Attached';

                if (designerPreviewWrapper) designerPreviewWrapper.style.display = 'block';
                if (isVideo) {
                    if (designerVideoPreview) {
                        designerVideoPreview.src = url;
                        designerVideoPreview.style.display = 'block';
                    }
                    if (designerImgPreview) designerImgPreview.style.display = 'none';
                } else {
                    if (designerImgPreview) {
                        designerImgPreview.src = url;
                        designerImgPreview.style.display = 'block';
                    }
                    if (designerVideoPreview) designerVideoPreview.style.display = 'none';
                }
                designerMediaUrlInput.value = '';
            });
        }

        if (btnRemoveDesignerMedia) {
            btnRemoveDesignerMedia.addEventListener('click', () => {
                if (designerMediaUrlHidden) designerMediaUrlHidden.value = '';
                if (designerPreviewWrapper) designerPreviewWrapper.style.display = 'none';
                if (designerImgPreview) designerImgPreview.src = '';
                if (designerVideoPreview) designerVideoPreview.src = '';
            });
        }

        // Form Submit
        if (addDesignerAssetForm) {
            addDesignerAssetForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const title = document.getElementById('designerAssetTitle').value.trim();
                const itemType = document.getElementById('designerAssetCategory').value;
                const status = document.getElementById('designerAssetStatus').value;
                const specs = document.getElementById('designerAssetSpecs').value.trim();
                const content = document.getElementById('designerAssetContent').value.trim();
                const mediaUrl = designerMediaUrlHidden ? designerMediaUrlHidden.value : '';
                const mediaType = designerMediaTypeHidden ? designerMediaTypeHidden.value : 'Image';

                let badgeColor = 'primary';
                if (status === 'Approved') badgeColor = 'success';
                else if (status === 'In Review') badgeColor = 'warning';
                else if (status === 'Draft') badgeColor = 'secondary';

                const payload = {
                    itemType: itemType,
                    title: title,
                    subtitle: specs || (itemType + ' Asset'),
                    content: content,
                    mediaUrl: mediaUrl,
                    mediaType: mediaType,
                    badge: status,
                    badgeColor: badgeColor
                };

                try {
                    const res = await fetch('/api/folders/designer/records', {
                        method: 'POST',
                        headers: getCsrfHeaders(),
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        showToast(`✓ Asset "${title}" saved to Designer Vault!`, 'success');
                        closeDesignerModal();
                        addDesignerAssetForm.reset();
                        fetchDesignerAssets();
                        if (typeof window.fetchFolders === 'function') window.fetchFolders();
                    } else {
                        showToast('Failed to save asset', 'error');
                    }
                } catch (err) {
                    console.error('Error saving designer asset:', err);
                    showToast('Server error saving asset', 'error');
                }
            });
        }

        fetchDesignerAssets();
    }

    // -------------------------------------------------------------------------
    // Content Creator Studio: Scripts, Hooks, Drafts (Single-Admin Managed)
    // -------------------------------------------------------------------------
    function initCreatorStudio() {
        const creatorDraftsList = document.getElementById('creatorDraftsList');
        const creatorDraftCountBadge = document.getElementById('creatorDraftCountBadge');
        const btnOpenAddCreatorModal = document.getElementById('btnOpenAddCreatorModal');
        const addCreatorDraftModal = document.getElementById('addCreatorDraftModal');
        const closeCreatorModalBtn = document.getElementById('closeCreatorModalBtn');
        const cancelCreatorModalBtn = document.getElementById('cancelCreatorModalBtn');
        const addCreatorDraftForm = document.getElementById('addCreatorDraftForm');
        const btnExploreCreatorFolder = document.getElementById('btnExploreCreatorFolder');
        const filterPills = document.querySelectorAll('#creatorFilterPills .studio-pill');

        let currentCreatorFolder = null;
        let allCreatorRecords = [];
        let activeFilter = 'all';

        async function fetchCreatorDrafts() {
            if (!creatorDraftsList) return;
            try {
                const res = await fetch('/api/folders/creator', { headers: getCsrfHeaders() });
                if (!res.ok) throw new Error('Failed to fetch creator studio');
                const data = await res.json();
                currentCreatorFolder = data.folder;
                allCreatorRecords = data.records || [];
                if (creatorDraftCountBadge) {
                    creatorDraftCountBadge.textContent = `${allCreatorRecords.length} ${allCreatorRecords.length === 1 ? 'Draft' : 'Drafts'}`;
                }
                renderCreatorList();
            } catch (err) {
                console.error('Error loading creator drafts:', err);
            }
        }

        function renderCreatorList() {
            if (!creatorDraftsList) return;
            let list = allCreatorRecords;
            if (activeFilter === 'video') {
                list = allCreatorRecords.filter(r => (r.itemType && (r.itemType.includes('VIDEO') || r.itemType.includes('REEL'))) || (r.subtitle && r.subtitle.toLowerCase().includes('reel')) || (r.subtitle && r.subtitle.toLowerCase().includes('video')));
            } else if (activeFilter === 'hook') {
                list = allCreatorRecords.filter(r => (r.itemType && r.itemType.includes('HOOK')) || (r.content && r.content.toLowerCase().includes('hook')) || (r.title && r.title.toLowerCase().includes('hook')));
            } else if (activeFilter === 'copy') {
                list = allCreatorRecords.filter(r => (r.itemType && (r.itemType.includes('DRAFT') || r.itemType.includes('COPY'))));
            }

            if (list.length === 0) {
                creatorDraftsList.innerHTML = `
                    <div style="padding: 40px 20px; text-align: center; color: var(--text-muted);">
                        <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 8px; opacity: 0.5;">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        <p style="font-size: 13px; font-weight: 600; margin-bottom: 4px;">No scripts or drafts found</p>
                        <p style="font-size: 12px; margin: 0;">Click "+ New Script" above to draft your first viral hook or reel outline.</p>
                    </div>
                `;
                return;
            }

            creatorDraftsList.innerHTML = list.map(item => {
                const badgeColor = item.badgeColor === 'success' ? '#10b981' : (item.badgeColor === 'warning' ? '#f59e0b' : (item.badgeColor === 'purple' ? '#8b5cf6' : '#0284c7'));

                return `
                    <div class="creator-draft-card" data-id="${item.id}">
                        <div class="creator-draft-header">
                            <div>
                                <h4 class="creator-draft-title">${escapeHtml(item.title)}</h4>
                                <div class="creator-draft-platform">${escapeHtml(item.subtitle || 'Multi-Platform Draft')}</div>
                            </div>
                            <span class="badge" style="background: rgba(249, 115, 22, 0.1); color: ${badgeColor}; border: 1px solid ${badgeColor}; font-size: 0.7rem; font-weight: 700; padding: 2px 8px; border-radius: 12px;">
                                ${escapeHtml(item.badge || 'Draft')}
                            </span>
                        </div>
                        <div class="creator-draft-body">${escapeHtml(item.content || '')}</div>
                        <div class="creator-draft-footer">
                            <button type="button" class="btn-convert-to-post" data-title="${escapeHtml(item.title)}" data-body="${escapeHtml(item.content || '')}" title="Convert script into new social post">
                                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                <span>Convert to Post</span>
                            </button>
                            <div style="display: flex; gap: 6px;">
                                <button type="button" class="btn-copy-script btn-copy-creator-text" data-text="${escapeHtml(item.content || item.title)}" title="Copy script to clipboard">
                                    📋 Copy Script
                                </button>
                                <button type="button" class="btn-icon-subtle btn-delete-creator-draft" data-id="${item.id}" title="Delete draft">
                                    <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="12" x2="18" y2="18"></line></svg>
                                </button>
                            </div>
                        </div>
                    </div>
                `;
            }).join('');

            // Bind actions in list
            creatorDraftsList.querySelectorAll('.btn-convert-to-post').forEach(btn => {
                btn.addEventListener('click', () => {
                    const title = btn.dataset.title;
                    const body = btn.dataset.body;

                    const createPostModal = document.getElementById('createPostModal');
                    const postTitleInput = document.getElementById('postTitleInput');
                    const postCaptionInput = document.getElementById('postCaptionInput');

                    if (createPostModal) {
                        if (postTitleInput && title) postTitleInput.value = title;
                        if (postCaptionInput && body) postCaptionInput.value = body;

                        createPostModal.classList.add('show');
                        createPostModal.setAttribute('aria-hidden', 'false');
                        showToast(`✓ Creator script loaded into Post Composer!`, 'success');
                    }
                });
            });

            creatorDraftsList.querySelectorAll('.btn-copy-creator-text').forEach(btn => {
                btn.addEventListener('click', () => {
                    navigator.clipboard.writeText(btn.dataset.text).then(() => {
                        showToast('✓ Creator script copied to clipboard!', 'success');
                    }).catch(() => {
                        showToast('Failed to copy script', 'error');
                    });
                });
            });

            creatorDraftsList.querySelectorAll('.btn-delete-creator-draft').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const recordId = btn.dataset.id;
                    if (!confirm('Are you sure you want to delete this script/draft?')) return;
                    if (!currentCreatorFolder) return;
                    try {
                        const res = await fetch(`/api/folders/${currentCreatorFolder.id}/records/${recordId}`, {
                            method: 'DELETE',
                            headers: getCsrfHeaders()
                        });
                        if (res.ok) {
                            showToast('Draft removed from Creator Studio', 'success');
                            fetchCreatorDrafts();
                            if (typeof window.fetchFolders === 'function') window.fetchFolders();
                        } else {
                            showToast('Failed to delete draft', 'error');
                        }
                    } catch (err) {
                        console.error('Delete error:', err);
                        showToast('Error removing draft', 'error');
                    }
                });
            });
        }

        // Filter pills click
        filterPills.forEach(pill => {
            pill.addEventListener('click', () => {
                filterPills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                activeFilter = pill.dataset.filter || 'all';
                renderCreatorList();
            });
        });

        // Open Explore Folder
        if (btnExploreCreatorFolder) {
            btnExploreCreatorFolder.addEventListener('click', () => {
                if (currentCreatorFolder && typeof window.openFolderDetail === 'function') {
                    window.openFolderDetail(currentCreatorFolder.id);
                }
            });
        }

        // Modal Open / Close
        if (btnOpenAddCreatorModal && addCreatorDraftModal) {
            btnOpenAddCreatorModal.addEventListener('click', () => {
                addCreatorDraftForm.reset();
                addCreatorDraftModal.classList.add('show');
                addCreatorDraftModal.setAttribute('aria-hidden', 'false');
            });
        }

        function closeCreatorModal() {
            if (addCreatorDraftModal) {
                addCreatorDraftModal.classList.remove('show');
                addCreatorDraftModal.setAttribute('aria-hidden', 'true');
            }
        }

        if (closeCreatorModalBtn) closeCreatorModalBtn.addEventListener('click', closeCreatorModal);
        if (cancelCreatorModalBtn) cancelCreatorModalBtn.addEventListener('click', closeCreatorModal);

        // Form Submit
        if (addCreatorDraftForm) {
            addCreatorDraftForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const title = document.getElementById('creatorDraftTitle').value.trim();
                const itemType = document.getElementById('creatorDraftCategory').value;
                const status = document.getElementById('creatorDraftStatus').value;
                const platform = document.getElementById('creatorTargetPlatform').value.trim();
                const hook = document.getElementById('creatorDraftHook').value.trim();
                const content = document.getElementById('creatorDraftContent').value.trim();

                let fullContent = content;
                if (hook && !content.includes(hook)) {
                    fullContent = hook + '\n\n' + content;
                }

                let badgeColor = 'info';
                if (status === 'Ready to Post') badgeColor = 'success';
                else if (status === 'Polished') badgeColor = 'purple';
                else if (status === 'Filmed') badgeColor = 'warning';

                const payload = {
                    itemType: itemType,
                    title: title,
                    subtitle: platform || 'Social Media Copy',
                    content: fullContent,
                    badge: status,
                    badgeColor: badgeColor
                };

                try {
                    const res = await fetch('/api/folders/creator/records', {
                        method: 'POST',
                        headers: getCsrfHeaders(),
                        body: JSON.stringify(payload)
                    });

                    if (res.ok) {
                        showToast(`✓ Script "${title}" saved to Creator Studio!`, 'success');
                        closeCreatorModal();
                        addCreatorDraftForm.reset();
                        fetchCreatorDrafts();
                        if (typeof window.fetchFolders === 'function') window.fetchFolders();
                    } else {
                        showToast('Failed to save script', 'error');
                    }
                } catch (err) {
                    console.error('Error saving creator script:', err);
                    showToast('Server error saving draft', 'error');
                }
            });
        }

        fetchCreatorDrafts();
    }

    // -------------------------------------------------------------------------
    // Dark / Light Theme Mode Switcher
    // -------------------------------------------------------------------------
    function initThemeToggle() {
        const themeBtn = document.getElementById('themeToggleBtn');
        
        function getEffectiveTheme() {
            const saved = localStorage.getItem('theme') || localStorage.getItem('color-scheme');
            if (saved) return saved;
            return (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'light';
        }

        function applyTheme(theme, showNotification) {
            document.documentElement.setAttribute('data-theme', theme);
            const meta = document.querySelector('meta[name="color-scheme"]');
            if (meta) meta.content = theme;
            try {
                localStorage.setItem('theme', theme);
                localStorage.setItem('color-scheme', theme);
            } catch (e) {}

            // Update Chart.js themes if charts exist
            if (window.followerGrowthChart && window.followerGrowthChart.options) {
                const gridColor = theme === 'dark' ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';
                const textColor = theme === 'dark' ? '#9ca3af' : '#64748b';
                if (window.followerGrowthChart.options.scales && window.followerGrowthChart.options.scales.x) {
                    window.followerGrowthChart.options.scales.x.ticks.color = textColor;
                    window.followerGrowthChart.options.scales.x.grid.color = gridColor;
                }
                if (window.followerGrowthChart.options.scales && window.followerGrowthChart.options.scales.y) {
                    window.followerGrowthChart.options.scales.y.ticks.color = textColor;
                    window.followerGrowthChart.options.scales.y.grid.color = gridColor;
                }
                window.followerGrowthChart.update();
            }

            if (showNotification && typeof showToast === 'function') {
                showToast(theme === 'dark' ? '🌙 Dark mode activated' : '☀️ Light mode activated', 'info');
            }
        }

        // Initialize state
        const current = getEffectiveTheme();
        applyTheme(current, false);

        if (themeBtn) {
            themeBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const active = document.documentElement.getAttribute('data-theme') || getEffectiveTheme();
                const target = active === 'dark' ? 'light' : 'dark';
                applyTheme(target, true);
            });
        }

        // Listen for OS system theme changes
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                if (!localStorage.getItem('theme')) {
                    applyTheme(e.matches ? 'dark' : 'light', false);
                }
            });
        }

        // Sync theme across browser tabs
        window.addEventListener('storage', (e) => {
            if (e.key === 'theme' || e.key === 'color-scheme') {
                if (e.newValue) applyTheme(e.newValue, false);
            }
        });
    }

    initDesignerVault();
    initCreatorStudio();
    initThemeToggle();
});
