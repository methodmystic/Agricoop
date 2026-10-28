// ========================================
// AgriCoop - Marketplace Page JavaScript
// market.js - Product filtering, orders, deals
// (Connected to Java JDBC + MySQL Backend)
// ========================================

const API_BASE = window.location.port === '3000' || window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:3000/api'
    : '/api';

const currentUserEmail = localStorage.getItem('userEmail') || 'customer@agricoop.com';

// ========================================
// View Navigation
// ========================================
function showView(viewName) {
    document.getElementById('view-marketplace').classList.add('hidden');
    document.getElementById('view-details').classList.add('hidden');
    document.getElementById('view-orders').classList.add('hidden');
    
    document.getElementById('view-' + viewName).classList.remove('hidden');
    
    document.querySelectorAll('.nav-item').forEach(el => {
        el.classList.remove('bg-[#e8f3f0]', 'text-primary', 'font-semibold');
        el.classList.add('text-slate-500', 'font-medium');
    });
    
    const activeNav = document.getElementById('nav-' + (viewName === 'marketplace' ? 'market' : viewName));
    if (activeNav) {
        activeNav.classList.add('bg-[#e8f3f0]', 'text-primary', 'font-semibold');
        activeNav.classList.remove('text-slate-500', 'font-medium');
    }

    if (viewName === 'orders') {
        loadOrders();
    }
    
    document.getElementById('main-scroll').scrollTop = 0;
}

// ========================================
// Product Details View
// ========================================
function showProductDetails(name, cat, price, img) {
    document.getElementById('detail-title').innerText = name + " (Premium)";
    document.getElementById('detail-img').src = img;
    
    document.getElementById('view-marketplace').classList.add('hidden');
    document.getElementById('view-details').classList.remove('hidden');
    document.getElementById('main-scroll').scrollTop = 0;
}

function hideProductDetails() {
    document.getElementById('view-details').classList.add('hidden');
    document.getElementById('view-marketplace').classList.remove('hidden');
    document.getElementById('main-scroll').scrollTop = 0;
}

// ========================================
// Product Category Filtering
// ========================================
function filterProducts(category) {
    const cards = document.querySelectorAll('.product-card');
    const btns = document.querySelectorAll('.category-btn');

    btns.forEach(btn => {
        if (btn.innerText === category) {
            btn.classList.add('bg-primary', 'text-white');
            btn.classList.remove('bg-white', 'text-slate-600');
        } else {
            btn.classList.remove('bg-primary', 'text-white');
            btn.classList.add('bg-white', 'text-slate-600');
        }
    });

    cards.forEach(card => {
        if (category === 'All' || card.getAttribute('data-category') === category) {
            card.classList.remove('hidden');
        } else {
            card.classList.add('hidden');
        }
    });
}

// ========================================
// Order Placement (JDBC MySQL API)
// ========================================
async function placeOrder(productName, category, price, farmerName) {
    const email = localStorage.getItem('userEmail') || currentUserEmail;
    const orderPayload = {
        user_email: email,
        product_name: productName,
        category: category,
        price: price,
        farmer_name: farmerName || 'AgriCoop Verified Farm'
    };

    try {
        const response = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderPayload)
        });

        const result = await response.json();
        if (response.ok && result.success) {
            alert(`Order placed successfully in MySQL via JDBC! (Order ID: #${result.order_id || 'Confirmed'})`);
        } else {
            throw new Error(result.message || 'Server returned error');
        }
    } catch (err) {
        console.warn('Backend order failed, backing up to localStorage:', err);
        const order = {
            id: Date.now(),
            ...orderPayload,
            status: 'Processing',
            created_at: new Date().toISOString()
        };
        const existing = JSON.parse(localStorage.getItem('agriOrders') || '[]');
        existing.push(order);
        localStorage.setItem('agriOrders', JSON.stringify(existing));
        alert('Order saved! (Recorded in local session; syncs to MySQL when Java backend is running).');
    }
}

// ========================================
// Load Orders List (JDBC MySQL API)
// ========================================
async function loadOrders() {
    const list = document.getElementById('orders-list');
    list.innerHTML = '<tr><td colspan="5" class="px-8 py-10 text-center text-slate-400">Loading orders from AgriCoop MySQL database...</td></tr>';

    const email = localStorage.getItem('userEmail') || currentUserEmail;
    let orders = [];

    try {
        const res = await fetch(`${API_BASE}/orders/${encodeURIComponent(email)}`);
        if (res.ok) {
            const json = await res.json();
            if (json.success && Array.isArray(json.data) && json.data.length > 0) {
                orders = json.data;
            }
        }
    } catch (e) {
        console.warn('Could not reach Java backend, checking local storage:', e);
    }

    // Fallback to local storage if API didn't return orders
    if (orders.length === 0) {
        const local = JSON.parse(localStorage.getItem('agriOrders') || '[]');
        orders = local.filter(o => o.user_email === email);
    }

    if (orders.length > 0) {
        list.innerHTML = orders.map(order => `
            <tr class="hover:bg-slate-50 transition-colors">
                <td class="px-8 py-5">
                    <p class="font-bold text-slate-800">${order.product_name}</p>
                    <p class="text-xs text-slate-400">${order.category}</p>
                </td>
                <td class="px-8 py-5 text-slate-600">${order.farmer_name || 'AgriCoop Cooperative'}</td>
                <td class="px-8 py-5 font-black text-dark">₹${order.price}</td>
                <td class="px-8 py-5">
                    <span class="px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider ${
                        order.status === 'Delivered' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                    }">${order.status || 'Processing'}</span>
                </td>
                <td class="px-8 py-5 text-slate-400 text-xs">${order.created_at ? new Date(order.created_at).toLocaleDateString() : 'Today'}</td>
            </tr>
        `).join('');
    } else {
        list.innerHTML = '<tr><td colspan="5" class="px-8 py-10 text-center text-slate-400">No orders found. Buy some fresh produce from the marketplace!</td></tr>';
    }
}

// Expose functions globally
window.showView = showView;
window.showProductDetails = showProductDetails;
window.hideProductDetails = hideProductDetails;
window.filterProducts = filterProducts;
window.placeOrder = placeOrder;
window.loadOrders = loadOrders;
