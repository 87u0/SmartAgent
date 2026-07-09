let toolList = [];

// Load tools on page load
document.addEventListener('DOMContentLoaded', function() {
    loadTools();
});

function loadTools() {
    fetch('/api/tools')
        .then(r => r.json())
        .then(data => {
            toolList = data.tools || [];
            const container = document.getElementById('toolList');
            if (container) {
                container.innerHTML = toolList.map(t =>
                    `<div class="tool-item">🔧 ${t.name}: ${t.description}</div>`
                ).join('');
            }
        })
        .catch(() => {});
}

function sendMessage() {
    const input = document.getElementById('userInput');
    const message = input.value.trim();
    if (!message) return;

    const messagesDiv = document.getElementById('chatMessages');
    const enableTools = document.getElementById('enableTools')?.checked !== false;
    const enableRag = document.getElementById('enableRag')?.checked || false;
    const statusBadge = document.getElementById('statusBadge');

    // Add user message
    addMessage('user', message);
    input.value = '';

    // Show thinking state
    statusBadge.textContent = '思考中...';
    statusBadge.className = 'status-badge thinking';

    // Add loading message
    const loadingId = 'loading-' + Date.now();
    addMessage('agent', '<div class="loading" id="' + loadingId + '">思考中</div>');

    // Call API
    fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            message: message,
            enableTools: enableTools,
            enableRag: enableRag
        })
    })
    .then(r => r.json())
    .then(data => {
        // Remove loading message
        const loading = document.getElementById(loadingId);
        if (loading) {
            const msgDiv = loading.closest('.message');
            if (msgDiv) msgDiv.remove();
        }

        const responseHtml = formatResponse(data.response);
        const infoParts = [];
        if (data.usedTools) infoParts.push('工具调用: ' + data.iterations + '步');
        if (data.usedRag) infoParts.push('RAG检索');

        const html = responseHtml + (infoParts.length > 0 ?
            `<div class="msg-iteration">${infoParts.join(' · ')}</div>` : '');
        addMessageHTML('agent', html);

        statusBadge.textContent = '就绪';
        statusBadge.className = 'status-badge';
    })
    .catch(e => {
        const loading = document.getElementById(loadingId);
        if (loading) {
            const msgDiv = loading.closest('.message');
            if (msgDiv) msgDiv.remove();
        }
        addMessage('agent', '❌ 请求失败: ' + e.message);
        statusBadge.textContent = '错误';
        statusBadge.className = 'status-badge';
    });
}

function addMessage(role, content) {
    const messagesDiv = document.getElementById('chatMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message ' + role + '-msg';
    msgDiv.innerHTML = `<div class="msg-content">${content}</div>`;
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function addMessageHTML(role, html) {
    const messagesDiv = document.getElementById('chatMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message ' + role + '-msg';
    msgDiv.innerHTML = `<div class="msg-content">${html}</div>`;
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function clearChat() {
    const messagesDiv = document.getElementById('chatMessages');
    messagesDiv.innerHTML = `
        <div class="message system-msg">
            <div class="msg-content">
                👋 对话已清空，可以开始新的问题了！
            </div>
        </div>`;
}

function formatResponse(text) {
    if (!text) return '';
    // Escape HTML
    text = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    // Convert code blocks
    text = text.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>');
    // Convert inline code
    text = text.replace(/`([^`]+)`/g, '<code>$1</code>');
    // Convert line breaks
    text = text.replace(/\n/g, '<br>');
    return text;
}
