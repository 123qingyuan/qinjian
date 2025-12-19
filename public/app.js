// 全局变量
let currentUser = null;
let onlineUsers = new Set();
let notifications = [];
let users = [];

// 初始化应用
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

async function initializeApp() {
    // 初始化Socket.IO连接 (简化版本)
    initializeSocket();
    
    // 初始化导航
    initializeNavigation();
    
    // 初始化时间显示
    updateCurrentTime();
    setInterval(updateCurrentTime, 1000);
    
    // 加载初始数据
    await loadDashboardData();
    await loadUsers();
    await loadNotifications();
    
    // 初始化表单事件
    initializeForms();
    
    // 初始化筛选器
    initializeFilters();
}

// Socket.IO 初始化 (改为非Socket.IO版本)
function initializeSocket() {
    // 模拟连接成功
    console.log('连接到服务器成功');
    showNotification('连接成功', '已连接到服务器', 'success');
    
    // 启动定时更新模拟实时数据
    setInterval(() => {
        // 模拟实时更新
        updateRealTimeData();
    }, 5000);
}

// 导航初始化
function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetPage = this.dataset.page;
            if (!targetPage) return;
            
            // 更新导航状态
            navItems.forEach(nav => nav.classList.remove('active'));
            this.classList.add('active');
            
            // 切换页面
            switchPage(targetPage);
        });
    });
}

// 页面切换
function switchPage(pageName) {
    const pages = document.querySelectorAll('.page');
    
    pages.forEach(page => {
        page.classList.remove('active');
        if (page.id === pageName) {
            page.classList.add('active');
        }
    });
    
    // 根据页面加载相应数据
    switch(pageName) {
        case 'dashboard':
            loadDashboardData();
            break;
        case 'users':
            loadUsers();
            break;
        case 'monitoring':
            loadMonitoringData();
            break;
        case 'notifications':
            loadNotifications();
            break;
    }
}

// 加载仪表盘数据
async function loadDashboardData() {
    try {
        // 获取统计数据
        const statsResponse = await fetch('/api/stats');
        const stats = await statsResponse.json();
        
        // 更新统计卡片
        document.getElementById('totalUsers').textContent = stats.totalUsers || 0;
        document.getElementById('activeToday').textContent = stats.activeToday || 0;
        document.getElementById('unresolvedEvents').textContent = stats.unresolvedEvents || 0;
        
        // 加载最近活动
        await loadRecentActivities();
        
        // 初始化图表
        initializeCharts(stats);
        
    } catch (error) {
        console.error('加载仪表盘数据失败:', error);
        showNotification('加载失败', '无法加载仪表盘数据', 'error');
    }
}

// 加载最近活动
async function loadRecentActivities() {
    try {
        const activitiesContainer = document.getElementById('recentActivities');
        activitiesContainer.innerHTML = '';
        
        // 模拟最近活动数据
        const activities = [
            {
                id: 1,
                type: 'user_login',
                icon: 'fa-user-check',
                color: '#48bb78',
                title: '用户登录',
                description: '张大爷成功登录系统',
                time: '2分钟前'
            },
            {
                id: 2,
                type: 'abnormal_event',
                icon: 'fa-exclamation-triangle',
                color: '#ed8936',
                title: '异常事件',
                description: '检测到连续误触操作',
                time: '5分钟前'
            },
            {
                id: 3,
                type: 'family_added',
                icon: 'fa-user-plus',
                color: '#667eea',
                title: '家属添加',
                description: '李阿姨添加了新的家属联系方式',
                time: '10分钟前'
            }
        ];
        
        activities.forEach(activity => {
            const activityItem = createActivityItem(activity);
            activitiesContainer.appendChild(activityItem);
        });
        
    } catch (error) {
        console.error('加载最近活动失败:', error);
    }
}

// 创建活动项
function createActivityItem(activity) {
    const div = document.createElement('div');
    div.className = 'activity-item';
    div.innerHTML = `
        <div class="activity-icon" style="background: ${activity.color}">
            <i class="fas ${activity.icon}"></i>
        </div>
        <div class="activity-content">
            <h4>${activity.title}</h4>
            <p>${activity.description}</p>
        </div>
        <div class="activity-time">${activity.time}</div>
    `;
    return div;
}

// 初始化图表
function initializeCharts(stats) {
    // 这里可以使用Chart.js或其他图表库
    // 为了简单起见，这里只显示占位符
    const activityCanvas = document.getElementById('activityChart');
    const eventsCanvas = document.getElementById('eventsChart');
    
    if (activityCanvas) {
        const ctx = activityCanvas.getContext('2d');
        // 简单的文本占位符
        ctx.fillStyle = '#667eea';
        ctx.font = '16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('活跃度趋势图表', activityCanvas.width / 2, activityCanvas.height / 2);
    }
    
    if (eventsCanvas) {
        const ctx = eventsCanvas.getContext('2d');
        ctx.fillStyle = '#ed8936';
        ctx.font = '16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('异常事件分布图表', eventsCanvas.width / 2, eventsCanvas.height / 2);
    }
}

// 加载用户列表
async function loadUsers() {
    try {
        const response = await fetch('/api/users');
        users = await response.json();
        
        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = '';
        
        users.forEach(user => {
            const row = createUserRow(user);
            tbody.appendChild(row);
        });
        
    } catch (error) {
        console.error('加载用户列表失败:', error);
        showNotification('加载失败', '无法加载用户列表', 'error');
    }
}

// 创建用户行
function createUserRow(user) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>
            <div class="user-info-cell">
                <div class="user-avatar-small">${user.name.charAt(0)}</div>
                <div class="user-details">
                    <h4>${user.name}</h4>
                    <p>${user.phone || '未设置'}</p>
                </div>
            </div>
        </td>
        <td>${user.device_id || '-'}</td>
        <td>-</td>
        <td>${formatDate(user.created_at)}</td>
        <td>${formatDate(user.last_active)}</td>
        <td>
            <span class="status-badge ${isUserActive(user.last_active) ? 'active' : 'inactive'}">
                ${isUserActive(user.last_active) ? '活跃' : '非活跃'}
            </span>
        </td>
        <td>
            <div class="action-buttons">
                <button class="action-btn" onclick="viewUserDetail('${user.id}')" title="查看详情">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="action-btn" onclick="addFamilyMember('${user.id}')" title="添加家属">
                    <i class="fas fa-user-plus"></i>
                </button>
                <button class="action-btn" onclick="editUser('${user.id}')" title="编辑用户">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn" onclick="deleteUser('${user.id}')" title="删除用户">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </td>
    `;
    return tr;
}

// 加载监控数据
async function loadMonitoringData() {
    try {
        // 加载在线用户
        await loadOnlineUsers();
        
        // 加载事件流
        await loadEventStream();
        
    } catch (error) {
        console.error('加载监控数据失败:', error);
    }
}

// 加载在线用户
async function loadOnlineUsers() {
    const onlineUsersContainer = document.getElementById('onlineUsers');
    
    // 模拟在线用户数据
    const mockOnlineUsers = [
        { id: '1', name: '张大爷', status: '使用微信', lastActive: '刚刚' },
        { id: '2', name: '李阿姨', status: '今日步数: 2,345', lastActive: '1分钟前' },
        { id: '3', name: '王大爷', status: '观看新闻', lastActive: '2分钟前' },
        { id: '4', name: '赵阿姨', status: '通话中', lastActive: '3分钟前' }
    ];
    
    onlineUsersContainer.innerHTML = '';
    
    mockOnlineUsers.forEach(user => {
        const userItem = createOnlineUserItem(user);
        onlineUsersContainer.appendChild(userItem);
    });
}

// 创建在线用户项
function createOnlineUserItem(user) {
    const div = document.createElement('div');
    div.className = 'online-user-item';
    div.onclick = () => selectUserForDetail(user.id);
    div.innerHTML = `
        <div class="online-indicator"></div>
        <div class="user-avatar-small">${user.name.charAt(0)}</div>
        <div class="user-details">
            <h4>${user.name}</h4>
            <p>${user.status}</p>
        </div>
        <div class="activity-time">${user.lastActive}</div>
    `;
    return div;
}

// 加载事件流
async function loadEventStream() {
    const eventStreamContainer = document.getElementById('eventStream');
    
    // 模拟事件流数据
    const mockEvents = [
        {
            id: 1,
            type: 'app_launch',
            title: '应用启动',
            content: '张大爷启动了微信应用',
            time: '2分钟前'
        },
        {
            id: 2,
            type: 'button_click',
            title: '按钮点击',
            content: '李阿姨点击了"发送消息"按钮',
            time: '3分钟前'
        },
        {
            id: 3,
            type: 'error_detected',
            title: '错误检测',
            content: '检测到王大爷连续误触3次',
            time: '5分钟前'
        }
    ];
    
    eventStreamContainer.innerHTML = '';
    
    mockEvents.forEach(event => {
        const eventItem = createEventItem(event);
        eventStreamContainer.appendChild(eventItem);
    });
}

// 创建事件项
function createEventItem(event) {
    const div = document.createElement('div');
    div.className = 'event-item';
    div.innerHTML = `
        <div class="event-time">${event.time}</div>
        <div class="event-content">
            <div class="event-type">${event.title}</div>
            <div>${event.content}</div>
        </div>
    `;
    return div;
}

// 加载通知
async function loadNotifications() {
    try {
        // 模拟通知数据
        const mockNotifications = [
            {
                id: 1,
                type: 'warning',
                title: '异常事件提醒',
                message: '张大爷连续触发了5次误操作，建议检查',
                time: '10分钟前',
                read: false
            },
            {
                id: 2,
                type: 'info',
                title: '用户添加成功',
                message: '新用户李阿姨已成功注册',
                time: '1小时前',
                read: false
            },
            {
                id: 3,
                type: 'success',
                title: '远程协助完成',
                message: '已成功帮助王大爷解决微信使用问题',
                time: '2小时前',
                read: true
            }
        ];
        
        notifications = mockNotifications;
        updateNotificationList();
        updateNotificationBadge();
        
    } catch (error) {
        console.error('加载通知失败:', error);
    }
}

// 更新通知列表
function updateNotificationList(filter = 'all') {
    const container = document.getElementById('notificationsList');
    container.innerHTML = '';
    
    const filteredNotifications = filter === 'all' 
        ? notifications 
        : notifications.filter(n => n.type === filter);
    
    filteredNotifications.forEach(notification => {
        const item = createNotificationItem(notification);
        container.appendChild(item);
    });
    
    if (filteredNotifications.length === 0) {
        container.innerHTML = '<div style="padding: 2rem; text-align: center; color: #718096;">暂无通知</div>';
    }
}

// 创建通知项
function createNotificationItem(notification) {
    const div = document.createElement('div');
    div.className = `notification-item ${!notification.read ? 'unread' : ''}`;
    div.onclick = () => markNotificationAsRead(notification.id);
    
    const iconMap = {
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle',
        success: 'fa-check-circle',
        error: 'fa-times-circle'
    };
    
    div.innerHTML = `
        <div class="notification-icon ${notification.type}">
            <i class="fas ${iconMap[notification.type]}"></i>
        </div>
        <div class="notification-content">
            <div class="notification-title">${notification.title}</div>
            <div class="notification-message">${notification.message}</div>
            <div class="notification-time">${notification.time}</div>
        </div>
    `;
    return div;
}

// 更新通知徽章
function updateNotificationBadge() {
    const badge = document.getElementById('notificationBadge');
    const unreadCount = notifications.filter(n => !n.read).length;
    if (badge) {
        badge.textContent = unreadCount;
    }
}

// 标记通知为已读
function markNotificationAsRead(notificationId) {
    const notification = notifications.find(n => n.id === notificationId);
    if (notification) {
        notification.read = true;
        updateNotificationList();
        updateNotificationBadge();
    }
}

// 更新实时数据
function updateRealTimeData() {
    // 随机更新一些统计数据来模拟实时更新
    const totalUsersElement = document.getElementById('totalUsers');
    const activeTodayElement = document.getElementById('activeToday');
    const unresolvedEventsElement = document.getElementById('unresolvedEvents');
    
    if (totalUsersElement) {
        totalUsersElement.textContent = 4 + Math.floor(Math.random() * 2);
    }
    if (activeTodayElement) {
        activeTodayElement.textContent = Math.floor(Math.random() * 4) + 1;
    }
    if (unresolvedEventsElement) {
        unresolvedEventsElement.textContent = Math.floor(Math.random() * 3);
    }
}

// 显示通知
function showNotification(title, message, type = 'info') {
    const notification = {
        id: Date.now(),
        type: type,
        title: title,
        message: message,
        time: '刚刚',
        read: false
    };
    
    notifications.unshift(notification);
    updateNotificationList();
    updateNotificationBadge();
}

// 工具函数
function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN');
    } catch (e) {
        return dateString;
    }
}

function isUserActive(lastActive) {
    if (!lastActive) return false;
    try {
        const lastActiveTime = new Date(lastActive);
        const now = new Date();
        const diffHours = (now - lastActiveTime) / (1000 * 60 * 60);
        return diffHours < 24;
    } catch (e) {
        return false;
    }
}

// 用户操作函数
function viewUserDetail(userId) {
    console.log('查看用户详情:', userId);
    showNotification('用户详情', `查看用户 ${userId} 的详细信息`, 'info');
}

function addFamilyMember(userId) {
    console.log('添加家属:', userId);
    showNotification('添加家属', `为用户 ${userId} 添加家属`, 'info');
}

function editUser(userId) {
    console.log('编辑用户:', userId);
    showNotification('编辑用户', `编辑用户 ${userId} 的信息`, 'info');
}

function deleteUser(userId) {
    if (confirm('确定要删除这个用户吗？')) {
        console.log('删除用户:', userId);
        showNotification('删除成功', `用户 ${userId} 已删除`, 'success');
        loadUsers();
    }
}

// 模态框函数
function showAddUserModal() {
    const modal = document.getElementById('addUserModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function showAddFamilyMemberModal(userId) {
    const modal = document.getElementById('addFamilyMemberModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// 表单初始化
function initializeForms() {
    // 添加用户表单
    const addUserForm = document.getElementById('addUserForm');
    if (addUserForm) {
        addUserForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            const userData = {
                name: formData.get('name'),
                phone: formData.get('phone'),
                email: formData.get('email'),
                deviceId: formData.get('deviceId')
            };
            
            // 调用API添加用户
            fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData)
            })
            .then(response => response.json())
            .then(data => {
                console.log('用户添加成功:', data);
                showNotification('添加成功', '用户已成功添加', 'success');
                closeModal('addUserModal');
                loadUsers();
            })
            .catch(error => {
                console.error('添加用户失败:', error);
                showNotification('添加失败', '无法添加用户', 'error');
            });
        });
    }
    
    // 添加家属表单
    const addFamilyMemberForm = document.getElementById('addFamilyMemberForm');
    if (addFamilyMemberForm) {
        addFamilyMemberForm.addEventListener('submit', function(e) {
            e.preventDefault();
            showNotification('添加成功', '家属已成功添加', 'success');
            closeModal('addFamilyMemberModal');
        });
    }
    
    // 点击模态框外部关闭
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });
}

// 筛选器初始化
function initializeFilters() {
    // 通知筛选按钮
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            const filter = this.dataset.filter;
            updateNotificationList(filter);
        });
    });
    
    // 用户搜索
    const userSearch = document.getElementById('userSearch');
    if (userSearch) {
        userSearch.addEventListener('input', function() {
            console.log('搜索用户:', this.value);
        });
    }
    
    // 状态筛选
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            console.log('状态筛选:', this.value);
        });
    }
}

// 实时监控相关函数
function selectUserForDetail(userId) {
    const userDetailCard = document.getElementById('userDetailCard');
    if (userDetailCard) {
        userDetailCard.style.display = 'block';
    }
}

function handleStatusUpdate(data) {
    console.log('状态更新:', data);
}

function handleAbnormalEvent(data) {
    console.log('异常事件:', data);
    showNotification('异常事件', data.description, 'warning');
}

// 更新当前时间
function updateCurrentTime() {
    const currentTimeElement = document.getElementById('currentTime');
    if (currentTimeElement) {
        const now = new Date();
        currentTimeElement.textContent = now.toLocaleTimeString('zh-CN');
    }
}