$(document).ready(function() {
    console.log('🚀 [NAV-BAR] Loading admin notification count...');
    loadAdminNotificationCount();
    setInterval(loadAdminNotificationCount, 30000);

    $(window).on('focus load pageshow', function() {
        console.log('🔄 [NAV-BAR] Refresh triggered.');
        loadAdminNotificationCount();
    });
});

function loadAdminNotificationCount() {
    console.log('🔄 [ADMIN] Fetching count...');
    $.get('/notifications/api/unread-count')
        .done(function(response) {
            if (response.success) {
                const count = response.unreadCount;
                updateAdminNotificationBadges(count);
            }
        });
}

function updateAdminNotificationBadges(count) {
    const displayCount = count > 99 ? '99+' : count.toString();
    $('#adminNotificationCount').text(displayCount);
    $('#adminNotificationCount').toggle(count > 0);

    if (count > 0) {
        $('.notification-btn').addClass('has-notifications');
    } else {
        $('.notification-btn').removeClass('has-notifications');
    }
}

// Expose globally if needed
window.loadAdminNotificationCount = loadAdminNotificationCount;
window.updateAdminNotificationBadges = updateAdminNotificationBadges;
